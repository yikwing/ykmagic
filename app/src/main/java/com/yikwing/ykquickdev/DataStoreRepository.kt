package com.yikwing.ykquickdev

import com.yikwing.datastore.DataStoreOwner
import com.yikwing.datastore.IDataStoreOwner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @Author yikwing
 * @Date 16/5/2023-11:24
 * @Description:
 */

@Singleton
class DataStoreRepository @Inject constructor() : IDataStoreOwner by DataStoreOwner("settings") {
    val count by intPreference(default = 1)
}
