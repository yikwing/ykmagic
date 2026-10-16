package com.yikwing.ykquickdev.viewmodel

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.network.ApiException
import com.yikwing.network.RequestState
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.repository.NetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

/**
 * WanAndroid 数据视图模型
 *
 * 负责管理 WanAndroid 章节列表（chapters）：Room 缓存为 SSOT，网络拉取只负责写入缓存
 */
@Immutable
data class WanAndroidUiState(
    val chapters: RequestState<List<ChapterBean>> = RequestState.Loading,
)

@KoinViewModel
class WanAndroidViewModel(
    private val netRepository: NetRepository,
    @InjectedParam private val name: String,
) : ViewModel() {
    // Lazy loading like HttpBinViewModel: the fetch starts when the UI subscribes, not at construction.
    // null = fetch still in progress; emitted first so combine can show cached chapters meanwhile.
    private val chaptersFetchResult: Flow<Result<Unit>?> =
        flow<Result<Unit>?> {
            Log.d("==== initWanAndroidData", "assistedInject $name")
            emit(netRepository.fetchAndCacheChapters())
        }.onStart { emit(null) }

    val uiState: StateFlow<WanAndroidUiState> =
        combine(
            netRepository.observeChapters(),
            chaptersFetchResult,
        ) { cachedChapters, fetchResult ->
            WanAndroidUiState(
                chapters = chaptersState(cachedChapters, fetchResult),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WanAndroidUiState(),
        )
}

/**
 * Derives the chapters UI state from the Room cache (SSOT) and the latest fetch result.
 *
 * Cached data always wins (offline-first: a failed refresh keeps showing the cache).
 * An empty cache is only reported as "数据为空" after the fetch has succeeded, so the
 * UI shows Loading instead of flashing an error while the first fetch is in flight.
 *
 * @param fetchResult null while the fetch is in progress
 */
internal fun chaptersState(
    cachedChapters: List<ChapterBean>,
    fetchResult: Result<Unit>?,
): RequestState<List<ChapterBean>> {
    val fetchError = fetchResult?.exceptionOrNull()
    return when {
        cachedChapters.isNotEmpty() -> {
            RequestState.Success(cachedChapters)
        }

        fetchResult == null -> {
            RequestState.Loading
        }

        fetchError != null -> {
            RequestState.Error(
                fetchError as? ApiException ?: ApiException.createDefault(
                    fetchError.message,
                    fetchError,
                ),
            )
        }

        else -> {
            RequestState.Error(ApiException.createDefault("数据为空"))
        }
    }
}
