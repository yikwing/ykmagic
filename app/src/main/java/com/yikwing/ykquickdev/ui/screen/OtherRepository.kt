package com.yikwing.ykquickdev.ui.screen

import com.yikwing.network.ApiException
import com.yikwing.network.RequestState
import com.yikwing.ykquickdev.api.entity.Headers
import com.yikwing.ykquickdev.api.provider.ApiProvider
import javax.inject.Inject

class OtherRepository
    @Inject
    constructor() {
        suspend fun initHttpBinData(): RequestState<Headers> =
            try {
                RequestState.Success(ApiProvider.createHttpBinService().getOtherHeaders().headers)
            } catch (exception: Exception) {
                RequestState.Error(ApiException.createDefault(exception.message, exception))
            }
    }
