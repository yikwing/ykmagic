package com.yikwing.ykquickdev.repository

import android.util.Log
import com.yikwing.network.ApiException
import com.yikwing.network.RequestState
import com.yikwing.ykquickdev.api.apiserver.HttpBinApi
import com.yikwing.ykquickdev.api.entity.Headers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.koin.core.annotation.Single
import kotlin.coroutines.cancellation.CancellationException

interface OtherRepository {
    fun initHttpBinData(): Flow<RequestState<Headers>>
}

@Single
class OtherRepositoryImpl(
    private val httpBinApi: HttpBinApi,
) : OtherRepository {
    /**
     * 获取 HttpBin Headers 数据
     */
    override fun initHttpBinData(): Flow<RequestState<Headers>> =
        flow {
            emit(RequestState.Loading)
            val data = httpBinApi.getHeaders().headers
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
