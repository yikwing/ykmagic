package com.yikwing.ykquickdev

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import com.yikwing.proxy.BaseActivity
import com.yikwing.ykquickdev.databinding.MainActivityBinding
import com.yikwing.ykquickdev.ui.fragment.HiltScreenFragment

class SecondActivity : BaseActivity<MainActivityBinding>(MainActivityBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    setResult(
                        Activity.RESULT_OK,
                        Intent().apply {
                            putExtra("result", "result from A")
                        },
                    )

                    finish()
                }
            }

        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        if (savedInstanceState == null) {
            with(supportFragmentManager.beginTransaction()) {
                replace(R.id.container, HiltScreenFragment())
                commit()
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        val data = intent.data
        val action = intent.action
        val scheme = intent.scheme

        val categories = intent.categories

        Log.e("TAG", "data===========$data")
        Log.e("TAG", "action===========$action")
        Log.e("TAG", "categories===========$categories")
        Log.e("TAG", "DataString===========" + intent.dataString)
        Log.e("TAG", "==============================")
        Log.e("TAG", "scheme===========$scheme")
        Log.e("TAG", "id ===========" + data?.queryParameterNames)
        Log.e("TAG", "query ===========" + data?.query)
        Log.e("TAG", "host===========" + data?.host)
        Log.e("TAG", "path===========" + data?.path)
        Log.e("TAG", "port===========" + data?.port)
    }
}
