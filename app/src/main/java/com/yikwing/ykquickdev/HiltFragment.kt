package com.yikwing.ykquickdev

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import coil.load
import com.yikwing.logger.Logger
import com.yikwing.ykextension.FragmentArgumentDelegate
import com.yikwing.ykextension.app.getPackageInfo
import com.yikwing.ykextension.unSafeLazy
import com.yikwing.ykquickdev.databinding.FragmentHiltBinding
import com.yk.compress.compress
import com.yk.compress.compressBitmap
import com.yk.ykproxy.BaseFragment
import kotlinx.coroutines.launch
import java.io.File
import kotlin.concurrent.thread

class HiltFragment : BaseFragment<FragmentHiltBinding>(FragmentHiltBinding::inflate) {

    private var param1: Int by FragmentArgumentDelegate(0)
    private var param2: String by FragmentArgumentDelegate("")

    companion object {
        fun newInstance(a: Int = 23, b: String = "zs") = HiltFragment().apply {
            param1 = a
            param2 = b
        }
    }

    private val packageInfo by unSafeLazy {
        requireContext().getPackageInfo("com.yktc.nutritiondiet")
    }


    override fun lazyInit() {
        binding.appIcon.load(packageInfo?.appIcon)

        binding.tvAppName.text = packageInfo?.appName
        binding.tvAppPackage.text = packageInfo?.appPackageName

        binding.tvHiltAppVersionCode.text = packageInfo?.versionCode?.toString()
        binding.tvHiltAppVersionName.text = packageInfo?.versionName

        binding.tvHiltMd5.apply {
            text = packageInfo?.signMD5
            setOnClickListener {
                copyToClipboard(context, packageInfo?.signMD5, "MD5值已复制")
            }
        }

        binding.tvHiltSha1.apply {
            text = packageInfo?.signSHA1
            setOnClickListener {
                copyToClipboard(context, packageInfo?.signSHA1, "SHA1值已复制")
            }


            viewLifecycleOwner.lifecycleScope.launch {
                compress(
                    context, listOf(
                        File(context.filesDir, "result_11.webp"),
                        File(context.filesDir, "default_image.jpg"),
                        File(context.filesDir, "result.webp"),
                    )
                ) {
                    Logger.d(it.joinToString())
                }
            }


            thread {
                compressBitmap(context, File(context.filesDir, "cc.jpg").absolutePath, 200) { file ->
                    Logger.d(file.absolutePath)
                }
            }


        }
    }

    private fun copyToClipboard(context: Context, copyStr: String?, tips: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("simple text", copyStr)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, tips, Toast.LENGTH_SHORT).show()
    }

}