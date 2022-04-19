package com.yikwing.ykquickdev

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import coil.load
import com.yikwing.ykextension.app.getPackageInfo
import com.yikwing.ykquickdev.databinding.FragmentHiltBinding
import com.yk.ykproxy.BaseFragment

class HiltFragment : BaseFragment<FragmentHiltBinding>(FragmentHiltBinding::inflate) {

    companion object {
        fun newInstance() = HiltFragment()
    }

    override fun lazyInit() {
        val packageInfo = requireContext().getPackageInfo("com.yktc.nutritiondiet")
        binding.appIcon.load(packageInfo?.appIcon)

        binding.tvAppName.text = packageInfo?.appName
        binding.tvAppPackage.text = packageInfo?.appPackageName

        binding.tvHiltAppVersionCode.text = packageInfo?.versionCode?.toString()
        binding.tvHiltAppVersionName.text = packageInfo?.versionName

        binding.tvHiltMd5.apply {
            text = packageInfo?.signMD5
            setOnClickListener {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("simple text", packageInfo?.signMD5)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "MD5值已复制", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvHiltSha1.apply {
            text = packageInfo?.signSHA1
            setOnClickListener {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("simple text", packageInfo?.signSHA1)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "SHA1值已复制", Toast.LENGTH_SHORT).show()
            }
        }
    }

}