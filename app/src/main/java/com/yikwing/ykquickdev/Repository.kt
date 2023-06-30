package com.yikwing.ykquickdev

import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.api.entity.Headers
import com.yikwing.ykquickdev.api.provider.ApiProvider
import com.yk.yknetwork.RequestState
import com.yk.yknetwork.transformApi
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class Repository @Inject constructor() {

    suspend fun initHttpBinData(): RequestState<Headers> {
        return try {
            RequestState.Success(ApiProvider.createHttpBinService().getOtherHeaders().headers)
        } catch (exception: Exception) {
            RequestState.Error(exception)
        }
    }

    fun initWanAndroidData(): Flow<RequestState<List<ChapterBean>?>> {
        return transformApi {
            ApiProvider.createWanAndroidService().getChapters()
        }
    }
}
