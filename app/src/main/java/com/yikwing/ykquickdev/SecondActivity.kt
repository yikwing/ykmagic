package com.yikwing.ykquickdev

import android.util.Log
import com.yikwing.ykquickdev.databinding.SecondActivityBinding
import com.yk.ykproxy.BaseActivity

class SecondActivity : BaseActivity<SecondActivityBinding>(SecondActivityBinding::inflate) {
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
}