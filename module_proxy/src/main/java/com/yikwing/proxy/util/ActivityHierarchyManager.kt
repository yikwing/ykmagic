package com.yikwing.proxy.util

import android.app.Activity
import android.util.Log
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Activity 层级管理器
 *
 * 用于管理应用中所有 Activity 的栈，提供统一的 Activity 生命周期管理和栈操作功能。
 *
 * 特性：
 * - 线程安全：使用 CopyOnWriteArrayList 保证并发安全
 * - 内存安全：使用 WeakReference 避免内存泄漏
 * - 自动清理：自动清理已销毁的 Activity 引用
 *
 * 使用方式：
 * ```
 * // 在 Application.ActivityLifecycleCallbacks 中注册和注销
 * override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
 *     ActivityHierarchyManager.register(activity)
 * }
 *
 * override fun onActivityDestroyed(activity: Activity) {
 *     ActivityHierarchyManager.unregister(activity)
 * }
 * ```
 *
 * @author yikwing
 */
object ActivityHierarchyManager {
    private const val TAG = "ActivityHierarchy"

    /**
     * Activity 栈，使用 CopyOnWriteArrayList 保证线程安全
     * 使用 WeakReference 避免内存泄漏
     */
    private val activities = CopyOnWriteArrayList<WeakReference<Activity>>()

    /**
     * 注册 Activity
     *
     * 在 Activity 创建时调用，将 Activity 添加到栈中。
     * 会自动检查是否已存在，避免重复注册。
     *
     * @param activity 要注册的 Activity
     */
    fun register(activity: Activity) {
        // 检查是否已经注册，避免重复添加
        val exists = activities.any { it.get() == activity }
        if (!exists) {
            activities.add(WeakReference(activity))
        }
        // 注册时清理无效引用
        cleanupInvalidReferences()
    }

    /**
     * 注销 Activity
     *
     * 在 Activity 销毁时调用，将 Activity 从栈中移除。
     *
     * @param activity 要注销的 Activity
     */
    fun unregister(activity: Activity) {
        activities.removeAll { it.get() == activity || it.get() == null }
    }

    /**
     * 根据传入的数量关闭多个 Activity
     *
     * 从栈顶开始关闭指定数量的 Activity。
     *
     * @param popNum 要关闭的 Activity 数量
     * @deprecated 使用 [finishTopActivities] 替代，命名更清晰
     */
    @Deprecated(
        message = "使用 finishTopActivities(count) 替代",
        replaceWith = ReplaceWith("finishTopActivities(popNum)")
    )
    fun finishActivities(popNum: Int) {
        finishTopActivities(popNum)
    }

    /**
     * 从栈顶开始关闭指定数量的 Activity
     *
     * @param count 要关闭的 Activity 数量，必须大于 0
     * @return 实际关闭的 Activity 数量
     */
    fun finishTopActivities(count: Int): Int {
        if (count <= 0) return 0

        val validActivities = getValidActivities()
        val toFinish = validActivities.takeLast(count.coerceAtMost(validActivities.size))

        var finishedCount = 0
        toFinish.forEach { activity ->
            if (!activity.isFinishing && !activity.isDestroyed) {
                activity.finish()
                finishedCount++
            }
        }

        return finishedCount
    }

    /**
     * 获取当前有效的 Activity 数量
     *
     * @return Activity 数量
     */
    fun getActivityCount(): Int {
        return getValidActivities().size
    }

    /**
     * 打印 Activity 栈信息
     *
     * @param isDebug 是否为 Debug 模式，只有 Debug 模式下才会打印
     */
    fun printActivityHierarchy(isDebug: Boolean) {
        if (!isDebug) return

        val validActivities = getValidActivities()
        Log.d(TAG, "========== Activity Stack (Total: ${validActivities.size}) ==========")
        validActivities.forEachIndexed { index, activity ->
            val status = when {
                activity.isDestroyed -> "[DESTROYED]"
                activity.isFinishing -> "[FINISHING]"
                else -> "[ACTIVE]"
            }
            Log.d(TAG, "[$index] $status ${activity.javaClass.simpleName} @${Integer.toHexString(activity.hashCode())}")
        }
        Log.d(TAG, "=".repeat(50))
    }

    /**
     * 计算从指定 Activity 开始需要关闭的 Activity 数量
     *
     * @param activityClass Activity 类型
     * @return 需要关闭的 Activity 数量，如果未找到返回 -1
     * @deprecated 使用 [getIndexFromTop] 替代，命名更清晰
     */
    @Deprecated(
        message = "使用 getIndexFromTop(activityClass) 替代",
        replaceWith = ReplaceWith("getIndexFromTop(activityClass)")
    )
    fun calculatePopNum(activityClass: Class<out Activity>): Int {
        return getIndexFromTop(activityClass)
    }

