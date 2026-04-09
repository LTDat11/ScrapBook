package com.example.scrapbooking.data.repository

import android.graphics.Bitmap
import com.example.scrapbooking.data.local.StampDao
import com.example.scrapbooking.domain.model.Stamp
import com.example.scrapbooking.domain.repository.StampRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StampRepositoryImpl @Inject constructor(
    private val stampDao: StampDao
) : StampRepository {
    override suspend fun saveStampImage(bitmap: Bitmap, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        // TODO: Xử lý lưu file bitmap vào storage, sau đó insert vào Room
        // Giả sử đã lưu file thành công, path là filePath
        val filePath = "[implement file saving logic here]"
        val stamp = Stamp(path = filePath, lastModified = System.currentTimeMillis())
        val id = stampDao.insertStamp(stamp)
        if (id > 0) Result.success(filePath) else Result.failure(Exception("Insert failed"))
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
