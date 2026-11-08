package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "skip_records",
    indices = [
        Index(value = ["membershipId", "year", "month"], unique = true)
    ]
)
data class SkipRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val membershipId: Long,
    val year: Int,
    val month: Int, // 1 to 12
    val status: String, // "SKIPPED", "BILLED", "PAUSED", "MISSED"
    val actionTimestamp: Long = System.currentTimeMillis(),
    val savedAmount: Double = 0.0,
    val note: String = ""
)
