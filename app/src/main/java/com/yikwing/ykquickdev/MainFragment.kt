package com.yikwing.ykquickdev

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
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
import com.yk.ykpermission.PermissionX
import com.yk.ykproxy.BaseFragment
import kotlinx.coroutines.launch

class MainFragment : BaseFragment<MainFragmentBinding>(MainFragmentBinding::inflate), CustomListAdapterCallBack {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel by viewModels<MyViewModel>()

    var forActivityResultLauncher = registerForActivityResult(object : ActivityResultContract<String, String>() {
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
                        Log.d("headers", "?????????")
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
    }

    private fun requestPermission() {
        PermissionX.request(
            requireActivity(),
            arrayOf(
                android.Manifest.permission.CALL_PHONE,
                android.Manifest.permission.CAMERA,
            )
        ) { allGranted, deniedList ->
            if (allGranted) {
                Toast.makeText(context, "???????????????", Toast.LENGTH_SHORT).show()

                goToLinkActivity()
            } else {
                deniedList.forEach { permissionName ->
                    if (!shouldShowRequestPermissionRationale(permissionName)) {
                        //???????????????????????????????????????????????????????????????
                        Logger.d("???????????????????????? === $permissionName")
                    }
                }
                Toast.makeText(context, "????????? $deniedList", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun goToLinkActivity() {
        //  <a href ="yikwing://yk:9001/props?macthId=222&time=10001">?????????????????????????????????</a>
//        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("yikwing://yk:9001/props?macthId=222&time=10001")))

        forActivityResultLauncher.launch("macthId=222&time=10001")
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