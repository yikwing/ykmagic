package com.yikwing.proxy

import androidx.fragment.app.Fragment

abstract class LazyFragment : Fragment() {
    private var isLoaded = false

    override fun onResume() {
        super.onResume()
        if (!isLoaded && !isHidden) {
            lazyInit()
            isLoaded = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isLoaded = false
    }

    protected open fun lazyInit() {}
}
