package com.yikwing.ykquickdev.repository

import android.util.Log
import com.yikwing.network.ApiException
import com.yikwing.network.RequestState
import com.yikwing.ykquickdev.api.apiserver.HttpApi
import com.yikwing.ykquickdev.api.entity.Headers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.koin.core.annotation.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class OtherRepository(
    private val httpApi: HttpApi,
) {
    /**
     * 获取 HttpBin Headers 数据
     */
    fun initHttpBinData(): Flow<RequestState<Headers>> =
        flow {
            emit(RequestState.Loading)
            val data = httpApi.binGet().headers
            emit(RequestState.Success(data))
        }.flowOn(Dispatchers.IO).catch { exception ->
            if (exception is CancellationException) throw exception
            Log.e("initHttpBinData", "Error: ${exception.message}", exception)
            val apiException =
                exception as? ApiException ?: ApiException.createDefault(
                    exception.message,
                    exception,
                )
            emit(RequestState.Error(apiException))
        }
}
