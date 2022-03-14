package com.yikwing.ykquickdev

import android.util.Log
import javax.inject.Inject


class HiltDemo @Inject constructor() {
    fun printEnv() {
        Log.i("HiltDemo", this.toString())
    }
}