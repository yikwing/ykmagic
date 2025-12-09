package com.yikwing.ykquickdev

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import javax.inject.Inject

@KoinViewModel
class DataStoreViewModel
    @Inject
    constructor(
        private val repository: DataStoreRepository,
    ) : ViewModel() {
        fun initX() {
            viewModelScope.launch {
                repository.count.set(123)
            }

            viewModelScope.launch {
                Log.d("====", "${repository.count.get()}")
            }
        }

//    companion object {
//        val Factory: ViewModelProvider.Factory = viewModelFactory {
//            initializer {
//                DataStoreViewModel(DataStoreRepository)
//            }
//        }
//    }
    }
