package com.yikwing.ykquickdev.ui.utils

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

// ============ 基础导航 ============

/** 导航到指定路由 */
fun NavBackStack<NavKey>.navigate(route: NavKey) {
    add(route)
}

/** 返回上一页（根路由不操作） */
fun NavBackStack<NavKey>.pop(): Boolean {
    return if (size > 1) {
        removeLastOrNull()
        true
    } else {
        false
    }
}

/** 返回到根路由 */
fun NavBackStack<NavKey>.popRoot() {
    while (size > 1) {
        removeLastOrNull()
    }
}

// ============ popTo 系列 ============

/**
 * 返回到指定路由
 *
 * @param inclusive true 则目标路由也会被弹出
 *
 * 示例：栈 A -> B -> C -> D
 * - popTo(B) → A -> B
 * - popTo(B, true) → A
 */
fun NavBackStack<NavKey>.popTo(
    route: NavKey,
    inclusive: Boolean = false,
): Boolean {
    val targetIndex = indexOfLast { it == route }
    if (targetIndex == -1) return false

    val targetSize = if (inclusive) targetIndex else targetIndex + 1
    if (targetSize < 1) return false // 保护根路由

    // 批量删除：从目标位置后一个开始删除到末尾
    while (size > targetSize) {
        removeLastOrNull()
    }

    return true
}

// ============ 组合导航 ============

/**
 * 替换到指定路由（清理中间路由）
 *
 * 示例：栈 A -> B -> C
 * - replaceUpTo(D, upTo = A) → A -> D
 * - replaceUpTo(D, upTo = A, inclusive = true) → D
 */
fun NavBackStack<NavKey>.replaceUpTo(
    route: NavKey,
    upTo: NavKey,
    inclusive: Boolean = false,
) {
    popTo(upTo, inclusive)
    navigate(route)
}

// ============ 栈管理 ============

/**
 * 设置新的根路由（破坏性操作）
 *
 * 使用场景：登出、完成引导、注册成功
 *
 * 示例：栈 A -> B -> C
 * - setRoot(D) → D
 */
fun NavBackStack<NavKey>.setRoot(route: NavKey) {
    clear()
    add(route)
}
