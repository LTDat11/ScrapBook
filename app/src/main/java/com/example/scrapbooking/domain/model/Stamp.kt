package com.example.scrapbooking.domain.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "stamps")
data class Stamp(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val path: String,
    val lastModified: Long
)