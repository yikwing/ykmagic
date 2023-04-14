package com.yk.ykproxy.startup

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.studydemo.startup.Initializer

class AppInitializer(private val context: Context) {

    private val taskList = mutableListOf<Initializer<*>>()

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: AppInitializer? = null

        fun getInstance(context: Context): AppInitializer {
            return instance ?: synchronized(this) {
                instance ?: AppInitializer(context).also { instance = it }
            }
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

    private fun discover(taskList: MutableList<Initializer<*>>): List<Initializer<*>> {
        val inDeCountMap = mutableMapOf<Initializer<*>, Int>()
        val zeroDeque = ArrayDeque<Initializer<*>>()
        val finalList = mutableListOf<Initializer<*>>()

        for (task in taskList) {
            val dependencies = task.dependencies()
            inDeCountMap[task] = dependencies.size
            if (dependencies.isEmpty()) {
                zeroDeque.add(task)
            }
        }

        while (zeroDeque.isNotEmpty()) {
            val inDeTask = zeroDeque.removeFirst()
            finalList.add(inDeTask)
            for (task in taskList) {
                val dependencies = task.dependencies()
                if (dependencies.any { it == inDeTask.javaClass }) {
                    val count = inDeCountMap[task]!! - 1
                    inDeCountMap[task] = count
                    if (count == 0) {
                        zeroDeque.add(task)
                    }
                }
            }
        }

        if (finalList.size != taskList.size) {
            throw Error("存在回环依赖")
        }

        return finalList
    }
}
