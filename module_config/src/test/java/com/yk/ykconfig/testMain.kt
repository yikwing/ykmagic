package com.yk.ykconfig

import com.squareup.moshi.Moshi

fun main() {

    val moshi = Moshi.Builder().add(SexAdapter()).build()

    val person = moshi.adapter(Person::class.java).fromJson(
        """
            {
                "name":"zs",
                "age":23,
                "sex":0
            }
        """.trimIndent()
    )

    println(person)
    println("==================")

    val personToJson = Person(
        "ls", 24, Sex.MALE
    )

    val jsonStr = moshi.adapter(Person::class.java).toJson(
        personToJson
    )

    println(jsonStr)


}