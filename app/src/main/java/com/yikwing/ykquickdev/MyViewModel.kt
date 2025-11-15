package com.yikwing.ykquickdev

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.network.RStateLiveData
import com.yikwing.network.RequestState
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.api.entity.Headers
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = MyViewModel.Factory::class)
class MyViewModel
    @AssistedInject
    constructor(
        private val repository: Repository,
        @Assisted val name: String,
    ) : ViewModel() {
        private val _headers = MutableLiveData<RequestState<Headers>>(RequestState.Loading)

        val headers: RStateLiveData<Headers>
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
            Log.d("==== initWanAndroidData", "assistedInject $name")

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

        @AssistedFactory
        fun interface Factory {
            // create 方法的参数必须和 @Assisted 标记的参数完全匹配
            fun create(name: String): MyViewModel
        }
    }
