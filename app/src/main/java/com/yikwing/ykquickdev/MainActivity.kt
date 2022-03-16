package com.yikwing.ykquickdev

import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yikwing.ykquickdev.api.entity.Headers
import com.yikwing.ykquickdev.databinding.ActivityMainBinding
import com.yikwing.ykquickdev.db.User
import com.yikwing.ykquickdev.db.UserDatabase
import com.yk.yknetwork.ApiException
import com.yk.yknetwork.collectState
import com.yk.ykproxy.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {


    private val viewModel by viewModels<MyViewModel>()

    private val userDao by lazy {
        UserDatabase.getInstance(this).getUserDao()
    }

    private val chapterDao by lazy {
        UserDatabase.getInstance(this).getChapterDao()
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
                viewModel.headers.collectState {
                    onLoading = {
                        Log.d("headers", "加载中")
                    }

                    onSuccess = { data ->
                        data?.let {

                            lifecycleScope.launch {
                                withContext(Dispatchers.IO) {
                                    chapterDao.insertUser(it)
                                }
                            }

                        }

                        binding.tvTitle.text = data.toString()
                    }

                    onError = { e ->

                        when (e) {
                            is ApiException -> Log.e("headers", "${e.code} === ${e.message}")
                            else -> Log.e("headers", e.message ?: "Not Error")
                        }

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