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
import coil3.load
import coil3.request.transformations
import coil3.size.Scale
import coil3.transform.CircleCropTransformation
import com.yikwing.extension.image.compressImageFromUri
import com.yikwing.extension.intArgument
import com.yikwing.extension.plus.yes
import com.yikwing.extension.stringArgument
import com.yikwing.extension.view.backGroundRadiusColor
import com.yikwing.proxy.BaseFragment
import com.yikwing.ykquickdev.databinding.FragmentHiltBinding
import java.io.File
import kotlin.random.Random

class HiltFragment : BaseFragment<FragmentHiltBinding>(FragmentHiltBinding::inflate) {
    private var param1: Int by intArgument()
    private var param2: String by stringArgument()

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
