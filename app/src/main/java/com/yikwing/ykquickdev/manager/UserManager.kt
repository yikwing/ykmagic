package com.yikwing.ykquickdev.manager

import android.util.Log
import com.yikwing.extension.GlobalContextProvider
import com.yikwing.ykquickdev.UserInfo
import com.yikwing.ykquickdev.di.EntryPointDependencies
import dagger.hilt.android.EntryPointAccessors
import kotlinx.serialization.json.Json

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

    private val entryPoint: EntryPointDependencies by lazy {
        EntryPointAccessors.fromApplication(
            GlobalContextProvider.appContext,
            EntryPointDependencies::class.java,
        )
    }

    private val json: Json by lazy {
        entryPoint.getJson()
    }

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
