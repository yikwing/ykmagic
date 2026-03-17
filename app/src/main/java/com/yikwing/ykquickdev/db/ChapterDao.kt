package com.yikwing.ykquickdev.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
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
