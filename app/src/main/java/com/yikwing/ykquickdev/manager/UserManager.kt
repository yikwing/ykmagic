package com.yikwing.ykquickdev.manager

import android.util.Log
import com.yikwing.ykquickdev.UserInfo
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Singleton

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
@Singleton
class UserManager(
    private val json: Json,
) {
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

    private companion object {
        const val TAG = "UserManager"
    }
}
