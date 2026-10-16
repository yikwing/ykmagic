package com.yikwing.ykquickdev.viewmodel

import androidx.compose.runtime.Immutable
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.network.ApiException
import com.yikwing.network.RequestState
import com.yikwing.ykquickdev.UserPreferences
import com.yikwing.ykquickdev.api.entity.Headers
import com.yikwing.ykquickdev.di.UserPreferencesStore
import com.yikwing.ykquickdev.repository.OtherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/**
 * HttpBin 测试视图模型
 *
 * 负责管理 HttpBin API 的测试功能：
 * - 请求头测试（headers）
 * - 用户偏好设置（userName）
 */
@Immutable
data class HttpBinUiState(
    val headers: RequestState<Headers> = RequestState.Loading,
    val userName: String = "",
)

@KoinViewModel
class HttpBinViewModel(
    private val otherRepository: OtherRepository,
    @param:UserPreferencesStore private val userPreferencesStore: DataStore<UserPreferences>,
) : ViewModel() {
    // Lazy loading per official guidance: the request starts when the UI subscribes and
    // re-runs only after WhileSubscribed's 5s timeout has stopped the upstream.
    private val headersFlow =
        flow<RequestState<Headers>> {
            emit(RequestState.Loading)
            emit(
                otherRepository.initHttpBinData().fold(
                    onSuccess = { RequestState.Success(it) },
                    onFailure = {
                        RequestState.Error(it as? ApiException ?: ApiException.createDefault(it.message, it))
                    },
                ),
            )
        }

    val uiState: StateFlow<HttpBinUiState> =
        combine(
            headersFlow,
            userPreferencesStore.data.map { it.name },
        ) { headers, userName ->
            HttpBinUiState(
                headers = headers,
                userName = userName,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HttpBinUiState(),
        )

    fun updateName(name: String) {
        viewModelScope.launch {
            userPreferencesStore.updateData { current ->
                current.copy(name = name)
            }
        }
    }
}
