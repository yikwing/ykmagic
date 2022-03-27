package com.yikwing.ykquickdev.permission

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment


const val PREMISSIONCODE = 1001

typealias PermissionCallback = (Boolean, List<String>) -> Unit

class InvisibleFragment : Fragment() {

    private var callback: PermissionCallback? = null

    fun requestNow(cb: PermissionCallback, vararg permissions: String) {
        callback = cb

        requestPermissionLaunch.launch(permissions)
//        requestPermissions(permissions, PREMISSIONCODE)
    }


//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        if (requestCode == PREMISSIONCODE) {
//            val deniedList = mutableListOf<String>()
//            for ((index, result) in grantResults.withIndex()) {
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    deniedList.add(permissions[index])
//                }
//            }
//            val allGranted = deniedList.isEmpty()
//            callback?.let {
//                it(allGranted, deniedList)
//            }
//        }
//
//    }

    private val requestPermissionLaunch = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
        val deniedList = mutableListOf<String>()

        for ((permission, isGRANTED) in grantResults) {
            if (!isGRANTED) {
                deniedList.add(permission)
            }
        }

        val allGranted = deniedList.isEmpty()
        callback?.let {
            it(allGranted, deniedList)
        }
    }

}