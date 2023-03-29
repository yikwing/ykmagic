package com.yk.ykconfig

import org.junit.Assert.*
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
        assertEquals("www.baidu.com", YkConfigManager.getConfig(SmallConfig::class.java).testString)
        assertEquals(999, YkConfigManager.getConfig(SmallConfig::class.java).testInt)
        require(3333.5555 == YkConfigManager.getConfig(SmallConfig::class.java).testDouble)
        assertEquals(true, YkConfigManager.getConfig(SmallConfig::class.java).testBoolean)
    }
}
