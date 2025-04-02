package com.yikwing.ykquickdev.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File

/**
 * <pre>
 *     author: yikwing
 *
 *        _ _              _
 *       (_) |            (_)
 *  _   _ _| | ____      ___ _ __   __ _
 * | | | | | |/ /\ \ /\ / / | '_ \ / _` |
 * | |_| | |   <  \ V  V /| | | | | (_| |
 *  \__, |_|_|\_\  \_/\_/ |_|_| |_|\__, |
 *   __/ |                          __/ |
 *  |___/                          |___/
 *
 *     email : 49999@live.com
 *     time  : 2025-04-02 22:14
 *     desc  : clean cache
 * </pre>
 */
class CleanCacheWork(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        try {
            val cacheDir = applicationContext.cacheDir
            deleteFiles(cacheDir)
            Log.d("ClearCacheWorker", "缓存清理完成")
            Result.success()
        } catch (e: Exception) {
            Log.e("ClearCacheWorker", "清理失败: ${e.message}")
            Result.failure()
        }

    private fun deleteFiles(directory: File) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                deleteFiles(file) // 递归清理子目录
            }
            file.delete() // 删除文件
        }
    }
}
