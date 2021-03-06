package com.yikwing.ykquickdev

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.yikwing.ykquickdev.databinding.MainActivityBinding
import com.yk.ykproxy.BaseActivity

class SecondActivity : BaseActivity<MainActivityBinding>(MainActivityBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HiltFragment.newInstance())
                .commitNow()
        }
    }

    override fun initView() {
        super.initView()

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

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("result", "result from A")
        })
        super.onBackPressed()
    }
}