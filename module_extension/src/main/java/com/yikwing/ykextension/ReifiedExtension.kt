package com.yikwing.ykextension

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 *
 * startActivity
 * startActivity<NewActivity>(context)
 * startActivity<NewActivity>(context){}
 *
 * */
inline fun <reified T : Activity> Activity.startActivity(context: Context) {
    startActivity(Intent(context, T::class.java))
}

inline fun <reified T : Activity> Activity.startActivity(context: Context, block: Intent.() -> Unit) {
    startActivity(Intent(context, T::class.java).apply(block))
}
