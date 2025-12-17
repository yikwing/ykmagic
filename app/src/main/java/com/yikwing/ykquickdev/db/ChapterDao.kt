package com.yikwing.ykquickdev.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yikwing.ykquickdev.api.entity.ChapterBean
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterBean>)

    @Query("SELECT * FROM t_chapter")
    fun observeAllChapters(): Flow<List<ChapterBean>>

    @Query("DELETE FROM t_chapter")
    suspend fun deleteAll()
}
