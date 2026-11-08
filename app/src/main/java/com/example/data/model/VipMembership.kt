package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vip_memberships")
data class VipMembership(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val monthlyCost: Double,
    val currency: String = "$",
    val windowStartDay: Int = 1,     // Day of month skip window opens (e.g. 1)
    val windowEndDay: Int = 5,       // Day of month skip window closes (e.g. 5)
    val category: String = "Fashion",
    val portalUrl: String = "",
    val skipInstructions: String = "",
    val notes: String = "",
    val colorHex: Long = 0xFF6366F1,
    val isActive: Boolean = true,
    val remindEve: Boolean = true,    // Eve before window opens (e.g. last day of prior month at 8 PM)
    val remindDay1: Boolean = true,   // Day 1 at 9 AM
    val remindDay3: Boolean = true,   // Day 3 reminder
    val remindDayLast: Boolean = true, // Final day morning & evening urgent reminder
    val createdAt: Long = System.currentTimeMillis()
)
