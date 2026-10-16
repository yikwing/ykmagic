package com.yikwing.ykquickdev.repository

import com.yikwing.network.ApiException
import com.yikwing.network.requestResult
import com.yikwing.ykquickdev.api.apiserver.WanAndroidApi
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.db.ChapterDao
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Singleton

interface NetRepository {
    fun observeChapters(): Flow<List<ChapterBean>>

    suspend fun fetchAndCacheChapters(): Result<Unit>
}

@Singleton
class NetRepositoryImpl(
    private val wanAndroidApi: WanAndroidApi,
    private val chapterDao: ChapterDao,
) : NetRepository {
    // ==================== Chapters SSOT（读写分离）====================

    /**
     * 读取：观察 Room 数据（SSOT）
     * 返回原始缓存列表，Room 数据变化时自动更新；空列表的含义（加载中/失败/无数据）
     * 需结合 [fetchAndCacheChapters] 的结果判断，由调用方组合
     */
    override fun observeChapters(): Flow<List<ChapterBean>> = chapterDao.observeAllChapters()

    /**
     * 写入：从网络获取数据并缓存到 Room
     * 调用后 Room 数据更新，observeChapters 自动收到新数据
     *
     * @return 失败时为 [ApiException]，CancellationException 会重抛
     */
    override suspend fun fetchAndCacheChapters(): Result<Unit> =
        requestResult { wanAndroidApi.getChapters() }
            .map { data -> if (data != null) chapterDao.insertChapters(data) }
}
