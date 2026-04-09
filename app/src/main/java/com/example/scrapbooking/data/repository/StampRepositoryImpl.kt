package com.example.scrapbooking.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import com.example.scrapbooking.data.local.StampDao
import com.example.scrapbooking.domain.model.Stamp
import com.example.scrapbooking.domain.repository.StampRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import android.util.Log
import com.example.scrapbooking.BuildConfig

class StampRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stampDao: StampDao
) : StampRepository {
    override suspend fun saveStampImage(bitmap: Bitmap, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Nếu fileName là đường dẫn tuyệt đối đã tồn tại (ví dụ HomeViewModel đã lưu trước đó), dùng trực tiếp
            val providedPath = when {
                fileName.startsWith("file://") -> fileName.removePrefix("file://")
                fileName.startsWith("/") -> fileName
                else -> null
            }

            val finalPath = if (providedPath != null) {
                val existing = File(providedPath)
                if (existing.exists()) {
                    Log.d("StampRepo", "Using existing file path: $providedPath")
                    "file://${existing.absolutePath}"
                } else {
                    Log.w("StampRepo", "Provided path does not exist, will create new file: $providedPath")
                    null
                }
            } else null

            val resultPath = if (finalPath != null) {
                finalPath
            } else {
                // Lưu bitmap vào thư mục private của app (nếu không có đường dẫn hợp lệ)
                val dir = context.getExternalFilesDir("images") ?: context.filesDir
                if (!dir.exists()) dir.mkdirs()
                val safeName = File(fileName).name
                val file = File(dir, safeName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    out.flush()
                }
                val filePath = file.absolutePath
                if (BuildConfig.DEBUG) Log.d("StampRepo", "Saved file: $filePath, exists: ${file.exists()}")
                "file://$filePath"
            }

            // Insert vào DB
            val stamp = Stamp(path = resultPath, lastModified = System.currentTimeMillis())
            val id = stampDao.insertStamp(stamp)
            if (BuildConfig.DEBUG) Log.d("StampRepo", "Insert DB id: $id, path: $resultPath")
            if (id > 0) Result.success(resultPath) else Result.failure(Exception("Insert failed"))
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("StampRepo", "Save error", e)
            Result.failure(e)
        }
    }

    override fun getStampImages(): Flow<List<Stamp>> = stampDao.getAllStamps()



    override suspend fun deleteStampById(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val stamp = stampDao.getStampById(id)
            if (stamp == null) return@withContext Result.failure(Exception("Stamp not found"))
            stampDao.deleteStamp(stamp)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStampById(id: Long): Result<Stamp> = withContext(Dispatchers.IO) {
        try {
            val stamp = stampDao.getStampById(id)
            if (stamp != null) Result.success(stamp) else Result.failure(Exception("Stamp not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
