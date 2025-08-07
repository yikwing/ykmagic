package com.yikwing.ykquickdev

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.squareup.moshi.Moshi
import com.yikwing.extension.NetConnectManager
import com.yikwing.proxy.BaseActivity
import com.yikwing.ykquickdev.api.serializers.BooleanAsIntSerializer
import com.yikwing.ykquickdev.databinding.MainActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

@AndroidEntryPoint
class MainActivity : BaseActivity<MainActivityBinding>(MainActivityBinding::inflate) {
    @Inject
    lateinit var moshi: Moshi

    private var backPressTime by Delegates.observable(0L) { _: KProperty<*>, oldValue: Long, newValue: Long ->
        if (newValue - oldValue < 2000) {
            finish()
        } else {
            Toast.makeText(this, "再按一次返回退出", Toast.LENGTH_SHORT).show()
        }
    }

    private val vm: DataStoreViewModel by viewModels()

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        Log.e(
            "qingshan",
            "网络是否连接= ${NetConnectManager.isConnected()} & 网络类型= ${NetConnectManager.getConnectType()}",
        )
        NetConnectManager.addNetTypeChangeListener {
            Log.e("qingshan", "网络类型[监听]= $it")
        }
        NetConnectManager.addNetStatusChangeListener {
            Log.e("qingshan", "网络是否已连接[监听]= $it")
        }

//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
//            // 设置状态栏字体颜色
//            val insetsController = WindowCompat.getInsetsController(window, binding.root)
//            // 设置状态栏图标颜色
//            insetsController.isAppearanceLightStatusBars = true
//
//            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.updatePadding(top = insets.top)
//            WindowInsetsCompat.CONSUMED
//        }

        vm.initX()

        updateUserPreferences()

        setupOnBackPressedHandler()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.container, MainFragment())
                commit()
            }
        }
    }

    private fun updateUserPreferences() {
        lifecycleScope.launch {
            userPreferencesStore
                .updateData { user ->
                    user.copy(name = "zs", age = 18)
                }.also {
                    logUserPreferences()
                }
        }
    }

    @Serializable
    data class User(
        val name: String,
        val age: Int,
        @Serializable(with = BooleanAsIntSerializer::class) val isMan: Boolean,
    )

    private suspend fun getUserPreferences(): UserPreferences = userPreferencesStore.data.map { it }.first()

    private fun logUserPreferences() {
        lifecycleScope.launch {
            val preferences = getUserPreferences()
            Log.d("==== lifecycleScope", preferences.toString())
        }
    }

    private fun setupOnBackPressedHandler() {
        val onBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressTime = System.currentTimeMillis()
                }
            }
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
    }
}
