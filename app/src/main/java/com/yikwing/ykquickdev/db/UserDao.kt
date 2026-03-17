package com.yikwing.ykquickdev.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    fun insertUser(user: User)

    @Query("SELECT * FROM T_USER")
    fun queryAllUsers(): Flow<List<User>>
}
