package com.yikwing.proxy.startup

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log

class AppInitializer(
    private val context: Application,
) {
    private val taskList = mutableListOf<Initializer<*>>()

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: AppInitializer? = null

        fun getInstance(context: Application): AppInitializer =
            instance ?: synchronized(this) {
                instance ?: AppInitializer(context).also { instance = it }
            }
    }

    fun addTask(initializer: Initializer<*>): AppInitializer {
        taskList.add(initializer)
        return this
    }

    fun build(debug: Boolean = false) {
        val discoverList = discover(taskList)

        discoverList.forEach {
            if (debug) Log.d("AppInitializer", "${it.javaClass.simpleName} run")
            it.create(context = context)
        }
    }

    /**
     * 使用拓扑排序（Kahn's 算法）解析任务依赖关系
     *
     * 算法思路：
     * 1. 构建反向依赖图：记录哪些任务依赖当前任务
     * 2. 计算入度：每个任务的依赖数量
     * 3. 零入度入队：无依赖的任务可立即执行
     * 4. 逐个消除：处理任务后，减少依赖它的任务的入度
     * 5. 循环检测：如果无法处理所有任务，说明存在循环依赖
     *
     * 时间复杂度：O(V + E)，V=任务数，E=依赖边数
     * 空间复杂度：O(V + E)
     */
    private fun discover(taskList: List<Initializer<*>>): List<Initializer<*>> {
        val inDeCountMap = mutableMapOf<Initializer<*>, Int>()
        val zeroDeque = ArrayDeque<Initializer<*>>()
        val finalList = mutableListOf<Initializer<*>>()

        // 构建反向依赖图：depClass -> 依赖它的任务列表
        val dependents = mutableMapOf<Class<*>, MutableList<Initializer<*>>>()

        // 阶段1：计算入度并构建反向依赖图
        for (task in taskList) {
            val dependencies = task.dependencies()
            inDeCountMap[task] = dependencies.size

            // 为每个依赖项建立反向关系
            dependencies.forEach { depClass ->
                dependents.getOrPut(depClass) { mutableListOf() }.add(task)
            }

            // 无依赖的任务加入零入度队列
            if (dependencies.isEmpty()) {
                zeroDeque.add(task)
            }
        }

        // 阶段2：拓扑排序 - 逐个处理零入度任务
        while (zeroDeque.isNotEmpty()) {
            val currentTask = zeroDeque.removeFirst()
            finalList.add(currentTask)

            // 通过反向依赖图直接找到依赖当前任务的所有任务
            dependents[currentTask.javaClass]?.forEach { dependentTask ->
                val currentCount = inDeCountMap[dependentTask] ?: return@forEach
                val count = currentCount - 1
                inDeCountMap[dependentTask] = count

                // 入度变为0，说明所有依赖已满足，可以执行了
                if (count == 0) {
                    zeroDeque.add(dependentTask)
                }
            }
        }

        // 阶段3：循环依赖检测
        if (finalList.size != taskList.size) {
            throw Error("存在回环依赖")
        }

        return finalList
    }
}
