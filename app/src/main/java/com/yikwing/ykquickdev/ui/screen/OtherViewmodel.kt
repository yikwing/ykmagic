package com.yikwing.ykquickdev.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.ykquickdev.api.entity.Headers
import com.yk.yknetwork.RequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OtherViewModel @Inject constructor(
    private val otherRepository: OtherRepository
) : ViewModel() {
    private val _headers = MutableStateFlow<RequestState<Headers>>(RequestState.Loading)

    val headers: StateFlow<RequestState<Headers>>
        get() = _headers.asStateFlow()

    private fun initHttpBinData() {
        viewModelScope.launch {
            _headers.value = otherRepository.initHttpBinData()
        }
    }

    init {
        initHttpBinData()
    }
}
