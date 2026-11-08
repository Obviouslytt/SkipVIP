package com.example

import android.app.Application
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.repository.VipRepository
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import com.example.work.VipWorkScheduler

class SkipVipApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { VipRepository(database.vipMembershipDao(), database.skipRecordDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        AlarmScheduler.scheduleDailyCheckAlarm(this)
        try {
            VipWorkScheduler.scheduleAllReminders(this)
        } catch (e: Exception) {
            Log.w("SkipVipApplication", "Could not schedule reminders in current context", e)
        }
    }
}
