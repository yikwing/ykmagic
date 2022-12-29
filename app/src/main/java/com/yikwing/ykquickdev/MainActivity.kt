package com.yikwing.ykquickdev

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import kotlin.properties.Delegates
import kotlin.reflect.KProperty

class MainActivity : AppCompatActivity() {

    private var backPressTime by Delegates.observable(0L) { _: KProperty<*>, oldValue: Long, newValue: Long ->
        if (newValue - oldValue < 2000) {
            finish()
        } else {
            Toast.makeText(this, "再按一次返回退出", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressTime = System.currentTimeMillis()
            }
        }

        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance()).commitNow()
        }
    }
}