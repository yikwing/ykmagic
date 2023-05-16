package com.yikwing.ykquickdev.task

import android.app.Application
import android.content.Context
import com.yikwing.datastore.IDataStoreOwner
import com.yk.ykproxy.startup.Initializer

class DataStoreInitTask : Initializer<Unit> {
    override fun create(context: Context) {
        IDataStoreOwner.application = context as Application
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> = emptySet()
}
