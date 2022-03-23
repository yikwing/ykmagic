package com.yikwing.ykquickdev

import com.yikwing.ykquickdev.databinding.FragmentHiltBinding
import com.yk.ykproxy.BaseFragment

class HiltFragment : BaseFragment<FragmentHiltBinding>(FragmentHiltBinding::inflate) {
    override fun lazyInit() {

    }
}