package com.yikwing.ykquickdev.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.yikwing.ykquickdev.api.entity.ChapterBean
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterBean>)

    /** 替换完整章节列表；删除和写入同一事务，失败时保留原缓存。 */
    @Transaction
    suspend fun replaceChapters(chapters: List<ChapterBean>) {
        deleteAll()
        insertChapters(chapters)
    }

    @Query("SELECT * FROM t_chapter")
    fun observeAllChapters(): Flow<List<ChapterBean>>

    @Query("DELETE FROM t_chapter")
    suspend fun deleteAll()
}
