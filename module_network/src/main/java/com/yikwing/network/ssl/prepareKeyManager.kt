package com.yikwing.network.ssl

import android.util.Log
import java.io.InputStream
import java.security.KeyStore
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory

internal fun prepareKeyManager(
    bksFile: InputStream?,
    password: String?,
): Array<KeyManager>? {
    try {
        if (bksFile == null || password == null) return null
        val clientKeyStore = KeyStore.getInstance("BKS")
        clientKeyStore.load(bksFile, password.toCharArray())
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(clientKeyStore, password.toCharArray())
        return kmf.keyManagers
    } catch (e: Exception) {
        Log.d("prepareKeyManager", e.localizedMessage, e)
    }
    return null
}
