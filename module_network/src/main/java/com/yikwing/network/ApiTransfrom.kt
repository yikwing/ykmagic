package com.yikwing.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.cancellation.CancellationException

/**
 * 将 HTTP 响应结果转换为业务数据或抛出异常
 *
 * 使用 [ApiConfig.errorCodeChecker] 判断错误码是否表示失败
 *
 * @param result HTTP 响应结果
 * @return 成功时返回数据，失败时抛出 ApiException
 * @throws ApiException 当错误码检查器返回 true 时抛出
 */
@PublishedApi
internal fun <T> transformHttpResult(result: BaseHttpResult<T>): T? =
    if (ApiConfig.errorCodeChecker(result.errorCode)) {
        throw ApiException(result.errorCode, result.errorMsg)
    } else {
        result.data
    }

/**
 * 流式 API 请求转换
 *
 * 将 API 调用转换为带状态的 Flow，自动处理 Loading、Success、Error 状态
 *
 * @param block API 调用的 suspend 函数
 * @return Flow<RequestState<T>> 包含完整请求状态的流
 *
 * 使用示例：
 * ```
 * requestStateFlow { apiService.getUserList() }
 *     .collect { state ->
 *         when (state) {
 *             is RequestState.Loading -> showLoading()
 *             is RequestState.Success -> showData(state.value)
 *             is RequestState.Error -> showError(state.throwable)
 *         }
 *     }
 * ```
 */
inline fun <T> requestStateFlow(crossinline block: suspend () -> BaseHttpResult<T>) =
    flow {
        emit(RequestState.Loading)
        val result = block()
        // 使用公共函数转换结果，失败时会抛出 ApiException
        val data = transformHttpResult(result)
        emit(RequestState.Success(data))
    }.flowOn(Dispatchers.IO).catch { exception ->
        // 检查是否是取消异常，如果是，则重新抛出
        if (exception is CancellationException) {
            throw exception
        }
        // 所有异常都在这里被捕捉
        Log.e("requestStateFlow", "Error: ${exception.message}", exception)
        // ApiException 直接使用，其他异常转换为默认 ApiException
        val apiException =
            exception as? ApiException ?: ApiException.createDefault(exception.message, exception)
        emit(RequestState.Error(apiException))
    }

/**
 * 单次 API 请求转换
 *
 * 适用于只需要一次性结果的场景，如上传日志、文件上传、提交表单等
 * 使用 Kotlin 标准库的 Result 类型，无 Loading 状态
 *
 * @param block API 调用的 suspend 函数
 * @return Result<T?> 包装的结果（Success 或 Failure）
 *
 * 使用示例：
 * ```
 * // 示例 1: 使用 onSuccess/onFailure 处理结果
 * requestResult { apiService.uploadLog(logData) }
 *     .onSuccess { data -> Log.d("Upload", "成功: $data") }
 *     .onFailure { error -> Log.e("Upload", "失败: ${error.message}") }
 *
 * // 示例 2: 使用 fold 转换结果
 * val message = requestResult { apiService.submit(form) }.fold(
 *     onSuccess = { "提交成功" },
 *     onFailure = { "提交失败: ${it.message}" }
 * )
 *
 * // 示例 3: 使用 getOrNull 获取数据
 * val data = requestResult { apiService.getData() }.getOrNull()
 * ```
 */
suspend inline fun <T> requestResult(crossinline block: suspend () -> BaseHttpResult<T>): Result<T?> =
    try {
        val result = block()
        // 使用公共函数转换结果，成功返回数据，失败抛出 ApiException
        Result.success(transformHttpResult(result))
    } catch (exception: CancellationException) {
        // 取消异常需要重新抛出，不捕获
        throw exception
    } catch (exception: Exception) {
        Log.e("requestResult", "Error: ${exception.message}", exception)
        // ApiException 直接使用，其他异常转换为默认 ApiException
        val apiException =
            exception as? ApiException ?: ApiException.createDefault(exception.message, exception)
        Result.failure(apiException)
    }
