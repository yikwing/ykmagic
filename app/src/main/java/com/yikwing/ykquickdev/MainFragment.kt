package com.yikwing.ykquickdev

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.yikwing.logger.Logger
import com.yikwing.ykextension.unSafeLazy
import com.yikwing.ykquickdev.databinding.MainFragmentBinding
import com.yk.yknetwork.ApiException
import com.yk.yknetwork.collectState
import com.yk.ykproxy.BaseFragment
import kotlinx.coroutines.launch

class MainFragment : BaseFragment<MainFragmentBinding>(MainFragmentBinding::inflate), CustomListAdapterCallBack {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel by viewModels<MyViewModel>()

    private val adapter by unSafeLazy {
        CustomListAdapter(this)
    }

    override fun lazyInit() {

        Logger.d("===%s===", "lazyInit")

        viewModel.initData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Logger.d("===%s===", "onActivityCreated")

        binding.wxRecycler.layoutManager = LinearLayoutManager(context)
        binding.wxRecycler.adapter = adapter


        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.headers.collectState {
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
        }


//        binding.message.setOnClickListener {
//
//            PermissionX.request(
//                requireActivity(),
//                Manifest.permission.CALL_PHONE,
//                Manifest.permission.CAMERA,
//            ) { allGranted, deniedList ->
//                if (allGranted) {
//                    Toast.makeText(context, "已全部同意", Toast.LENGTH_SHORT).show()
//
//                    goToLinkActivity()
//                } else {
//                    Toast.makeText(context, "已拒绝 $deniedList", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//        }
    }


    private fun goToLinkActivity() {
        //  <a href ="yikwing://yk:9001/props?macthId=222&time=10001">打开源生应用指定的页面</a>
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("yikwing://yk:9001/props?macthId=222&time=10001")))
    }

    override fun removeItem(position: Int) {
        if (position == 0) {
            goToLinkActivity()
            return
        }
        viewModel.removeItem(position, adapter.currentList)
    }

}