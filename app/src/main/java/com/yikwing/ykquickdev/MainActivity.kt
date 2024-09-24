package com.yikwing.ykquickdev

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.squareup.moshi.Moshi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        vm.initX()

        updateUserPreferences()

        setupOnBackPressedHandler()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.container, HiltFragment.newInstance())
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

    private fun logUserPreferences() {
        lifecycleScope.launch {
            val preferences = userPreferencesStore.data.map { it }.first()

            val c = moshi.adapter(UserPreferences::class.java).toJson(preferences)

            Log.d("==== lifecycleScope", c)
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
