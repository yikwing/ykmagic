package com.yikwing.ykquickdev.viewmodel

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.network.RequestState
import com.yikwing.ykquickdev.UserPreferences
import com.yikwing.ykquickdev.api.entity.Headers
import com.yikwing.ykquickdev.repository.OtherRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/**
 * HttpBin 测试视图模型
 *
 * 负责管理 HttpBin API 的测试功能：
 * - 请求头测试（headers）
 * - 用户偏好设置（userName）
 */
data class HttpBinUiState(
    val repo: RequestState<Headers> = RequestState.Loading,
)

@KoinViewModel
class HttpBinViewModel
    @Inject
    constructor(
        private val otherRepository: OtherRepository,
        private val userPreferencesStore: DataStore<UserPreferences>,
    ) : ViewModel() {
        private val _headers = MutableStateFlow<HttpBinUiState>(HttpBinUiState())
        val headers = _headers.asStateFlow()

        fun initHttpBinData() {
            viewModelScope.launch {
                otherRepository.initHttpBinData().collect { result ->
                    _headers.update {
                        it.copy(repo = result)
                    }
                }
            }
        }

        // 读取
        val userName: StateFlow<String> =
            userPreferencesStore.data
                .map { it.name }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

        // 写入
        fun updateName(name: String) {
            viewModelScope.launch {
                userPreferencesStore.updateData { current ->
                    current.copy(name = name)
                }
            }
        }
    }
