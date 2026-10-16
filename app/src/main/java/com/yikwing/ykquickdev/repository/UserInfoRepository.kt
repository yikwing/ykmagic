package com.yikwing.ykquickdev.repository

import android.content.Context
import com.yikwing.ykquickdev.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Singleton

interface UserInfoRepository {
    /** Single source of truth for the user name; in-memory only, resets to app_name on process death. */
    val name: StateFlow<String>

    fun updateName(name: String)
}

@Singleton
class UserInfoRepositoryImpl(
    context: Context,
) : UserInfoRepository {
    private val _name = MutableStateFlow(context.applicationContext.getString(R.string.app_name))
    override val name = _name.asStateFlow()

    override fun updateName(name: String) {
        _name.value = name
    }
}
