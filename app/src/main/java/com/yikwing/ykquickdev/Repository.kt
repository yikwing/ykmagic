package com.yikwing.ykquickdev

import com.yikwing.network.ApiException
import com.yikwing.network.RequestState
import com.yikwing.network.requestResult
import com.yikwing.network.requestStateFlow
import com.yikwing.ykquickdev.api.apiserver.Repo
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.api.entity.Headers
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class Repository
    @Inject
    constructor(
        private val repo: Repo,
    ) {
        /**
         * 获取 HttpBin Headers 数据
         * HttpBinHeaders 不是 BaseHttpResult 包装，手动处理
         */
        suspend fun initHttpBinData(): RequestState<Headers> =
            try {
                repo.binPost()
                RequestState.Success(repo.binGet().headers)
            } catch (exception: Exception) {
                RequestState.Error(ApiException.createDefault(exception.message, exception))
            }

        /**
         * 获取玩安卓章节数据
         * 使用 requestStateFlow 解析 BaseHttpResult 包装的响应
         */
        fun initWanAndroidData(): Flow<RequestState<List<ChapterBean>?>> =
            requestStateFlow {
                repo.getChapters()
            }

        /**
         * 获取玩安卓章节数据 (单次请求)
         * 使用 requestResult 返回 Result 类型
         */
        suspend fun initWanAndroidData2(): Result<List<ChapterBean>?> =
            requestResult {
                repo.getChapters()
            }
    }
