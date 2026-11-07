package com.yikwing.ykquickdev.repository

import com.yikwing.network.ApiConfig
import com.yikwing.network.ApiException
import com.yikwing.network.BaseHttpResult
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.api.apiserver.WanAndroidApi
import com.yikwing.ykquickdev.db.ChapterDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * NetRepositoryImpl 单元测试
 *
 * 覆盖 fetchAndCacheChapters 的异常包装与 CancellationException 重抛路径。
 *
 * `ApiConfig.logger` 是进程级单例,默认走 android.util.Log,每个用例后恢复默认。
 */
class NetRepositoryImplTest {
    private val defaultLogger = ApiConfig.logger
    private val wanAndroidApi = mockk<WanAndroidApi>()
    private val chapterDao = mockk<ChapterDao>(relaxed = true)
    private val repository =
        NetRepositoryImpl(
            wanAndroidApi = wanAndroidApi,
            chapterDao = chapterDao,
        )

    @Before
    fun setup() {
        ApiConfig.logger = { _, _, _ -> }
    }

    @After
    fun teardown() {
        ApiConfig.logger = defaultLogger
    }

    // ==================== fetchAndCacheChapters ====================

    @Test
    fun `fetchAndCacheChapters should cache chapters when request succeeds`() =
        runBlocking {
            val chapters = listOf(chapter(id = 1))
            coEvery { wanAndroidApi.getChapters() } returns
                BaseHttpResult(data = chapters, errorMsg = "", errorCode = 0)

            val result = repository.fetchAndCacheChapters()

            assertThat(result.isSuccess, `is`(true))
            coVerify(exactly = 1) { chapterDao.replaceChapters(chapters) }
        }

    @Test
    fun `fetchAndCacheChapters should return failure when request throws`() =
        runBlocking {
            coEvery { wanAndroidApi.getChapters() } throws IOException("network down")

            val result = repository.fetchAndCacheChapters()

            assertThat(result.exceptionOrNull(), instanceOf(ApiException::class.java))
            assertThat(result.exceptionOrNull()!!.message, `is`("network down"))
            coVerify(exactly = 0) { chapterDao.replaceChapters(any()) }
        }

    @Test
    fun `fetchAndCacheChapters should rethrow CancellationException`() =
        runBlocking {
            coEvery { wanAndroidApi.getChapters() } throws CancellationException("user-cancel")

            val caught =
                try {
                    repository.fetchAndCacheChapters()
                    null
                } catch (e: CancellationException) {
                    e
                }

            assertThat(caught, notNullValue())
            assertThat(caught!!.message, `is`("user-cancel"))
        }

    @Test
    fun `fetchAndCacheChapters should return failure when caching throws`() =
        runBlocking {
            val chapters = listOf(chapter(id = 1))
            coEvery { wanAndroidApi.getChapters() } returns
                BaseHttpResult(data = chapters, errorMsg = "", errorCode = 0)
            val cause = IllegalStateException("disk full")
            coEvery { chapterDao.replaceChapters(any()) } throws cause
            var logged: Throwable? = null
            ApiConfig.logger = { _, _, throwable -> logged = throwable }

            val result = repository.fetchAndCacheChapters()

            assertThat(result.exceptionOrNull(), instanceOf(ApiException::class.java))
            assertThat(result.exceptionOrNull()!!.message, `is`("disk full"))
            assertThat(logged, sameInstance<Throwable>(cause))
        }

    @Test
    fun `fetchAndCacheChapters should rethrow CancellationException from caching`() =
        runBlocking {
            coEvery { wanAndroidApi.getChapters() } returns
                BaseHttpResult(data = listOf(chapter(id = 1)), errorMsg = "", errorCode = 0)
            coEvery { chapterDao.replaceChapters(any()) } throws CancellationException("cache-cancel")

            val caught =
                try {
                    repository.fetchAndCacheChapters()
                    null
                } catch (e: CancellationException) {
                    e
                }

            assertThat(caught, notNullValue())
            assertThat(caught!!.message, `is`("cache-cancel"))
        }

    @Test
    fun `refresh should remove chapters missing from the server response`() =
        runBlocking {
            val original = chapter(id = 1)
            val updated = chapter(id = 2).copy(name = "updated")
            val cache = InMemoryChapterDao(listOf(original, chapter(id = 2)))
            val repository = NetRepositoryImpl(wanAndroidApi, cache)
            coEvery { wanAndroidApi.getChapters() } returns
                BaseHttpResult(data = listOf(updated), errorMsg = "", errorCode = 0)

            assertThat(repository.fetchAndCacheChapters().isSuccess, `is`(true))
            assertThat(repository.observeChapters().first(), `is`(listOf(updated)))
        }

    @Test
    fun `refresh with an empty list should clear the cache`() =
        runBlocking {
            val cache = InMemoryChapterDao(listOf(chapter(id = 1)))
            val repository = NetRepositoryImpl(wanAndroidApi, cache)
            coEvery { wanAndroidApi.getChapters() } returns
                BaseHttpResult(data = emptyList(), errorMsg = "", errorCode = 0)

            assertThat(repository.fetchAndCacheChapters().isSuccess, `is`(true))
            assertThat(repository.observeChapters().first(), `is`(emptyList()))
        }

    @Test
    fun `refresh with null data should preserve the cache`() =
        runBlocking {
            val original = listOf(chapter(id = 1))
            val cache = InMemoryChapterDao(original)
            val repository = NetRepositoryImpl(wanAndroidApi, cache)
            coEvery { wanAndroidApi.getChapters() } returns
                BaseHttpResult(data = null, errorMsg = "", errorCode = 0)

            assertThat(repository.fetchAndCacheChapters().isSuccess, `is`(true))
            assertThat(repository.observeChapters().first(), `is`(original))
        }

    // Exercises the DAO's real replacement method with in-memory insert/delete operations.
    // Room's transaction implementation is generated and checked by the app build.
    private class InMemoryChapterDao(initial: List<ChapterBean>) : ChapterDao {
        private val chapters = MutableStateFlow(initial)

        override suspend fun insertChapters(chapters: List<ChapterBean>) {
            this.chapters.value =
                (this.chapters.value.associateBy { it.id } + chapters.associateBy { it.id }).values.toList()
        }

        override fun observeAllChapters(): Flow<List<ChapterBean>> = chapters

        override suspend fun deleteAll() {
            chapters.value = emptyList()
        }
    }

    private fun chapter(id: Int) =
        ChapterBean(
            courseId = 13,
            id = id,
            name = "chapter-$id",
            order = id,
            parentChapterId = 0,
            userControlSetTop = false,
            visible = 1,
        )
}
