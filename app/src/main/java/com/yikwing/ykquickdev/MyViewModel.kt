package com.yikwing.ykquickdev

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.api.entity.Headers
import com.yikwing.ykquickdev.api.provider.ApiProvider
import com.yk.yknetwork.RequestState
import com.yk.yknetwork.transformApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MyViewModel : ViewModel() {

    init {
        initHttpBinData()
        initWanAndroidData()
    }

    private val _headers = MutableLiveData<RequestState<Headers>>(RequestState.Loading)

    val headers = _headers

    fun initHttpBinData() {
        viewModelScope.launch {
            _headers.value = try {
                RequestState.Success(ApiProvider.createHttpBinService().getOtherHeaders().headers)
            } catch (exception: Exception) {
                RequestState.Error(exception)
            }
        }
    }

    private val _wanAndroidList =
        MutableStateFlow<RequestState<List<ChapterBean>?>>(RequestState.Loading)

    val wanAndroidList = _wanAndroidList.asStateFlow()

    fun initWanAndroidData() {
        viewModelScope.launch {
            transformApi {
                ApiProvider.createWanAndroidService().getChapters()
            }.catch { exception ->
                _wanAndroidList.value = RequestState.Error(exception)
            }.collect { result ->
                _wanAndroidList.value = RequestState.Success(result)
            }
        }
    }

    fun removeItem(position: Int, list: MutableList<ChapterBean>) {
        val newData = mutableListOf<ChapterBean>()
        newData.addAll(list)
        newData.removeAt(position)

        _wanAndroidList.value = RequestState.Success(newData)
    }
}
