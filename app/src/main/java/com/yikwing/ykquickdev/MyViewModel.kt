package com.yikwing.ykquickdev

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.api.entity.Headers
import com.yikwing.api.provider.HttpBinProvider
import com.yk.yknetwork.RequestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MyViewModel : ViewModel() {

//    private val _headers = MutableLiveData<RequestState<Headers>>(RequestState.Loading)
//
//    val headers = _headers
//
//
//    fun initData() {
//
//        viewModelScope.launch {
//            _headers.value = try {
//                RequestState.Success(HttpBinProvider.providerHeader().getHeaders().headers)
//            } catch (exception: Exception) {
//                RequestState.Error(exception)
//            }
//        }
//
//    }

    private val _headers = MutableStateFlow<RequestState<Headers>>(RequestState.Loading)

    val headers: StateFlow<RequestState<Headers>> = _headers

    fun initData() {
        viewModelScope.launch {
            getIpInfo()
                .onStart {
                    Log.d("onStart", "onStart表示最开始调用方法之前执行的操作，这里是展示一个 loading ui；")
                }
                .catch { exception ->
                    _headers.value = RequestState.Error(exception)
                }.onCompletion {
                    Log.d("onCompletion", "onCompletion表示所有执行完成，不管有没有异常都会执行这个回调。")
                }
                .collect { result ->
                    _headers.value = RequestState.Success(result.headers)
                }
        }
    }

    fun getIpInfo() = flow {
        emit(HttpBinProvider.providerHeader().getHeaders())
    }.flowOn(Dispatchers.IO)

}