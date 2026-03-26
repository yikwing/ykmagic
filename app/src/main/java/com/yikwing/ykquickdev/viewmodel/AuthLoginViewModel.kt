package com.yikwing.ykquickdev.viewmodel

import androidx.lifecycle.ViewModel
import com.yikwing.ykquickdev.repository.UserInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AuthLoginViewModel(
    private val userInfoRepository: UserInfoRepository,
) : ViewModel() {
    private val _name = MutableStateFlow(userInfoRepository.name)
    val name = _name.asStateFlow()

    fun changeName(s: String) {
        _name.update { s }
    }
}
