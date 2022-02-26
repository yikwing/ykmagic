package com.yikwing.ykquickdev

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.api.entity.Headers
import com.yikwing.api.provider.HttpBinProvider
import com.yk.yknetwork.RequestState
import kotlinx.coroutines.launch

class MyViewModel : ViewModel() {

    private val _headers = MutableLiveData<RequestState<Headers>>(RequestState.Loading)

    val headers = _headers


    fun initData() {

        viewModelScope.launch {
            _headers.value = try {
                RequestState.Success(HttpBinProvider.providerHeader().getHeaders().headers)
            } catch (exception: Exception) {
                RequestState.Error(exception)
            }
        }

    }

}