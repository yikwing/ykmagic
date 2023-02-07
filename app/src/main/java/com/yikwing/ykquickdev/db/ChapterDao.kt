package com.yikwing.ykquickdev.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.yikwing.ykquickdev.api.entity.ChapterBean

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: List<ChapterBean>)
}
