package com.yikwing.ykquickdev

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yikwing.logger.Logger
import com.yikwing.ykquickdev.databinding.MainFragmentBinding
import com.yk.yknetwork.ApiException
import com.yk.yknetwork.collectState
import com.yk.ykpermission.PermissionX
import com.yk.ykproxy.BaseFragment
import kotlinx.coroutines.launch

class MainFragment : BaseFragment<MainFragmentBinding>(MainFragmentBinding::inflate) {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel by viewModels<MyViewModel>()

    override fun lazyInit() {

        Logger.d("===%s===", "lazyInit")

        viewModel.initData()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Logger.d("===%s===", "onActivityCreated")

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.headers.collectState {
                    onLoading = {
                        Log.d("headers", "加载中")
                    }

                    onSuccess = { data ->
                        binding.message.text = data.toString()
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


        binding.message.setOnClickListener {

            PermissionX.request(
                requireActivity(),
                Manifest.permission.CALL_PHONE,
                Manifest.permission.CAMERA,
            ) { allGranted, deniedList ->
                if (allGranted) {
                    Toast.makeText(context, "已全部同意", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "已拒绝 $deniedList", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

}