package com.yikwing.ykquickdev.viewmodel

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.network.RequestState
import com.yikwing.ykquickdev.UserPreferences
import com.yikwing.ykquickdev.api.entity.Headers
import com.yikwing.ykquickdev.repository.OtherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import javax.inject.Inject

data class HttpBinUiState(
    val repo: RequestState<Headers> = RequestState.Loading,
)

@KoinViewModel
class OtherViewModel
    @Inject
    constructor(
        private val otherRepository: OtherRepository,
        private val userPreferencesStore: DataStore<UserPreferences>,
    ) : ViewModel() {
        val headers: StateFlow<HttpBinUiState>
            field = MutableStateFlow<HttpBinUiState>(HttpBinUiState())

        fun initHttpBinData() {
            viewModelScope.launch {
                otherRepository.initHttpBinData().collect { result ->
                    headers.update {
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
