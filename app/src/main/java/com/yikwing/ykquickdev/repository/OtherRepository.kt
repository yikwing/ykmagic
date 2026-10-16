package com.yikwing.ykquickdev.repository

import com.yikwing.network.requestRawResult
import com.yikwing.ykquickdev.api.apiserver.HttpBinApi
import com.yikwing.ykquickdev.api.entity.Headers
import org.koin.core.annotation.Singleton

interface OtherRepository {
    suspend fun initHttpBinData(): Result<Headers>
}

@Singleton
class OtherRepositoryImpl(
    private val httpBinApi: HttpBinApi,
) : OtherRepository {
    /**
     * 获取 HttpBin Headers 数据
     *
     * @return 失败时为 [com.yikwing.network.ApiException]，CancellationException 会重抛
     */
    override suspend fun initHttpBinData(): Result<Headers> = requestRawResult { httpBinApi.getHeaders().headers }
}
