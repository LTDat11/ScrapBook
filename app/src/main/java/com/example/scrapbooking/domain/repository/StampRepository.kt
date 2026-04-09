package com.example.scrapbooking.domain.repository

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import com.example.scrapbooking.domain.model.Stamp

interface StampRepository {
    /**
     * Save a stamp image to app storage
     * @param bitmap The bitmap to save
     * @param fileName The name for the saved file
     * @return The absolute path or Uri of the saved image
     */
    suspend fun saveStampImage(bitmap: Bitmap, fileName: String): Result<String>

    /**
     * Observe all stamp images in storage
     */
    fun getStampImages(): Flow<List<Stamp>>

    /**
     * Delete a stamp image by id (Room id)
     */
    suspend fun deleteStampById(id: Long): Result<Unit>

    /**
     * Get a stamp by id
     */    
    suspend fun getStampById(id: Long): Result<Stamp>
}