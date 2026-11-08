package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.data.local.AppDatabase
import com.example.data.model.SkipRecord
import com.example.data.model.SkipStatus
import com.example.notification.NotificationHelper
import com.example.util.SkipDateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SkipReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CHECK_REMINDERS = "com.example.skipvip.CHECK_REMINDERS"
        const val ACTION_MARK_SKIPPED_FROM_NOTIFICATION = "com.example.skipvip.MARK_SKIPPED"
        const val EXTRA_MEMBERSHIP_ID = "EXTRA_MEMBERSHIP_ID"
        const val EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val db = AppDatabase.getDatabase(context)

        when (intent.action) {
            ACTION_CHECK_REMINDERS -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val currentYear = SkipDateUtils.getCurrentYear()
                    val currentMonth = SkipDateUtils.getCurrentMonth()
                    val activeMemberships = db.vipMembershipDao().getActiveMembershipsSync()
                    val existingRecords = db.skipRecordDao().getRecordsForMonthSync(currentYear, currentMonth)
                    val recordsMap = existingRecords.associateBy { it.membershipId }

                    val unskippedOpenList = activeMemberships.map { membership ->
                        SkipDateUtils.calculateStatus(
                            membership = membership,
                            record = recordsMap[membership.id],
                            viewYear = currentYear,
                            viewMonth = currentMonth
                        )
                    }.filter { it.status == SkipStatus.WINDOW_OPEN || it.status == SkipStatus.URGENT_CLOSING_SOON }

                    if (unskippedOpenList.isNotEmpty()) {
                        val totalAvoidable = unskippedOpenList.sumOf { it.membership.monthlyCost }
                        val names = unskippedOpenList.joinToString(", ") { it.membership.name }
                        val hasUrgent = unskippedOpenList.any { it.status == SkipStatus.URGENT_CLOSING_SOON }

                        val title = if (hasUrgent) {
                            "⚠️ FINAL CALL: Skip Month for $names"
                        } else {
                            "🛡️ Skip Window Open: $names"
                        }

                        val message = "Skip today to avoid ${SkipDateUtils.formatCurrency(totalAvoidable)} in unwanted auto-charges!"

                        // If single membership, pass its ID and URL for direct actions
                        val firstItem = unskippedOpenList.first()
                        NotificationHelper.showSkipReminderNotification(
                            context = context,
                            title = title,
                            message = message,
                            notificationId = NotificationHelper.NOTIFICATION_ID_BASE,
                            membershipId = if (unskippedOpenList.size == 1) firstItem.membership.id else null,
                            portalUrl = if (unskippedOpenList.size == 1) firstItem.membership.portalUrl else null
                        )
                    }
                }
            }

            ACTION_MARK_SKIPPED_FROM_NOTIFICATION -> {
                val membershipId = intent.getLongExtra(EXTRA_MEMBERSHIP_ID, -1L)
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, NotificationHelper.NOTIFICATION_ID_BASE)

                if (membershipId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val currentYear = SkipDateUtils.getCurrentYear()
                        val currentMonth = SkipDateUtils.getCurrentMonth()
                        val membership = db.vipMembershipDao().getMembershipById(membershipId)

                        if (membership != null) {
                            db.skipRecordDao().insertOrUpdate(
                                SkipRecord(
                                    membershipId = membershipId,
                                    year = currentYear,
                                    month = currentMonth,
                                    status = "SKIPPED",
                                    actionTimestamp = System.currentTimeMillis(),
                                    savedAmount = membership.monthlyCost,
                                    note = "Skipped from notification"
                                )
                            )
                        }

                        NotificationManagerCompat.from(context).cancel(notificationId)
                    }
                }
            }
        }
    }
}
