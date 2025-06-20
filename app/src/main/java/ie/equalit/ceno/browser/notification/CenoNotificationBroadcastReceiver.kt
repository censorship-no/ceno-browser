package ie.equalit.ceno.browser.notification

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import mozilla.components.support.base.ids.SharedIdsHelper
import mozilla.components.support.ktx.android.notification.ChannelData
import mozilla.components.support.ktx.android.notification.ensureNotificationChannelExists

class CenoNotificationBroadcastReceiver(listener: NotificationListener): BroadcastReceiver() {

    private var listener : NotificationListener? = null

    init {
        this.listener = listener
    }

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        when(intent.action) {
            AbstractPublicNotificationService.ACTION_STOP -> {
                listener?.onStopTapped()
            }
            AbstractPublicNotificationService.ACTION_CLEAR -> {
                listener?.onClearTapped()
            }
            BrowserActivity.ACTION_FORGROUND_REMIND -> {
                val notification = NotificationCompat.Builder(context, getChannelId(context))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true)
                    .setLocalOnly(true)
                    .setContentTitle(context.getString(R.string.reminder_notification_title))
                    .setContentText(context.getString(R.string.reminder_notification_text))
                    .setSmallIcon(R.drawable.ic_launcher_white_foreground)
                    .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, BrowserActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
                    .build()
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    //do nothing if permission not granted
                    return
                }
                NotificationManagerCompat.from(context).notify(SharedIdsHelper.getIdForTag(context, NOTIFICATION_TAG), notification)
            }
        }
    }

    interface NotificationListener {
        fun onStopTapped()

        fun onClearTapped()
    }

    private fun getChannelId(context:Context): String {
        return ensureNotificationChannelExists(
            context,
            NOTIFICATION_CHANNEL_REMIND,
            onSetupChannel = {
                if (SDK_INT >= Build.VERSION_CODES.O) {
                    enableLights(false)
                    enableVibration(false)
                    setShowBadge(false)
                }
            },
        )
    }
    companion object {
        const val NOTIFICATION_TAG =
            "ie.equalit.ceno.browser.notification.ReminderNotification"
        val NOTIFICATION_CHANNEL_REMIND = ChannelData(
            id = "foreground-reminder",
            name = R.string.foreground_notification_reminder_channel,
            importance = IMPORTANCE_LOW,
        )
    }
}