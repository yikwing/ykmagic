package com.yikwing.ykquickdev.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yikwing.ykquickdev.api.entity.ChapterBean

@Database(
    entities = [User::class, ChapterBean::class],
    version = 1,
    exportSchema = true,
    autoMigrations = [],
)
abstract class UserDatabase : RoomDatabase() {

    abstract fun getUserDao(): UserDao
    abstract fun getChapterDao(): ChapterDao

    companion object {

        @Volatile
        private var instance: UserDatabase? = null

        fun getInstance(context: Context): UserDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }
        }

        private fun buildDatabase(context: Context): UserDatabase {
            return Room.databaseBuilder(
                context,
                UserDatabase::class.java,
                "user_table",
            ).addCallback(
                object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        Log.d("创建数据库", "创建数据库")
                    }
                },
            ).build()

//                .addMigrations(
//                Migration_1_2,
//                Migration_2_3,
//            ).build()
        }

//        val Migration_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                TODO("Not yet implemented")
//            }
//        }
//
//
//        val Migration_2_3 = object : Migration(2, 3) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                TODO("Not yet implemented")
//            }
//        }
    }
}
