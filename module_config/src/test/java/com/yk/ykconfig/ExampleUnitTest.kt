package com.yk.ykconfig

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    val jsonStr = """
    
    {
        "test_string":"www.baidu.com",
        "test_other":{
            "test_double":3333.5555,
            "test_int":999,
            "test_boolean":true
        }
    }
    
    """.trimIndent()

    @Before
    fun setUp() {
        YkConfigManager.setUp(jsonStr)
    }

    @Test
    fun addition_isCorrect() {
        assertThat(
            YkConfigManager.getConfig(SmallConfig::class.java).testString,
            `is`("www.baidu.com"),
        )
        assertThat(YkConfigManager.getConfig(SmallConfig::class.java).testInt, `is`(999))
        assertThat(YkConfigManager.getConfig(SmallConfig::class.java).testDouble, `is`(3333.5555))
        assertThat(YkConfigManager.getConfig(SmallConfig::class.java).testBoolean, `is`(true))
    }
}
