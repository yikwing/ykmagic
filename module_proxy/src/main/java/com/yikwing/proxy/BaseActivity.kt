package com.yikwing.proxy

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
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

    private val backPressedCallback =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                onHandleBack()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        initView(savedInstanceState)
        initListener()
        initData()
    }

    protected fun setBackInterceptEnable(enable: Boolean) {
        backPressedCallback.isEnabled = enable
    }

    open fun onHandleBack() {
        finish()
    }

    open fun initView(savedInstanceState: Bundle?) {}

    open fun initData() {}

    open fun initListener() {}
}

/**
 * 在 STARTED 状态时重复执行，适用于 Flow 收集
 * - 每次进入 STARTED 状态都会重新执行 block
 * - 当生命周期低于 STARTED（进入 STOPPED）时取消收集
 * - Activity 销毁时彻底取消
 */
inline fun AppCompatActivity.repeatOnStarted(crossinline block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            block.invoke(this)
        }
    }
}

/**
 * 在 RESUMED 状态时重复执行，适用于 Flow 收集
 * - 每次进入 RESUMED 状态都会重新执行 block
 * - 当生命周期低于 RESUMED（进入 PAUSED）时取消收集
 * - Activity 销毁时彻底取消
 *
 * 注意：极少使用，仅适用于相机预览、视频播放等需要严格跟随前台状态的场景
 * 大多数情况请使用 [repeatOnStarted]
 */
inline fun AppCompatActivity.repeatOnResumed(crossinline block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            block.invoke(this)
        }
    }
}
