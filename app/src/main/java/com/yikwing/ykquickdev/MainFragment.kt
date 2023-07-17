package com.yikwing.ykquickdev

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yikwing.logger.Logger
import com.yikwing.ykextension.unSafeLazy
import com.yikwing.ykquickdev.databinding.MainFragmentBinding
import com.yikwing.network.ApiException
import com.yikwing.network.collectState
import com.yikwing.network.observeState
import com.yikwing.permission.PermissionX
import com.yikwing.proxy.BaseFragment
import kotlinx.coroutines.launch

class MainFragment :
    BaseFragment<MainFragmentBinding>(MainFragmentBinding::inflate),
    CustomListAdapterCallBack {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MyViewModel by viewModels()

    var forActivityResultLauncher =
        registerForActivityResult(object : ActivityResultContract<String, String>() {
            override fun createIntent(context: Context, input: String): Intent {
                return Intent(Intent.ACTION_VIEW, Uri.parse("yikwing://yk:9001/props?$input"))
            }

            override fun parseResult(resultCode: Int, intent: Intent?): String {
                return if (resultCode == Activity.RESULT_OK) {
                    intent?.getStringExtra("result") ?: "empty"
                } else {
                    ""
                }
            }
        }) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

    private val adapter by unSafeLazy {
        CustomListAdapter(this)
    }

    override fun lazyInit() {
        Logger.d("===%s===", "lazyInit")

//        viewModel.apply {
//            initHttpBinData()
//            initWanAndroidData()
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.d("===%s===", "onActivityCreated")

        binding.wxRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.wxRecycler.adapter = adapter

//        viewLifecycleOwner.lifecycleScope.launch {
//            // 可重启生命周期感知型协程
//            // 网络请求不需要重复请求
//            // 例如重复定位 socket链接
//            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.headers.collectState {
//                    onLoading = {
//                        Log.d("headers", "加载中")
//                    }
//
//                    onSuccess = { data ->
//                        adapter.submitList(data)
//                    }
//
//                    onError = { e ->
//
//                        when (e) {
//                            is ApiException -> Log.e("headers", "${e.code} === ${e.message}")
//                            else -> Log.e("headers", e.message ?: "Not Error")
//                        }
//
//                    }
//                }
//            }
//        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.wanAndroidList.collectState {
                onLoading = {
                    Log.d("headers", "加载中")
                }

                onSuccess = { data ->
                    adapter.submitList(data)
                }

                onError = { e ->

                    when (e) {
                        is ApiException -> Log.e("headers", "${e.code} === ${e.message}")
                        else -> Log.e("headers", e.message ?: "Not Error")
                    }
                }
            }
        }

        viewModel.headers.observeState(viewLifecycleOwner) {
            onLoading = {
                Log.d("headers", "加载中")
            }

            onSuccess = { data ->
                Log.d("===========", data.userAgent)
            }

            onError = { e ->

                when (e) {
                    is ApiException -> Log.e("headers", "${e.code} === ${e.message}")
                    else -> Log.e("headers", e.message ?: "Not Error")
                }
            }
        }
    }

    private fun requestPermission() {
        PermissionX.request(
            requireActivity(),
            arrayOf(
                android.Manifest.permission.CALL_PHONE,
                android.Manifest.permission.CAMERA
            )
        ) { allGranted, deniedList ->
            if (allGranted) {
                Toast.makeText(context, "已全部同意", Toast.LENGTH_SHORT).show()

                goToLinkActivity()
            } else {
                deniedList.forEach { permissionName ->
                    if (!shouldShowRequestPermissionRationale(permissionName)) {
                        // 用户拒绝权限并且系统不再弹出请求权限的弹窗
                        Logger.d("已拒绝并不再提示 === $permissionName")
                    }
                }
                Toast.makeText(context, "已拒绝 $deniedList", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToLinkActivity() {
        //  <a href ="yikwing://yk:9001/props?macthId=222&time=10001">打开源生应用指定的页面</a>
//        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("yikwing://yk:9001/props?matchId=222&time=10001")))

        forActivityResultLauncher.launch("matchId=222&time=10001")
    }

    override fun removeItem(position: Int) {
        if (position == 0) {
            goToLinkActivity()
            return
        } else if (position == 1) {
            requestPermission()
            return
        }
        viewModel.removeItem(position, adapter.currentList)
    }
}
