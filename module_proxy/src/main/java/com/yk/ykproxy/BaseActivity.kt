package com.yk.ykproxy

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding>(val block: (LayoutInflater) -> VB) :
    AppCompatActivity() {

    protected val binding: VB by lazy(LazyThreadSafetyMode.NONE) {
        block(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
    }

    override fun onStart() {
        super.onStart()

        initData()
        initListener()
    }

    open fun initView() {}
    open fun initData() {}
    open fun initListener() {}
}
