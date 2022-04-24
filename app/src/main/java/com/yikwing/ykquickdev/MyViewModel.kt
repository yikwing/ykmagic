package com.yikwing.ykquickdev

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.api.provider.ApiProvider
import com.yk.yknetwork.RequestState
import com.yk.yknetwork.StatefulFlow
import com.yk.yknetwork.transformApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
//                RequestState.Success(HttpBinProvider.providerHeader(        ).getHeaders().headers)
//            } catch (exception: Exception) {
//                RequestState.Error(exception)
//            }
//        }
//
//    }

    private val _headers = MutableStateFlow<RequestState<List<ChapterBean>?>>(RequestState.Loading)

    val headers = _headers.asStateFlow()

    fun initData() {
        viewModelScope.launch {
            transformApi {
                ApiProvider.createWanAndroidService().getChapters()
            }
                .catch { exception ->
                    _headers.value = RequestState.Error(exception)
                }
                .collect { result ->
                    _headers.value = RequestState.Success(result)
                }
        }
    }

    fun removeItem(position: Int, list: MutableList<ChapterBean>) {

        val newData = mutableListOf<ChapterBean>()
        newData.addAll(list)
        newData.removeAt(position)

        _headers.value = RequestState.Success(newData)
    }
}
