package com.yikwing.network

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.StateFlow

/**
 * 包含 [RequestState] 状态的 LiveData 类型别名
 */
typealias RStateLiveData<T> = LiveData<RequestState<T>>

/**
 * 包含 [RequestState] 状态的 StateFlow 类型别名
 */
typealias RStateFlow<T> = StateFlow<RequestState<T>>

/**
 * 请求状态封装
 *
 * 表示异步请求的三种状态：加载中、成功、失败
 *
 * @param T 成功时的数据类型
 */
sealed interface RequestState<out T> {
    /**
     * 加载中状态
     */
    data object Loading : RequestState<Nothing>

    /**
     * 成功状态
     *
     * @property value 成功时的数据
     */
    data class Success<out T>(
        val value: T,
    ) : RequestState<T>

    /**
     * 失败状态
     *
     * @property throwable 失败时的异常
     */
    data class Error(
        val throwable: ApiException,
    ) : RequestState<Nothing>
}

/**
 * 当状态为成功时执行回调
 *
 * @param success 成功时的回调，参数为成功的数据
 * @return 原始的 RequestState，支持链式调用
 *
 * 使用示例：
 * ```
 * state.onSuccess { data ->
 *     println("成功: $data")
 * }.onFailure { error ->
 *     println("失败: ${error.message}")
 * }
 * ```
 */
inline fun <T> RequestState<T>.onSuccess(success: (T) -> Unit): RequestState<T> {
    if (this is RequestState.Success) {
        success(value)
    }
    return this
}

/**
 * 当状态为失败时执行回调
 *
 * @param failure 失败时的回调，参数为异常对象
 * @return 原始的 RequestState，支持链式调用
 *
 * 使用示例：
 * ```
 * state.onFailure { error ->
 *     showError(error.message)
 * }
 * ```
 */
inline fun <T> RequestState<T>.onFailure(failure: (ApiException) -> Unit): RequestState<T> {
    if (this is RequestState.Error) {
        failure(throwable)
    }
    return this
}

/**
 * 获取成功时的值，否则返回 null
 *
 * Loading 和 Error 状态都返回 null
 *
 * @return 成功时的值或 null
 */
fun <T> RequestState<T>.getOrNull(): T? = if (this is RequestState.Success) value else null

/**
 * 获取成功时的值，否则通过回调计算默认值
 *
 * @param onFailure 当状态为 Loading 或 Error 时的回调，Loading 时 exception 为 null
 * @return 成功时的值或通过回调计算的默认值
 *
 * 使用示例：
 * ```
 * val userName = userState.getOrElse { exception ->
 *     if (exception != null) "加载失败" else "加载中..."
 * }
 * ```
 */
inline fun <T> RequestState<T>.getOrElse(onFailure: (ApiException?) -> T): T =
    when (this) {
        is RequestState.Success -> value
        is RequestState.Error -> onFailure(throwable)
        is RequestState.Loading -> onFailure(null)
    }

/**
 * 获取成功时的值，否则返回默认值
 *
 * @param defaultValue 默认值
 * @return 成功时的值或默认值
 */
fun <T> RequestState<T>.getOrDefault(defaultValue: T): T = if (this is RequestState.Success) value else defaultValue

/**
 * 是否处于加载中状态
 */
val <T> RequestState<T>.isLoading: Boolean
    get() = this is RequestState.Loading

/**
 * 是否处于成功状态
 */
val <T> RequestState<T>.isSuccess: Boolean
    get() = this is RequestState.Success

/**
 * 是否处于失败状态
 */
val <T> RequestState<T>.isFailure: Boolean
    get() = this is RequestState.Error

/**
 * 获取失败时的异常，否则返回 null
 *
 * Loading 和 Success 状态都返回 null
 *
 * @return 失败时的异常或 null
 */
fun <T> RequestState<T>.exceptionOrNull(): ApiException? = if (this is RequestState.Error) throwable else null

/**
 * 请求状态回调构建器
 *
 * 用于 DSL 风格的状态处理，支持分别定义三种状态的回调
 *
 * @param T 成功时的数据类型
 */
class ResultBuilder<T> {
    /**
     * 加载中状态的回调
     */
    var onLoading: (() -> Unit)? = null

    /**
     * 成功状态的回调
     */
    var onSuccess: ((data: T) -> Unit)? = null

    /**
     * 失败状态的回调
     */
    var onFailure: ((e: ApiException) -> Unit)? = null

    companion object {
        inline fun <T> build(init: ResultBuilder<T>.() -> Unit) = ResultBuilder<T>().apply(init)
    }
}

/**
 * 以 DSL 风格观察 LiveData 的状态变化
 *
 * 使用 [ResultBuilder] 提供声明式的状态处理方式
 *
 * @param owner 生命周期拥有者
 * @param init ResultBuilder 的初始化 lambda
 *
 * 使用示例：
 * ```
 * viewModel.userLiveData.observeState(viewLifecycleOwner) {
 *     onLoading = { showLoading() }
 *     onSuccess = { user -> showUser(user) }
 *     onFailure = { error -> showError(error) }
 * }
 * ```
 */
@MainThread
inline fun <T> RStateLiveData<T>.observeState(
    owner: LifecycleOwner,
    init: ResultBuilder<T>.() -> Unit,
) {
    val result = ResultBuilder.build(init)

    observe(owner) { state ->
        when (state) {
            is RequestState.Loading -> result.onLoading?.invoke()
            is RequestState.Success -> result.onSuccess?.invoke(state.value)
            is RequestState.Error -> result.onFailure?.invoke(state.throwable)
        }
    }
}

/**
 * 以 DSL 风格收集 Flow 的状态变化
 *
 * 使用 [ResultBuilder] 提供声明式的状态处理方式
 *
 * @param init ResultBuilder 的初始化 lambda
 *
 * 使用示例：
 * ```
 * lifecycleScope.launch {
 *     viewModel.userFlow.collectState {
 *         onLoading = { showLoading() }
 *         onSuccess = { user -> showUser(user) }
 *         onFailure = { error -> showError(error) }
 *     }
 * }
 * ```
 */
@MainThread
suspend inline fun <T> RStateFlow<T>.collectState(init: ResultBuilder<T>.() -> Unit) {
    val result = ResultBuilder.build(init)

    collect { state ->
        when (state) {
            is RequestState.Loading -> result.onLoading?.invoke()
            is RequestState.Success -> result.onSuccess?.invoke(state.value)
            is RequestState.Error -> result.onFailure?.invoke(state.throwable)
        }
    }
}
