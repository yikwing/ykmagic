package com.yikwing.ykquickdev

import com.yikwing.datastore.DataStoreOwner
import com.yikwing.datastore.IDataStoreOwner

/**
 * @Author yikwing
 * @Date 16/5/2023-11:24
 * @Description:
 */
object DataStoreRepository : IDataStoreOwner by DataStoreOwner("settings") {
    val count by intPreference(default = 1)
}
