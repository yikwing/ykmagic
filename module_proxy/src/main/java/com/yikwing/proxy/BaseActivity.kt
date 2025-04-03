package com.yikwing.proxy

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

inline fun AppCompatActivity.launchWhenStarted(crossinline block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            block.invoke(this)
        }
    }
}

inline fun AppCompatActivity.launchWhenResumed(crossinline block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            block.invoke(this)
        }
    }
}
