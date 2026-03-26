package com.yikwing.ykquickdev.repository

import android.content.Context
import com.yikwing.ykquickdev.R
import org.koin.core.annotation.Single

interface UserInfoRepository {
    val name: String
}

@Single
class UserInfoRepositoryImpl(
    private val context: Context,
) : UserInfoRepository {
    override val name: String
        get() = context.applicationContext.getString(R.string.app_name)
}
