package com.yikwing.ykquickdev

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.network.RequestState
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.api.entity.Headers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyViewModel
    @Inject
    constructor(
        private val repository: Repository,
    ) : ViewModel() {
        private val _headers = MutableLiveData<RequestState<Headers>>(RequestState.Loading)

        val headers: LiveData<RequestState<Headers>>
            get() = _headers

        private fun initHttpBinData() {
            viewModelScope.launch {
                _headers.value = repository.initHttpBinData()
            }
        }

        private val _wanAndroidList =
            MutableStateFlow<RequestState<List<ChapterBean>?>>(RequestState.Loading)

        val wanAndroidList = _wanAndroidList.asStateFlow()

        private fun initWanAndroidData() {
            viewModelScope.launch {
                repository
                    .initWanAndroidData()
                    .collect { result ->
                        _wanAndroidList.value = result
                    }
            }
        }

        fun removeItem(
            position: Int,
            list: List<ChapterBean>,
        ) {
            if (position in list.indices) {
                val newData = list.toMutableList()
                newData.removeAt(position)
                _wanAndroidList.value = RequestState.Success(newData)
            }
        }

        // 初始化代码应该在最后面
        init {
            initHttpBinData()
            initWanAndroidData()
        }
    }
