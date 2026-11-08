package com.example.data.model

enum class SkipStatus {
    SKIPPED,
    URGENT_CLOSING_SOON,
    WINDOW_OPEN,
    UPCOMING,
    MISSED,
    PAUSED
}

data class MembershipWithStatus(
    val membership: VipMembership,
    val record: SkipRecord?,
    val status: SkipStatus,
    val daysRemainingInWindow: Int,
    val daysUntilWindowOpens: Int,
    val isWindowCurrentlyOpen: Boolean,
    val displayStatusMessage: String
)

data class MonthlySummary(
    val year: Int,
    val month: Int,
    val totalSavedThisMonth: Double,
    val totalAvoidableCost: Double,
    val activeMembershipsCount: Int,
    val skippedCount: Int,
    val unskippedOpenCount: Int,
    val missedCount: Int,
    val allTimeSaved: Double,
    val allTimeSkipsCount: Int
)
