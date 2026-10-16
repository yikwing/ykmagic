package com.yikwing.ykquickdev.viewmodel

import com.yikwing.network.ApiException
import com.yikwing.network.RequestState
import com.yikwing.ykquickdev.api.entity.ChapterBean
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.io.IOException

/**
 * WanAndroidViewModel 纯逻辑单元测试
 *
 * viewModelScope 依赖 Dispatchers.Main,按项目约定不用 setMain,
 * 这里只覆盖抽出的纯函数 chaptersState(缓存 + 拉取结果 → UI 状态)。
 */
class WanAndroidViewModelTest {
    private val cached = listOf(chapter(id = 1))

    // ==================== chaptersState ====================

    @Test
    fun `chaptersState should be loading when cache is empty and fetch in progress`() {
        assertThat(chaptersState(emptyList(), fetchResult = null), `is`(RequestState.Loading))
    }

    @Test
    fun `chaptersState should expose fetch error when cache is empty and fetch failed`() {
        val error = ApiException.createDefault("network down")

        val state = chaptersState(emptyList(), Result.failure(error))

        assertThat((state as RequestState.Error).throwable, sameInstance(error))
    }

    @Test
    fun `chaptersState should wrap non ApiException fetch error`() {
        val state = chaptersState(emptyList(), Result.failure(IOException("io")))

        assertThat((state as RequestState.Error).throwable.message, `is`("io"))
    }

    @Test
    fun `chaptersState should be empty error when fetch succeeded with no data`() {
        val state = chaptersState(emptyList(), Result.success(Unit))

        assertThat(state, instanceOf(RequestState.Error::class.java))
        assertThat((state as RequestState.Error).throwable.message, `is`("数据为空"))
    }

    @Test
    fun `chaptersState should show cache regardless of fetch result`() {
        val expected = RequestState.Success(cached)

        assertThat(chaptersState(cached, fetchResult = null), `is`(expected))
        assertThat(chaptersState(cached, Result.failure(IOException("io"))), `is`(expected))
        assertThat(chaptersState(cached, Result.success(Unit)), `is`(expected))
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
