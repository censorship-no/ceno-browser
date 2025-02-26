package ie.equalit.ceno.browser.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.ifChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.selector.normalTabs
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.ids.SharedIdsHelper
import mozilla.components.support.ktx.android.notification.ChannelData
import mozilla.components.support.ktx.android.notification.ensureNotificationChannelExists
import mozilla.components.support.utils.ext.stopForegroundCompat
import java.util.Locale

abstract class AbstractPublicNotificationService : Service() {

    private var publicTabScope: CoroutineScope? = null
    private var localeScope: CoroutineScope? = null

    abstract val store: BrowserStore

    /**
     * Customizes the public browsing notification.
     */
    abstract fun NotificationCompat.Builder.buildNotification(showConfirmAction: Boolean)

    /**
     * Customize the notification response when the [Locale] has been changed.
     */
    abstract fun notifyLocaleChanged()

    /**
     * Retrieves the notification id based on the tag.
     */
    protected fun getNotificationId(): Int {
        return SharedIdsHelper.getIdForTag(this, NOTIFICATION_TAG)
    }

    /**
     * Retrieves the channel id based on the channel data.
     */
    protected fun getChannelId(): String {
        return ensureNotificationChannelExists(
            this,
            NOTIFICATION_CHANNEL,
            onSetupChannel = {
                if (SDK_INT >= Build.VERSION_CODES.O) {
                    enableLights(false)
                    enableVibration(false)
                    setShowBadge(false)
                }
            },
        )
    }

    /**
     * Re-build and notify an existing notification.
     */
    protected fun refreshNotification(showConfirmAction:Boolean = false) {
        val notificationId = getNotificationId()
        val channelId = getChannelId()

        val notification = createNotification(channelId, showConfirmAction)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //do nothing if permission not granted
            return
        }
        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
    }

    /**
     * Create the private browsing notification and
     * add a listener to stop the service once all private tabs are closed.
     *
     * The service should be started only if private tabs are open.
     */
    @SuppressLint("ForegroundServiceType")
    final override fun onCreate() {
        val notificationId = getNotificationId()
        val channelId = getChannelId()
        val notification = createNotification(channelId)

        startForeground(notificationId, notification)

        publicTabScope = store.flowScoped { flow ->
            flow.map { state -> state.normalTabs.isEmpty() }
                .ifChanged()
                .collect { noPublicTabs ->
                    if (noPublicTabs) stopService()
                }
        }

        localeScope = store.flowScoped { flow ->
            flow.mapNotNull { state -> state.locale }
                .ifChanged()
                .collect {
                    notifyLocaleChanged()
                }
        }
    }

    /**
     * Builds a notification based on the specified channel id.
     *
     * @param channelId The channel id for the [Notification]
     */
    fun createNotification(channelId: String, showConfirmAction:Boolean = false): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setOngoing(true)
            .setVisibility(VISIBILITY_SECRET)
            .setShowWhen(false)
            .setLocalOnly(true)
            .apply { buildNotification(showConfirmAction) }
            .build()
    }

    final override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == ACTION_CONFIRM) {
            refreshNotification(true)
        }
        if (intent.action == ACTION_CANCEL) {
            refreshNotification(false)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        publicTabScope?.cancel()
        localeScope?.cancel()
    }

    final override fun onBind(intent: Intent?): IBinder? = null

    private fun stopService() {
        stopForegroundCompat(true)
        stopSelf()
    }

    companion object {
        private const val NOTIFICATION_TAG =
            "ie.equalit.ceno.browser.notification.AbstractPublicNotificationService"
        const val ACTION_STOP = "ie.equalit.ceno.browser.notification.action.ERASE"
        const val ACTION_CLEAR = "ie.equalit.ceno.browser.notification.action.CLEAR"
        const val ACTION_TAP = "ie.equalit.ceno.browser.notification.action.TAP"
        const val ACTION_CONFIRM = "ie.equalit.ceno.browser.notification.action.CONFIRM"
        const val ACTION_CANCEL = "ie.equalit.ceno.browser.notification.action.CANCEL"

        val NOTIFICATION_CHANNEL = ChannelData(
            id = "browsing-session",
            name = R.string.ceno_notification_channel_name,
            importance = IMPORTANCE_LOW,
        )
    }
}