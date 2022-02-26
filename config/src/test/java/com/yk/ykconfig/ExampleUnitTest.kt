package com.yk.ykconfig

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    val jsonStr = """
    
    {
        "base_url":"www.baidu.com",
        "wxInfo":{
            "wxid":"123456",
            "wxtype":999,
            "wxIsOpen":false
        }
    }
    
""".trimIndent()

    @Before
    fun setUp() {
        YkConfigManager.setUp(jsonStr)
    }


    @Test
    fun addition_isCorrect() {
        assertEquals("www.baidu.com", YkConfigManager.getConfig(TestConfig::class.java).baseUrl)
    }
}