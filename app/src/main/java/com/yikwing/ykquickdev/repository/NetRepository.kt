package com.yikwing.ykquickdev.repository

import com.yikwing.network.ApiException
import com.yikwing.network.RequestState
import com.yikwing.network.requestResult
import com.yikwing.network.requestStateFlow
import com.yikwing.ykquickdev.api.apiserver.HttpBinApi
import com.yikwing.ykquickdev.api.apiserver.WanAndroidApi
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.api.entity.Headers
import com.yikwing.ykquickdev.db.ChapterDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class NetRepository(
    private val httpBinApi: HttpBinApi,
    private val wanAndroidApi: WanAndroidApi,
    private val chapterDao: ChapterDao,
) {
    /**
     * 获取 HttpBin Headers 数据
     */
    suspend fun initHttpBinData(): RequestState<Headers> =
        try {
            httpBinApi.postData()
            RequestState.Success(httpBinApi.getHeaders().headers)
        } catch (exception: Exception) {
            RequestState.Error(ApiException.createDefault(exception.message, exception))
        }

    /**
     * 获取玩安卓章节数据
     * 使用 requestStateFlow 解析 BaseHttpResult 包装的响应
     */
    fun initWanAndroidData(): Flow<RequestState<List<ChapterBean>?>> =
        requestStateFlow {
            wanAndroidApi.getChapters()
        }

    /**
     * 获取玩安卓章节数据 (单次请求)
     * 使用 requestResult 返回 Result 类型
     */
    suspend fun initWanAndroidData2(): Result<List<ChapterBean>?> =
        requestResult {
            wanAndroidApi.getChapters()
        }.onSuccess { _data ->
            _data.let { chapterDao.insertChapters(it) }
        }

    // ==================== Chapters SSOT（读写分离）====================

    /**
     * 读取：观察 Room 数据（SSOT）
     * 返回 RequestState 包装，Room 数据变化时自动更新
     */
    fun observeChapters(): Flow<RequestState<List<ChapterBean>>> =
        chapterDao.observeAllChapters().map { _data ->
            if (_data.isEmpty()) {
                RequestState.Error(ApiException.createDefault("数据为空"))
            } else {
                RequestState.Success(_data)
            }
        }

    /**
     * 写入：从网络获取数据并缓存到 Room
     * 调用后 Room 数据更新，observeChapters 自动收到新数据
     */
    suspend fun fetchAndCacheChapters() {
        runCatching { wanAndroidApi.getChapters() }.onSuccess { result ->
            result.data.let { chapterDao.insertChapters(it) }
        }
    }
}
