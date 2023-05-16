package com.yikwing.ykquickdev

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch

class DataStoreViewModel(
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DataStoreViewModel(DataStoreRepository)
            }
        }
    }
}
