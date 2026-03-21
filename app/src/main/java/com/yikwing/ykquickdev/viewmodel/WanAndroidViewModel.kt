package com.yikwing.ykquickdev.viewmodel

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.network.RequestState
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.api.entity.Headers
import com.yikwing.ykquickdev.repository.NetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

/**
 * WanAndroid 数据视图模型
 *
 * 负责管理 WanAndroid API 的数据加载和状态：
 * - 章节列表数据（chapters）
 * - HttpBin 请求头测试（headers）
 */
@Immutable
data class WanAndroidUiState(
    val headers: RequestState<Headers> = RequestState.Loading,
    val wanAndroidList: RequestState<List<ChapterBean>?> = RequestState.Loading,
    val chapters: RequestState<List<ChapterBean>> = RequestState.Loading,
)

@KoinViewModel
class WanAndroidViewModel(
    private val netRepository: NetRepository,
    @InjectedParam private val name: String,
) : ViewModel() {
    private val headersFlow = MutableStateFlow<RequestState<Headers>>(RequestState.Loading)
    private val wanAndroidListFlow =
        MutableStateFlow<RequestState<List<ChapterBean>?>>(RequestState.Loading)

    val uiState: StateFlow<WanAndroidUiState> =
        combine(
            headersFlow,
            wanAndroidListFlow,
            netRepository.observeChapters(),
        ) { headers, wanAndroidList, chapters ->
            WanAndroidUiState(
                headers = headers,
                wanAndroidList = wanAndroidList,
                chapters = chapters,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WanAndroidUiState(),
        )

    private fun initHttpBinData() {
        viewModelScope.launch {
            headersFlow.value = netRepository.initHttpBinData()
        }
    }

    private fun initWanAndroidData() {
        Log.d("==== initWanAndroidData", "assistedInject $name")

        viewModelScope.launch {
            netRepository.initWanAndroidData().collect { result ->
                wanAndroidListFlow.value = result
            }
        }

        viewModelScope.launch {
            netRepository.fetchAndCacheChapters()
        }
    }

    fun removeItem(
        position: Int,
        list: List<ChapterBean>,
    ) {
        if (position in list.indices) {
            val newData = list.toMutableList()
            newData.removeAt(position)
            wanAndroidListFlow.value = RequestState.Success(newData)
        }
    }

    // 初始化代码应该在最后面
    init {
        initHttpBinData()
        initWanAndroidData()
    }
}
