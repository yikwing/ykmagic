package com.yk.ykpermission

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment


class InvisibleFragment : Fragment() {

    private var callback: PermissionCallback? = null

    fun requestNow(cb: PermissionCallback, permissions: Array<String>) {
        callback = cb

        requestPermissionLaunch.launch(permissions)
    }

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