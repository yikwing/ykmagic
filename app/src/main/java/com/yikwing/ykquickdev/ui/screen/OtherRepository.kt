package com.yikwing.ykquickdev.ui.screen

import android.util.Log
import com.yikwing.network.ApiException
import com.yikwing.network.RequestState
import com.yikwing.ykquickdev.api.apiserver.Repo
import com.yikwing.ykquickdev.api.entity.Headers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class OtherRepository
    @Inject
    constructor(
        private val repo: Repo,
    ) {
        /**
         * 获取 HttpBin Headers 数据
         */
        fun initHttpBinData(): Flow<RequestState<Headers>> =
            flow {
                emit(RequestState.Loading)
                val data = repo.binGet().headers
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
