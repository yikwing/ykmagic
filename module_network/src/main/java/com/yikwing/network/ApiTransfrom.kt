package com.yikwing.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.cancellation.CancellationException

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
 * requestFlow { apiService.getUserList() }
 *     .collect { state ->
 *         when (state) {
 *             is RequestState.Loading -> showLoading()
 *             is RequestState.Success -> showData(state.value)
 *             is RequestState.Error -> showError(state.throwable)
 *         }
 *     }
 * ```
 */
inline fun <T> requestFlow(crossinline block: suspend () -> BaseHttpResult<T>) =
    flow {
        emit(RequestState.Loading)
        val result = block()
        if (result.errorCode != 0) {
            emit(RequestState.Error(ApiException(result.errorCode, result.errorMsg)))
        } else {
            emit(RequestState.Success(result.data))
        }
    }.flowOn(Dispatchers.IO).catch { exception ->
        // 检查是否是取消异常，如果是，则重新抛出
        if (exception is CancellationException) {
            throw exception
        }
        // 所有异常都在这里被捕捉
        Log.e("requestFlow", "Error: ${exception.message}", exception)
        emit(RequestState.Error(ApiException.createDefault(exception.message, exception)))
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
 * request { apiService.uploadLog(logData) }
 *     .onSuccess { data -> Log.d("Upload", "成功: $data") }
 *     .onFailure { error -> Log.e("Upload", "失败: ${error.message}") }
 *
 * // 示例 2: 使用 fold 转换结果
 * val message = request { apiService.submit(form) }.fold(
 *     onSuccess = { "提交成功" },
 *     onFailure = { "提交失败: ${it.message}" }
 * )
 *
 * // 示例 3: 使用 getOrNull 获取数据
 * val data = request { apiService.getData() }.getOrNull()
 * ```
 */
suspend inline fun <T> request(crossinline block: suspend () -> BaseHttpResult<T>): Result<T?> =
    try {
        val result = block()
        if (result.errorCode != 0) {
            Result.failure(ApiException(result.errorCode, result.errorMsg))
        } else {
            Result.success(result.data)
        }
    } catch (exception: CancellationException) {
        // 取消异常需要重新抛出，不捕获
        throw exception
    } catch (exception: Exception) {
        Log.e("request", "Error: ${exception.message}", exception)
        Result.failure(ApiException.createDefault(exception.message, exception))
    }
