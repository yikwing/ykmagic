package com.yikwing.ykquickdev.task

import android.app.Application
import com.yk.ykproxy.startup.Initializer
import com.yikwing.datastore.IDataStoreOwner

class DataStoreInitTask : Initializer<Unit> {
    override fun create(context: Application) {
        IDataStoreOwner.application = context
    }

    override fun dependencies(): Set<Class<out Initializer<*>>> = emptySet()
}