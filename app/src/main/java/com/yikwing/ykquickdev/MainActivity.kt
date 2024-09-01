package com.yikwing.ykquickdev

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yikwing.ykquickdev.ui.fragment.MainScreenFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var backPressTime by Delegates.observable(0L) { _: KProperty<*>, oldValue: Long, newValue: Long ->
        if (newValue - oldValue < 2000) {
            finish()
        } else {
            Toast.makeText(this, "再按一次返回退出", Toast.LENGTH_SHORT).show()
        }
    }

    private val vm: DataStoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        vm.initX()

        updateUserPreferences()
        logUserPreferences()

        setupOnBackPressedHandler()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.container, MainScreenFragment())
                commit()
            }
        }
    }

    private fun updateUserPreferences() {
        lifecycleScope.launch {
            userPreferencesStore.updateData { user ->
                user.copy(name = "zs", age = 18)
            }
        }
    }

    private fun logUserPreferences() {
        lifecycleScope.launch {
            val preferences = userPreferencesStore.data.map { it }.first()

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
