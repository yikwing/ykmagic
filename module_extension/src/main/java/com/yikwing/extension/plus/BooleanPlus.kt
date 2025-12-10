package com.yikwing.extension.plus

/**
 * Boolean 扩展函数，提供更流畅的条件执行语法
 * 灵感来自 WhatIf 库，但更简洁实用
 */

// ========== 基础条件执行 ==========

/**
 * 当 Boolean 为 true 时执行 block，并返回自身以支持链式调用
 *
 * @param block 当值为 true 时执行的代码块
 * @return 返回原始 Boolean 值
 *
 * 使用示例:
 * ```kotlin
 * isValid.yes { println("验证通过") }
 *     .no { println("验证失败") }
 *
 * checkPermission()
 *     .yes { showContent() }
 *     .no { requestPermission() }
 * ```
 */
inline fun Boolean.yes(block: () -> Unit): Boolean {
    if (this) block()
    return this
}

/**
 * 当 Boolean 为 false 时执行 block，并返回自身以支持链式调用
 *
 * @param block 当值为 false 时执行的代码块
 * @return 返回原始 Boolean 值
 *
 * 使用示例:
 * ```kotlin
 * hasNetworkConnection()
 *     .yes { loadData() }
 *     .no { showOfflineMessage() }
 * ```
 */
inline fun Boolean.no(block: () -> Unit): Boolean {
    if (!this) block()
    return this
}

// ========== 条件转换（返回值） ==========

/**
 * 根据 Boolean 值返回不同的结果
 *
 * @param whenTrue 当值为 true 时返回的结果
 * @param whenFalse 当值为 false 时返回的结果
 * @return 根据条件返回对应的值
 *
 * 使用示例:
 * ```kotlin
 * val message = isSuccess.choose("成功", "失败")
 * val color = isDarkMode.choose(Color.White, Color.Black)
 * val visibility = isVisible.choose(View.VISIBLE, View.GONE)
 * ```
 */
fun <T> Boolean.choose(
    whenTrue: T,
    whenFalse: T,
): T = if (this) whenTrue else whenFalse

/**
 * 根据 Boolean 值延迟计算并返回结果
 * 相比 choose，只会执行对应分支的 lambda，避免不必要的计算
 *
 * @param whenTrue 当值为 true 时执行的 lambda
 * @param whenFalse 当值为 false 时执行的 lambda
 * @return 根据条件返回对应 lambda 的结果
 *
 * 使用示例:
 * ```kotlin
 * val data = hasCache.chooseLazy(
 *     whenTrue = { loadFromCache() },  // 只在 true 时执行
 *     whenFalse = { loadFromNetwork() } // 只在 false 时执行
 * )
 *
 * val user = isLoggedIn.chooseLazy(
 *     whenTrue = { getCurrentUser() },
 *     whenFalse = { getGuestUser() }
 * )
 * ```
 */
inline fun <T> Boolean.chooseLazy(
    whenTrue: () -> T,
    whenFalse: () -> T,
): T = if (this) whenTrue() else whenFalse()

// ========== 可空值操作 ==========

/**
 * 当 Boolean 为 true 时返回值，否则返回 null
 *
 * 使用示例:
 * ```kotlin
 * val result = isSuccess.thenValue("成功")
 * // isSuccess = true  -> "成功"
 * // isSuccess = false -> null
 *
 * val user = isLoggedIn.thenValue(currentUser)
 * ```
 */
fun <T> Boolean.thenValue(value: T): T? = if (this) value else null

/**
 * 当 Boolean 为 true 时执行 block 并返回结果，否则返回 null
 *
 * 使用示例:
 * ```kotlin
 * val data = hasPermission.thenRun { loadSensitiveData() }
 * // hasPermission = true  -> loadSensitiveData() 的结果
 * // hasPermission = false -> null
 *
 * val file = fileExists.thenRun { File(path).readText() }
 * ```
 */
inline fun <T> Boolean.thenRun(block: () -> T): T? = if (this) block() else null

// ========== 实用工具 ==========

/**
 * 将 Boolean 转换为 Int (true -> 1, false -> 0)
 *
 * 使用示例:
 * ```kotlin
 * val count = isChecked.toInt()  // 1 或 0
 * val total = items.sumOf { it.isValid.toInt() }
 * ```
 */
fun Boolean.toInt(): Int = if (this) 1 else 0

/**
 * 将 Int 转换为 Boolean (1 -> true, 0 -> false)
 *
 * @throws IllegalArgumentException 如果值不是 0 或 1
 *
 * 使用示例:
 * ```kotlin
 * val isEnabled = 1.toBoolean()  // true
 * val isDisabled = 0.toBoolean()  // false
 *
 * // 从数据库读取布尔值
 * val isActive = cursor.getInt(columnIndex).toBoolean()
 * ```
 */
fun Int.toBoolean(): Boolean {
    require(this in 0..1) { "Int value must be 0 or 1, but was $this" }
    return this == 1
}

// ========== 逻辑运算 ==========

/**
 * 对多个 Boolean 值执行 AND 运算
 *
 * 使用示例:
 * ```kotlin
 * val allValid = isNameValid.and(isAgeValid, isEmailValid)
 * // 等同于: isNameValid && isAgeValid && isEmailValid
 *
 * val canSubmit = hasPermission.and(isFormValid, isNetworkAvailable)
 * ```
 */
fun Boolean.and(vararg others: Boolean): Boolean = this && others.all { it }

/**
 * 对多个 Boolean 值执行 OR 运算
 *
 * 使用示例:
 * ```kotlin
 * val hasAnyError = hasNetworkError.or(hasValidationError, hasServerError)
 * // 等同于: hasNetworkError || hasValidationError || hasServerError
 *
 * val canProceed = isAdmin.or(isOwner, hasSpecialPermission)
 * ```
 */
fun Boolean.or(vararg others: Boolean): Boolean = this || others.any { it }
