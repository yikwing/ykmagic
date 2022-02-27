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
        "test_string":"www.baidu.com",
        "test_other":{
            "test_double":3333.5555,
            "test_int":999,
            "test_boolean":false
        }
    }
    
""".trimIndent()

    @Before
    fun setUp() {
        YkConfigManager.setUp(jsonStr)
    }


    @Test
    fun addition_isCorrect() {

        print(YkConfigManager.getConfig(SmallConfig::class.java).testDouble)

        assertEquals("www.baidu.com", YkConfigManager.getConfig(SmallConfig::class.java).testString)
        assertEquals(999, YkConfigManager.getConfig(SmallConfig::class.java).testInt)
        assertEquals(false, YkConfigManager.getConfig(SmallConfig::class.java).testBoolean)
    }
}