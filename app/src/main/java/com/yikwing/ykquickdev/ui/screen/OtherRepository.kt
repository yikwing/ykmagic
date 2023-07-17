package com.yikwing.ykquickdev.ui.screen

import com.yikwing.ykquickdev.api.entity.Headers
import com.yikwing.ykquickdev.api.provider.ApiProvider
import com.yikwing.network.RequestState
import javax.inject.Inject

class OtherRepository @Inject constructor() {

    suspend fun initHttpBinData(): RequestState<Headers> {
        return try {
            RequestState.Success(ApiProvider.createHttpBinService().getOtherHeaders().headers)
        } catch (exception: Exception) {
            RequestState.Error(exception)
        }
    }
}
