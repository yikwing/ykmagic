package com.yikwing.ykquickdev

import android.Manifest
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yikwing.ykextension.backGroundRadiusColor
import com.yikwing.ykextension.dp
import com.yikwing.ykquickdev.databinding.ActivityMainBinding
import com.yikwing.ykquickdev.db.UserDatabase
import com.yk.ykpermission.PermissionX
import com.yk.ykconfig.YkQuickManager
import com.yk.yknetwork.ApiException
import com.yk.yknetwork.collectState
import com.yk.ykproxy.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {


    private val viewModel by viewModels<MyViewModel>()

    private val userDao by lazy {
        UserDatabase.getInstance(YkQuickManager.getApplication()).getUserDao()
    }

    private val chapterDao by lazy {
        UserDatabase.getInstance(YkQuickManager.getApplication()).getChapterDao()
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
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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

                        binding.tvMainTitle.text = data.toString()
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


        binding.tvMainTitle.setOnClickListener {

            it.backGroundRadiusColor(Color.parseColor("#0D60B4"), 16f.dp)

            PermissionX.request(
                this,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.CAMERA,
            ) { allGranted, deniedList ->
                if (allGranted) {
                    Toast.makeText(this, "已全部同意", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "已拒绝 $deniedList", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

}