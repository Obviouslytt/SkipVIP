package com.example.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit

object VipWorkScheduler {

    const val UNIQUE_PERIODIC_WORK_NAME = "vip_skip_periodic_check"
    const val UNIQUE_IMMEDIATE_WORK_NAME = "vip_skip_immediate_check"

    fun scheduleAllReminders(context: Context) {
        schedulePeriodicSkipCheck(context)
        scheduleTargetedWindowReminders(context)
    }

    fun schedulePeriodicSkipCheck(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<VipSkipReminderWorker>(
            12, TimeUnit.HOURS,
            30, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .addTag(VipSkipReminderWorker.TAG)
            .setInputData(workDataOf(VipSkipReminderWorker.KEY_REASON to VipSkipReminderWorker.REASON_PERIODIC))
            .build()

        workManager.enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }

    fun scheduleTargetedWindowReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val now = Calendar.getInstance()

        // 1. Day 1 at 9:00 AM
        val day1Cal = getTargetCalendar(targetDay = 1, hour = 9, minute = 0)
        val delayDay1 = day1Cal.timeInMillis - now.timeInMillis
        if (delayDay1 > 0) {
            val day1Request = OneTimeWorkRequestBuilder<VipSkipReminderWorker>()
                .setInitialDelay(delayDay1, TimeUnit.MILLISECONDS)
                .addTag(VipSkipReminderWorker.TAG)
                .setInputData(workDataOf(VipSkipReminderWorker.KEY_REASON to VipSkipReminderWorker.REASON_DAY1))
                .build()
            workManager.enqueueUniqueWork("vip_skip_day1_alert", ExistingWorkPolicy.REPLACE, day1Request)
        }

        // 2. Day 3 at 10:00 AM
        val day3Cal = getTargetCalendar(targetDay = 3, hour = 10, minute = 0)
        val delayDay3 = day3Cal.timeInMillis - now.timeInMillis
        if (delayDay3 > 0) {
            val day3Request = OneTimeWorkRequestBuilder<VipSkipReminderWorker>()
                .setInitialDelay(delayDay3, TimeUnit.MILLISECONDS)
                .addTag(VipSkipReminderWorker.TAG)
                .setInputData(workDataOf(VipSkipReminderWorker.KEY_REASON to VipSkipReminderWorker.REASON_DAY3))
                .build()
            workManager.enqueueUniqueWork("vip_skip_day3_alert", ExistingWorkPolicy.REPLACE, day3Request)
        }

        // 3. Day 5 Urgent Final Day (9:00 AM)
        val day5Cal = getTargetCalendar(targetDay = 5, hour = 9, minute = 0)
        val delayDay5 = day5Cal.timeInMillis - now.timeInMillis
        if (delayDay5 > 0) {
            val day5Request = OneTimeWorkRequestBuilder<VipSkipReminderWorker>()
                .setInitialDelay(delayDay5, TimeUnit.MILLISECONDS)
                .addTag(VipSkipReminderWorker.TAG)
                .setInputData(workDataOf(VipSkipReminderWorker.KEY_REASON to VipSkipReminderWorker.REASON_FINAL_DAY))
                .build()
            workManager.enqueueUniqueWork("vip_skip_final_day_alert", ExistingWorkPolicy.REPLACE, day5Request)
        }
    }

    private fun getTargetCalendar(targetDay: Int, hour: Int, minute: Int): Calendar {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, targetDay)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) {
            // Target is in the next month
            target.add(Calendar.MONTH, 1)
            target.set(Calendar.DAY_OF_MONTH, targetDay)
        }
        return target
    }

    fun enqueueImmediateCheck(context: Context): UUID {
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<VipSkipReminderWorker>()
            .addTag(VipSkipReminderWorker.TAG)
            .setInputData(workDataOf(VipSkipReminderWorker.KEY_REASON to VipSkipReminderWorker.REASON_MANUAL))
            .build()

        workManager.enqueueUniqueWork(
            UNIQUE_IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
        return request.id
    }

    fun getWorkInfoFlow(context: Context): Flow<List<WorkInfo>> {
        val workManager = WorkManager.getInstance(context)
        return workManager.getWorkInfosByTagFlow(VipSkipReminderWorker.TAG)
    }

    fun cancelAllWork(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(VipSkipReminderWorker.TAG)
    }
}
