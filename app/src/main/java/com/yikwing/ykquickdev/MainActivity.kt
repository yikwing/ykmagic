package com.yikwing.ykquickdev

import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yikwing.api.entity.Headers
import com.yikwing.db.User
import com.yikwing.db.UserDatabase
import com.yikwing.ykquickdev.databinding.ActivityMainBinding
import com.yk.ykconfig.YkQuickManager
import com.yk.yknetwork.doError
import com.yk.yknetwork.doSuccess
import com.yk.yknetwork.observeState
import com.yk.ykproxy.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val viewModel by viewModels<MyViewModel>()

    private val userDao by lazy {
        UserDatabase.getInstance(this).getUserDao()
    }


    override fun initView() {
        super.initView()

        viewModel.initData()


//        viewModel.headers.observeState(this) {
//
//            onLoading = {
//                Log.d("headers", "加载中")
//            }
//
//            onSuccess = { data: Headers ->
//                binding.tvTitle.text = data.userAgent + "\n" + data.traceId
//            }
//
//            onError = { e ->
//                Log.e("headers", e.message ?: "Not Error")
//            }
//
//        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.headers.collect {
                    it.doSuccess { data: Headers ->
                        binding.tvTitle.text = data.userAgent + "\n" + data.traceId
                    }

                    it.doError { e ->
                        Log.e("headers", e?.message ?: "Not Error")
                    }
                }
            }
        }


        binding.tvTitle.setOnClickListener {
            lifecycleScope.launch() {
                withContext(Dispatchers.IO) {
                    userDao.insertUser(User(firstName = "z", lastName = "s", age = 23))
                }
            }
        }
    }

}