package com.yikwing.ykquickdev

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yikwing.datastore.DataStoreOwner
import com.yikwing.datastore.IDataStoreOwner
import kotlinx.coroutines.launch

class DataStoreViewModel : ViewModel(), IDataStoreOwner by DataStoreOwner("giao") {
    val count by intPreference(default = 1)

    fun initX() {
        viewModelScope.launch {
            count.set(123)
        }

        viewModelScope.launch {
            Log.d("====", "${count.get()}")
        }

    }
}