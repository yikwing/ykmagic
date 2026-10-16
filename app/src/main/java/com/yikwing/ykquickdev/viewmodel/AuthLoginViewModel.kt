package com.yikwing.ykquickdev.viewmodel

import androidx.lifecycle.ViewModel
import com.yikwing.ykquickdev.repository.UserInfoRepository
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AuthLoginViewModel(
    private val userInfoRepository: UserInfoRepository,
) : ViewModel() {
    val name = userInfoRepository.name

    fun changeName(s: String) {
        userInfoRepository.updateName(s)
    }
}
