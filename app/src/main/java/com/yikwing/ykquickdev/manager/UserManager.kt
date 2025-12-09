package com.yikwing.ykquickdev.manager

import android.util.Log
import com.yikwing.ykquickdev.UserInfo
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject

/**
 * <pre>
 *     author: yikwing
 *
 *        _ _              _
 *       (_) |            (_)
 *  _   _ _| | ____      ___ _ __   __ _
 * | | | | | |/ /\ \ /\ / / | '_ \ / _` |
 * | |_| | |   <  \ V  V /| | | | | (_| |
 *  \__, |_|_|\_\  \_/\_/ |_|_| |_|\__, |
 *   __/ |                          __/ |
 *  |___/                          |___/
 *
 *     email : 49999@live.com
 *     time  : 2025-11-29 22:08
 *     desc  :
 * </pre>
 */
object UserManager {
    private const val TAG = "UserManager"

    private val json: Json by inject(Json::class.java)

    fun printUserIsMan() {
        val jsonWithBooleanId =
            """
            {
              "age": 12,
              "name": "Product D",
              "isMan": 100
            }
            """.trimIndent()

        val u = json.decodeFromString<UserInfo>(jsonWithBooleanId)

        Log.i(TAG, u.isMan.toString())
    }
}
