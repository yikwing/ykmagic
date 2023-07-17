package com.yikwing.ykquickdev.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.ykquickdev.api.entity.Headers
import com.yikwing.network.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HttpBinUiState(
    val repo: RequestState<Headers> = RequestState.Loading
)

@HiltViewModel
class OtherViewModel @Inject constructor(
    private val otherRepository: OtherRepository
) : ViewModel() {
    private val _headers = MutableStateFlow<HttpBinUiState>(HttpBinUiState())
    val headers: StateFlow<HttpBinUiState> = _headers.asStateFlow()

    fun initHttpBinData() {
        viewModelScope.launch {
            _headers.update {
                val repo = otherRepository.initHttpBinData()
                it.copy(repo = repo)
            }
        }
    }
}
