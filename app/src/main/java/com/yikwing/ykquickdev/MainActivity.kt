package com.yikwing.ykquickdev

import android.util.Log
import androidx.activity.viewModels
import com.yikwing.api.entity.Headers
import com.yikwing.ykquickdev.databinding.ActivityMainBinding
import com.yk.yknetwork.observeState
import com.yk.ykproxy.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val viewModel by viewModels<MyViewModel>()


    override fun initView() {
        super.initView()

        viewModel.initData()


        viewModel.headers.observeState(this) {

            onLoading = {
                Log.d("headers", "加载中")
            }

            onSuccess = { data: Headers ->
                binding.tvTitle.text = data.userAgent
            }

            onError = { e ->
                Log.e("headers", e.message ?: "Not Error")
            }

        }

    }

}