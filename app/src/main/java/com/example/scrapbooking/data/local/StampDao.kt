package com.example.scrapbooking.data.local

import androidx.room.*
import com.example.scrapbooking.domain.model.Stamp
import kotlinx.coroutines.flow.Flow

@Dao
interface StampDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStamp(stamp: Stamp): Long

    @Delete
    suspend fun deleteStamp(stamp: Stamp): Int

    @Query("SELECT * FROM stamps ORDER BY lastModified DESC")
    fun getAllStamps(): Flow<List<Stamp>>

    @Query("SELECT * FROM stamps WHERE id = :id LIMIT 1")
    suspend fun getStampById(id: Long): Stamp?
}
