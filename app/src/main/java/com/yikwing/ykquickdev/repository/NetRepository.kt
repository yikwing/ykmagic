package com.yikwing.ykquickdev.repository

import com.yikwing.network.ApiConfig
import com.yikwing.network.ApiException
import com.yikwing.network.requestResult
import com.yikwing.ykquickdev.api.apiserver.WanAndroidApi
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.db.ChapterDao
import kotlin.coroutines.cancellation.CancellationException
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
     * @return 失败时为 [ApiException]（网络与写库失败均是），CancellationException 会重抛
     */
    override suspend fun fetchAndCacheChapters(): Result<Unit> {
        val chapters = requestResult { wanAndroidApi.getChapters() }.getOrElse { return Result.failure(it) }
        if (chapters == null) return Result.success(Unit)
        // Not mapCatching: it would also swallow CancellationException.
        return try {
            chapterDao.insertChapters(chapters)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Must log here: once the cache is non-empty the UI keeps showing it and hides this failure.
            ApiConfig.logger("fetchAndCacheChapters", "Cache chapters failed: ${e.message}", e)
            Result.failure(ApiException.createDefault(e.message, e))
        }
    }
}