    /**
     * 获取指定类型的 Activity 距离栈顶的距离
     *
     * @param activityClass Activity 类型
     * @return 距离栈顶的索引（0 表示就是栈顶），如果未找到返回 -1
     */
    fun getIndexFromTop(activityClass: Class<out Activity>): Int {
        val validActivities = getValidActivities()
        val reversedList = validActivities.asReversed()
        return reversedList.indexOfFirst { it.javaClass == activityClass }
    }

    /**
     * 获取栈顶 Activity
     *
     * @return 栈顶 Activity，如果栈为空则返回 null
     */
    fun getTopActivity(): Activity? {
        return getValidActivities().lastOrNull()
    }

    /**
     * 结束所有 Activity
     */
    fun finishAllActivities() {
        val activitiesToFinish = getValidActivities()
        activitiesToFinish.forEach { activity ->
            if (!activity.isFinishing && !activity.isDestroyed) {
                activity.finish()
            }
        }
        activities.clear()
    }

    /**
     * 检查是否包含指定的 Activity 实例
     *
     * @param activity Activity 实例
     * @return true 如果存在，false 如果不存在
     */
    fun contains(activity: Activity): Boolean {
        return getValidActivities().any { it == activity }
    }

    /**
     * 检查是否包含指定类型的 Activity
     *
     * @param activityClass Activity 类型
     * @return true 如果存在，false 如果不存在
     */
    fun contains(activityClass: Class<out Activity>): Boolean {
        return getValidActivities().any { it.javaClass == activityClass }
    }

    /**
     * 获取当前有效的 Activity 栈
     *
     * @return 有效的 Activity 列表，从栈底到栈顶排序
     */
    fun getActivityStack(): List<Activity> {
        return getValidActivities()
    }

    /**
     * 关闭直到指定类型的 Activity
     *
     * 从栈顶开始关闭 Activity，直到遇到指定类型的 Activity。
     *
     * @param activityClass 目标 Activity 类型
     * @param inclusive 是否也关闭目标 Activity，默认 false
     * @return 实际关闭的 Activity 数量，如果未找到目标 Activity 返回 -1
     */
    fun finishUntil(activityClass: Class<out Activity>, inclusive: Boolean = false): Int {
        val validActivities = getValidActivities()
        val reversedList = validActivities.asReversed()
        val targetIndex = reversedList.indexOfFirst { it.javaClass == activityClass }

        if (targetIndex == -1) return -1

        val endIndex = if (inclusive) targetIndex + 1 else targetIndex
        val toFinish = reversedList.take(endIndex)

        var finishedCount = 0
        toFinish.forEach { activity ->
            if (!activity.isFinishing && !activity.isDestroyed) {
                activity.finish()
                finishedCount++
            }
        }

        return finishedCount
    }

    /**
     * 关闭除了指定类型外的所有 Activity
     *
     * @param activityClass 要保留的 Activity 类型
     * @return 实际关闭的 Activity 数量
     */
    fun finishAllExcept(activityClass: Class<out Activity>): Int {
        val validActivities = getValidActivities()
        val toFinish = validActivities.filter { it.javaClass != activityClass }

        var finishedCount = 0
        toFinish.forEach { activity ->
            if (!activity.isFinishing && !activity.isDestroyed) {
                activity.finish()
                finishedCount++
            }
        }

        return finishedCount
    }

    /**
     * 获取指定索引位置的 Activity
     *
     * @param index 索引位置，0 表示栈底
     * @return 对应位置的 Activity，如果索引越界返回 null
     */
    fun getActivityAt(index: Int): Activity? {
        val validActivities = getValidActivities()
        return validActivities.getOrNull(index)
    }

    /**
     * 清理已经被回收或销毁的 Activity 引用
     *
     * 移除满足以下条件的引用：
     * - WeakReference 已被 GC 回收（get() 返回 null）
     * - Activity 正在结束（isFinishing == true）
     * - Activity 已销毁（isDestroyed == true）
     */
    private fun cleanupInvalidReferences() {
        activities.removeAll { ref ->
            val activity = ref.get()
            activity == null || activity.isFinishing || activity.isDestroyed
        }
    }

    /**
     * 获取所有有效的 Activity 列表
     *
     * 过滤掉已被 GC 回收、正在结束或已销毁的 Activity。
     * 此方法不会修改原始列表，只是返回过滤后的结果。
     *
     * @return 有效的 Activity 列表
     */
    private fun getValidActivities(): List<Activity> {
        return activities.mapNotNull { ref ->
            val activity = ref.get()
            if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                activity
            } else {
                null
            }
        }
    }
}
