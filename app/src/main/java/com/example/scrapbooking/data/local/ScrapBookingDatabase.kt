package com.example.scrapbooking.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.scrapbooking.domain.model.Stamp

@Database(entities = [Stamp::class], version = 1, exportSchema = false)
abstract class ScrapBookingDatabase : RoomDatabase() {
    abstract fun stampDao(): StampDao
}
