package com.yikwing.ykquickdev

import com.yikwing.extension.plus.prettierJson
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertThat(2 + 2, `is`(4))
    }

    @Test
    fun test_obj() {
        val user = User("yikwing", 18)
        val pJson = prettierJson(user)
        println(pJson)
    }

    @Test
    fun test_list() {
        val list = listOf(1, 2, 3)
        val pJson = prettierJson(list)
        println(pJson)
    }

    @Test
    fun test_map() {
        val map = mapOf("name" to "yikwing", "age" to 18)
        val pJson = prettierJson(map)
        println(pJson)
    }
}

data class User(
    val name: String,
    val age: Int,
)
