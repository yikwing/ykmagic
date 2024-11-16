package com.yikwing.ykquickdev

import com.yikwing.network.ApiException
import com.yikwing.network.RequestState
import com.yikwing.network.transformApi
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.api.entity.Headers
import com.yikwing.ykquickdev.api.provider.ApiProvider
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class Repository
    @Inject
    constructor() {
        suspend fun initHttpBinData(): RequestState<Headers> =
            try {
                RequestState.Success(ApiProvider.createHttpBinService().getOtherHeaders().headers)
            } catch (exception: Exception) {
                RequestState.Error(ApiException.createDefault(exception.message, exception))
            }

        fun initWanAndroidData(): Flow<RequestState<List<ChapterBean>?>> =
            transformApi {
                ApiProvider.createWanAndroidService().getChapters()
            }
    }
