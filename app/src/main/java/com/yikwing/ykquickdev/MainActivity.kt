package com.yikwing.ykquickdev

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yikwing.api.entity.Headers
import com.yikwing.api.provider.HttpBinProvider
import com.yk.yknetwork.observeState

class MainActivity : AppCompatActivity() {


    private val viewModel by viewModels<MyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.initData()


        viewModel.headers.observeState(this) {

            onLoading = {
                Log.d("headers", "加载中")
            }

            onSuccess = { data: Headers ->
                Log.d("headers", data.userAgent)
            }

            onError = { e ->
                Log.e("headers", e.message ?: "Not Error")
            }

        }


    }
}