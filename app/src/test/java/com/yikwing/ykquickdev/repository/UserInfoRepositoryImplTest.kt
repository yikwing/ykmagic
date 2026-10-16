package com.yikwing.ykquickdev.repository

import android.content.Context
import com.yikwing.ykquickdev.R
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * UserInfoRepositoryImpl 单元测试
 *
 * 覆盖 name 的初始值与 updateName 写回数据源。
 */
class UserInfoRepositoryImplTest {
    private val context =
        mockk<Context> {
            every { applicationContext } returns this
            every { getString(R.string.app_name) } returns "YkQuickDev"
        }

    // ==================== name ====================

    @Test
    fun `name should default to app name`() {
        val repository = UserInfoRepositoryImpl(context)

        assertThat(repository.name.value, `is`("YkQuickDev"))
    }

    @Test
    fun `updateName should update name`() {
        val repository = UserInfoRepositoryImpl(context)

        repository.updateName("changed")

        assertThat(repository.name.value, `is`("changed"))
    }
}
