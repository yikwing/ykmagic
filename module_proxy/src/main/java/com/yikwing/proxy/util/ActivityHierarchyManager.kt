package com.yikwing.proxy.util

import android.app.Activity
import android.util.Log
import java.lang.ref.WeakReference

object ActivityHierarchyManager {
    private val activities = mutableListOf<WeakReference<Activity>>()

    // 注册 Activity
    fun register(activity: Activity) {
        activities.add(WeakReference(activity))
        cleanupInvalidReferences()
    }

    // 注销 Activity
    fun unregister(activity: Activity) {
        activities.removeAll { it.get() == activity || it.get() == null }
    }

    // 根据传入的数量关闭多个 Activity
    fun finishActivities(popNum: Int) {
        if (popNum <= 0) return

        // 清理无效引用
        cleanupInvalidReferences()

        // 从后往前遍历，关闭最新的 popNum 个 Activity
        val validActivities = activities.mapNotNull { it.get() }
        val toFinish = validActivities.takeLast(popNum.coerceAtMost(validActivities.size))

        toFinish.forEach { activity ->
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
    }

    // 获取当前活动的 Activity 数量
    fun getActivityCount(): Int {
        cleanupInvalidReferences()
        return activities.size
    }

    // 打印 activity 链路
    fun printActivityHierarchy(isDebug: Boolean) {
        if (isDebug) {
            cleanupInvalidReferences()
            activities.forEachIndexed { index, reference ->
                Log.d(
                    "ActivityHierarchyManager",
                    "[$index] Activity: ${reference.get()?.javaClass?.simpleName}",
                )
            }
        }
    }

    /**
     * 计算从指定 Activity 开始需要关闭的 Activity 数量。
     * 返回值为需要关闭的 Activity 数量。
     * 如果未找到指定 Activity，返回 -1。
     */
    fun calculatePopNum(activityClass: Class<out Activity>): Int {
        cleanupInvalidReferences()
        val reversedList = activities.asReversed()
        val index = reversedList.indexOfFirst { it.get()?.javaClass == activityClass }
        return if (index != -1) index else -1
    }

    /**
     * 清理已经被回收或结束的Activity引用
     */
    private fun cleanupInvalidReferences() {
        activities.removeAll { it.get() == null || it.get()?.isFinishing == true }
    }

    /**
     * 获取顶部Activity
     * @return 栈顶Activity，如果没有则返回null
     */
    fun getTopActivity(): Activity? {
        cleanupInvalidReferences()
        return activities.lastOrNull()?.get()
    }

    /**
     * 结束所有Activity
     */
    fun finishAllActivities() {
        val activitiesToFinish = activities.mapNotNull { it.get() }
        activitiesToFinish.forEach { activity ->
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
        activities.clear()
    }
}
