package com.yikwing.config

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test

class YkConfigManagerTest {
    private val jsonStr =
        """
        {
            "base_url": "https://www.example.com"
        }
        """.trimIndent()

    @Before
    fun setUp() {
        YkConfigManager.setUp(jsonStr)
    }

    @Test
    fun `setUp should parse config correctly`() {
        assertThat(YkConfigManager.config.baseUrl, `is`("https://www.example.com"))
    }

    @Test
    fun `isInitialized should return true after setUp`() {
        assertThat(YkConfigManager.isInitialized, `is`(true))
    }

    @Test
    fun `ignoreUnknownKeys should not fail on extra fields`() {
        val jsonWithExtra =
            """
            {
                "base_url": "https://www.example.com",
                "unknown_field": "should be ignored"
            }
            """.trimIndent()
        YkConfigManager.setUp(jsonWithExtra)
        assertThat(YkConfigManager.config.baseUrl, `is`("https://www.example.com"))
    }
}