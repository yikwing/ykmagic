package com.yikwing.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlin.coroutines.cancellation.CancellationException

/**
 * 将 HTTP 响应结果转换为业务数据或抛出异常
 *
 * 使用 [ApiConfig.errorCodeChecker] 判断错误码是否表示失败
 *
 * @param result HTTP 响应结果
 * @return 成功时返回数据（服务端返回 null 时为 null），失败时抛出 ApiException
 * @throws ApiException 当错误码检查器返回 true 时抛出
 */
internal fun <T> transformHttpResult(result: BaseHttpResult<T>): T? =
    if (ApiConfig.errorCodeChecker(result.errorCode)) {
        throw ApiException(result.errorCode, result.errorMsg)
    } else {
        result.data
    }

/**
 * 归一化为 [ApiException]：已是 ApiException 的原样返回，其余包装为默认错误码
 */
internal fun Throwable.toApiException(): ApiException = this as? ApiException ?: ApiException.createDefault(message, this)

/**
 * 流式 API 请求转换
 *
 * 将 API 调用转换为带状态的 Flow，自动处理 Loading、Success、Error 状态
 *
 * [RequestState.Loading] 由最下游的 `onStart` 发出，因此它始终是第一个元素，
 * 且不受上游 `flowOn` 通道缓冲与异常传播的影响
 *
 * @param block API 调用的 suspend 函数
 * @return Flow<RequestState<T?>> 包含完整请求状态的流；服务端返回 null 时 Success.value 为 null
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
fun <T> requestStateFlow(block: suspend () -> BaseHttpResult<T>): Flow<RequestState<T?>> =
    flow<RequestState<T?>> {
        // 转换结果，错误码校验失败时抛出 ApiException，交由下游 catch 处理
        emit(RequestState.Success(transformHttpResult(block())))
    }.flowOn(Dispatchers.IO)
        .catch { exception ->
            // 取消异常必须重抛，否则会吞掉协程取消信号
            if (exception is CancellationException) {
                throw exception
            }
            ApiConfig.logger("requestStateFlow", "Error: ${exception.message}", exception)
            emit(RequestState.Error(exception.toApiException()))
        }.onStart { emit(RequestState.Loading) }

/**
 * 单次 API 请求转换
 *
 * 适用于只需要一次性结果的场景，如上传日志、文件上传、提交表单等
 * 使用 Kotlin 标准库的 Result 类型，无 Loading 状态
 *
 * @param block API 调用的 suspend 函数
 * @return Result<T?> 包装的结果，失败时 exception 恒为 [ApiException]
 * @throws CancellationException 协程被取消时原样重抛，不会包装进 Result
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
suspend fun <T> requestResult(block: suspend () -> BaseHttpResult<T>): Result<T?> =
    try {
        // 转换结果，成功返回数据，错误码校验失败时抛出 ApiException
        Result.success(transformHttpResult(block()))
    } catch (exception: CancellationException) {
        // 取消异常需要重抛，不捕获
        throw exception
    } catch (exception: Exception) {
        ApiConfig.logger("requestResult", "Error: ${exception.message}", exception)
        Result.failure(exception.toApiException())
    }
