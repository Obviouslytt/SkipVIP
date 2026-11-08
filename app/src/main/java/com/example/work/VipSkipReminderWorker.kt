package com.example.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.local.AppDatabase
import com.example.data.model.SkipStatus
import com.example.notification.NotificationHelper
import com.example.util.SkipDateUtils

class VipSkipReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "vip_skip_reminder_worker"
        const val KEY_REASON = "key_reason"
        const val KEY_MEMBERSHIP_ID = "key_membership_id"

        const val REASON_PERIODIC = "PERIODIC_CHECK"
        const val REASON_EVE = "EVE_ALERT"
        const val REASON_DAY1 = "DAY1_OPEN"
        const val REASON_DAY3 = "DAY3_CHECK"
        const val REASON_FINAL_DAY = "FINAL_DAY_ALERT"
        const val REASON_MANUAL = "MANUAL_CHECK"
    }

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val reason = inputData.getString(KEY_REASON) ?: REASON_PERIODIC
        val targetMembershipId = inputData.getLong(KEY_MEMBERSHIP_ID, -1L)

        val currentYear = SkipDateUtils.getCurrentYear()
        val currentMonth = SkipDateUtils.getCurrentMonth()
        val currentDay = SkipDateUtils.getCurrentDay()

        val activeMemberships = db.vipMembershipDao().getActiveMembershipsSync()
        if (activeMemberships.isEmpty()) {
            return Result.success(workDataOf("message" to "No active memberships found"))
        }

        val existingRecords = db.skipRecordDao().getRecordsForMonthSync(currentYear, currentMonth)
        val recordsMap = existingRecords.associateBy { it.membershipId }

        // Filter for specific membership if provided, otherwise all
        val targetList = if (targetMembershipId != -1L) {
            activeMemberships.filter { it.id == targetMembershipId }
        } else {
            activeMemberships
        }

        val evaluatedList = targetList.map { membership ->
            SkipDateUtils.calculateStatus(
                membership = membership,
                record = recordsMap[membership.id],
                viewYear = currentYear,
                viewMonth = currentMonth
            )
        }

        // Action needed: Window is open, urgent closing soon, or if reason is EVE alert and tomorrow opens
        val actionableList = evaluatedList.filter { item ->
            when (reason) {
                REASON_EVE -> item.status == SkipStatus.UPCOMING && item.membership.remindEve
                REASON_FINAL_DAY -> (item.status == SkipStatus.URGENT_CLOSING_SOON || item.status == SkipStatus.WINDOW_OPEN) && item.membership.remindDayLast
                REASON_DAY1 -> (item.status == SkipStatus.WINDOW_OPEN || item.status == SkipStatus.URGENT_CLOSING_SOON) && item.membership.remindDay1
                REASON_MANUAL -> item.status != SkipStatus.SKIPPED // Include unskipped for manual verification
                else -> item.status == SkipStatus.WINDOW_OPEN || item.status == SkipStatus.URGENT_CLOSING_SOON
            }
        }

        if (actionableList.isNotEmpty()) {
            val totalAvoidable = actionableList.sumOf { it.membership.monthlyCost }
            val names = actionableList.joinToString(", ") { it.membership.name }
            val hasUrgent = actionableList.any { it.status == SkipStatus.URGENT_CLOSING_SOON }

            val title = when (reason) {
                REASON_EVE -> "⏰ Tomorrow: Skip Window Opens for $names"
                REASON_FINAL_DAY -> "⚠️ FINAL CHANCE: Skip $names Today!"
                REASON_DAY1 -> "🛡️ Skip Window is OPEN: $names"
                REASON_MANUAL -> "🛡️ SkipVIP Check: $names Ready to Skip"
                else -> if (hasUrgent) "⚠️ URGENT: Skip $names" else "🛡️ Skip Window Open: $names"
            }

            val message = when (reason) {
                REASON_EVE -> "Window opens tomorrow on the 1st. Skip before the 5th to save ${SkipDateUtils.formatCurrency(totalAvoidable)}."
                REASON_FINAL_DAY -> "Window closes tonight at midnight! Skip now to avoid ${SkipDateUtils.formatCurrency(totalAvoidable)} auto-charge."
                else -> "Skip today to keep ${SkipDateUtils.formatCurrency(totalAvoidable)} in your wallet and avoid auto-charges!"
            }

            val firstItem = actionableList.first()
            NotificationHelper.showSkipReminderNotification(
                context = applicationContext,
                title = title,
                message = message,
                notificationId = NotificationHelper.NOTIFICATION_ID_BASE,
                membershipId = if (actionableList.size == 1) firstItem.membership.id else null,
                portalUrl = if (actionableList.size == 1) firstItem.membership.portalUrl else null
            )

            return Result.success(
                workDataOf(
                    "notified_count" to actionableList.size,
                    "saved_potential" to totalAvoidable,
                    "reason" to reason
                )
            )
        } else if (reason == REASON_MANUAL) {
            // For manual test runs when all are skipped, send a reassuring notification
            NotificationHelper.showSkipReminderNotification(
                context = applicationContext,
                title = "🛡️ SkipVIP: All Memberships Safe!",
                message = "WorkManager ran: All active VIP memberships are currently skipped for this month. Zero charges at risk! 🎉",
                notificationId = 8888
            )
        }

        return Result.success(
            workDataOf(
                "message" to "No open skip windows needing alerts",
                "reason" to reason
            )
        )
    }
}
