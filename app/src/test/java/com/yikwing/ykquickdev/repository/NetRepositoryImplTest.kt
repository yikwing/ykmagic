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
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue
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
            coVerify(exactly = 1) { chapterDao.insertChapters(chapters) }
        }

    @Test
    fun `fetchAndCacheChapters should return failure when request throws`() =
        runBlocking {
            coEvery { wanAndroidApi.getChapters() } throws IOException("network down")

            val result = repository.fetchAndCacheChapters()

            assertThat(result.exceptionOrNull(), instanceOf(ApiException::class.java))
            assertThat(result.exceptionOrNull()!!.message, `is`("network down"))
            coVerify(exactly = 0) { chapterDao.insertChapters(any()) }
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
