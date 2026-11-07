package com.yikwing.ykquickdev

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class DataStoreExtensionsTest {
    @Test
    fun `updateAndGet should return its own committed value even if a later write is visible`() =
        runBlocking {
            val store =
                object : DataStore<Int> {
                    override val data = MutableStateFlow(0)

                    override suspend fun updateData(transform: suspend (Int) -> Int): Int {
                        val committed = transform(data.value)
                        data.value = committed
                        // Model another write committing before this caller resumes.
                        data.value = committed + 1
                        return committed
                    }
                }

            val updated = store.updateAndGet { it + 1 }

            assertThat(updated, `is`(1))
            assertThat(store.data.value, `is`(2))
        }
}
