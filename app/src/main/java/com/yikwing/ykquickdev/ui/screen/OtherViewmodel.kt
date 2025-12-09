package com.yikwing.ykquickdev.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.network.RequestState
import com.yikwing.ykquickdev.api.entity.Headers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    ) : ViewModel() {
        private val _headers = MutableStateFlow<HttpBinUiState>(HttpBinUiState())
        val headers: StateFlow<HttpBinUiState> = _headers.asStateFlow()

        fun initHttpBinData() {
            viewModelScope.launch {
                otherRepository.initHttpBinData().collect { result ->
                    _headers.update {
                        it.copy(repo = result)
                    }
                }
            }
        }
    }
