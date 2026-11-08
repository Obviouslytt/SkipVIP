package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.receiver.SkipReminderReceiver

object NotificationHelper {
    const val CHANNEL_ID = "vip_skip_reminders"
    const val NOTIFICATION_ID_BASE = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showSkipReminderNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = NOTIFICATION_ID_BASE,
        membershipId: Long? = null,
        portalUrl: String? = null
    ) {
        // Create Intent to open MainActivity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            membershipId?.let { putExtra("EXTRA_MEMBERSHIP_ID", it) }
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // Add action button to open portal if available
        if (!portalUrl.isNullOrBlank()) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(portalUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val browserPendingIntent = PendingIntent.getActivity(
                context,
                notificationId + 500,
                browserIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_menu_view,
                "Open Skip Portal",
                browserPendingIntent
            )
        }

        // Add action button to mark as skipped directly from notification
        if (membershipId != null) {
            val markSkippedIntent = Intent(context, SkipReminderReceiver::class.java).apply {
                action = SkipReminderReceiver.ACTION_MARK_SKIPPED_FROM_NOTIFICATION
                putExtra(SkipReminderReceiver.EXTRA_MEMBERSHIP_ID, membershipId)
                putExtra(SkipReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val markSkippedPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 1000,
                markSkippedIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.checkbox_on_background,
                "Mark Skipped ✓",
                markSkippedPendingIntent
            )
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    fun sendTestNotification(context: Context) {
        showSkipReminderNotification(
            context = context,
            title = "🛡️ SkipVIP: Fabletics Window is OPEN!",
            message = "Skip by the 5th to save $69.95 monthly credit. Tap to open portal or mark skipped.",
            notificationId = 9999,
            portalUrl = "https://www.fabletics.com/account"
        )
    }
}
