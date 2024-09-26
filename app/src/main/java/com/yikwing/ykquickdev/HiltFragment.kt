package com.yikwing.ykquickdev

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.yikwing.extension.FragmentArgumentDelegate
import com.yikwing.extension.app.getPackageInfo
import com.yikwing.extension.image.compressImageFromUri
import com.yikwing.extension.unSafeLazy
import com.yikwing.extension.view.backGroundRadiusColor
import com.yikwing.proxy.BaseFragment
import com.yikwing.ykquickdev.databinding.FragmentHiltBinding
import java.io.File
import kotlin.random.Random

class HiltFragment : BaseFragment<FragmentHiltBinding>(FragmentHiltBinding::inflate) {
    private var param1: Int by FragmentArgumentDelegate(0)
    private var param2: String by FragmentArgumentDelegate("")

    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

    companion object {
        @JvmStatic
        fun newInstance(
            a: Int = 23,
            b: String = "zs",
        ) = HiltFragment().apply {
            param1 = a
            param2 = b
        }
    }

    private val packageInfo by unSafeLazy {
        requireContext().getPackageInfo("com.yktc.nutritiondiet")
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: $uri")

                    val cacheFile =
                        File(requireContext().cacheDir, "cache_uri_${Random.nextLong()}").path

                    compressImageFromUri(
                        requireContext(),
                        uri,
                        cacheFile,
                        100,
                    ).yes {
                        binding.appIcon.apply {
                            backGroundRadiusColor(
                                Color.parseColor("#54CEE3"),
                                50f,
                            )
                            load(
                                File(cacheFile),
                            ) {
//                                transformations(
//                                    RoundedCornersTransformation(
//                                        topLeft = 20f,
//                                        topRight = 20f,
//                                        bottomLeft = 50f,
//                                        bottomRight = 50f,
//                                    ),
//                                )

                                transformations(
                                    CircleCropTransformation(),
                                )

                                scale(Scale.FILL)

                                listener(
                                    onStart = { request ->
                                        Log.d("coil-", "onStart")
                                    },
                                    onError = { request, throwable ->
                                        Log.d("coil-", "onError")
                                    },
                                    onCancel = { request ->
                                        Log.d("coil-", "onCancel")
                                    },
                                    onSuccess = { request, metadata ->
                                        Log.d("coil-", "onSuccess")
                                    },
                                )
                            }
                        }
                    }
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }
    }

    override fun lazyInit() {
//        binding.appIcon.load(packageInfo?.appIcon)

        binding.tvAppName.text = packageInfo?.appName
        binding.tvAppPackage.text = packageInfo?.appPackageName

        binding.tvHiltAppVersionCode.text = packageInfo?.versionCode?.toString()
        binding.tvHiltAppVersionName.text = packageInfo?.versionName

        binding.tvAppVersionCode.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

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
        }
    }

    private fun copyToClipboard(
        context: Context,
        copyStr: String?,
        tips: String,
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("simple text", copyStr)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, tips, Toast.LENGTH_SHORT).show()
    }
}

inline fun Boolean.yes(block: () -> Unit) {
    if (this) {
        block()
    }
}
