package com.yikwing.ykquickdev

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.network.RStateLiveData
import com.yikwing.network.RequestState
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.api.entity.Headers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import javax.inject.Inject

@KoinViewModel
class MyViewModel
    @Inject
    constructor(
        private val repository: Repository,
        @InjectedParam val name: String,
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

            viewModelScope.launch {
                val cc = repository.initWanAndroidData2()
                cc.fold(onSuccess = { _data ->
                    Log.d("==== initWanAndroidData", "$name ${_data?.firstOrNull()?.name}")
                }, onFailure = { _err ->
                    Log.e("==== initWanAndroidData", "$name ${_err.message}")
                })
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
