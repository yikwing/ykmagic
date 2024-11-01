package com.yikwing.proxy

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding>(
    val block: (LayoutInflater) -> VB,
) : AppCompatActivity() {
    protected val binding: VB by lazy(LazyThreadSafetyMode.NONE) {
        block(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        initView(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        initData()
        initListener()
    }

    open fun initView(savedInstanceState: Bundle?) {}

    open fun initData() {}

    open fun initListener() {}
}
