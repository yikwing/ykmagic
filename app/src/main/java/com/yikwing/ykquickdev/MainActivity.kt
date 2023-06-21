package com.yikwing.ykquickdev

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.yikwing.ykquickdev.ui.screens.MainScreenFragment
import dagger.hilt.android.AndroidEntryPoint
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

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressTime = System.currentTimeMillis()
            }
        }

        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        if (savedInstanceState == null) {
            with(supportFragmentManager.beginTransaction()) {
                replace(R.id.container, MainScreenFragment())
                commit()
            }
        }
    }
}
