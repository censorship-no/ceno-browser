package ie.equalit.ceno.browser.notification

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.components
import ie.equalit.ouinet.OuinetNotification.Companion.MILLISECOND
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.support.utils.PendingIntentUtils

class PublicNotificationService:AbstractPublicNotificationService() {
    override val store: BrowserStore by lazy { components.core.store }
    private val handler : Handler = Handler(Looper.myLooper()!!)

    override fun NotificationCompat.Builder.buildNotification(showConfirmAction:Boolean) {
        setSmallIcon(R.drawable.ic_notification)
        setContentTitle(
            getString(R.string.ceno_notification_title)
        )
        setContentText(
            if (showConfirmAction) {
                getString(R.string.ceno_notification_confirm_description)
            } else {
                getString(R.string.ceno_notification_description)
            }
        )
        color = ContextCompat.getColor(
            this@PublicNotificationService,
            R.color.ceno_blue_500,
        )
        setContentIntent(getTapIntent())

        //Adds confirm button to be shown for 3 seconds when clear button is tapped
        //Clear app data and close Ceno if confirm is tapped
        if(showConfirmAction) {
            addAction(R.drawable.ic_notification, getString(R.string.ceno_notification_clear_do_description).uppercase(), getClearIntent())
            addAction(R.drawable.ic_notification, getString(R.string.dialog_cancel).uppercase(), getCancelIntent())
            handler.postDelayed(
                showConfirmCallback,
                5 * MILLISECOND
            )
        } else {
            //Adds stop button. Stops Ceno on tap
            addAction(R.drawable.ic_notification, getString(R.string.browser_menu_stop).uppercase(), getStopIntent())
            //Adds clear button. Shows the confirm button on tap
            addAction(R.drawable.ic_clear_ceno, getString(R.string.ceno_clear_action_description).uppercase(), getConfirmIntent())
        }
    }

    private fun getTapIntent(): PendingIntent? {
        val intent = Intent(this, BrowserActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            action = ACTION_TAP
        }
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    override fun notifyLocaleChanged() {
        super.refreshNotification(false)
    }

    private fun getStopIntent():PendingIntent {
        Intent().also { intent ->
            intent.action = ACTION_STOP
            intent.setPackage(packageName)
            return PendingIntent.getBroadcast(
                this.applicationContext,
                0,
                intent,
                getFlags()
            )
        }
    }

    private fun getClearIntent():PendingIntent {
        Intent().also { intent ->
            intent.action = ACTION_CLEAR
            intent.setPackage(packageName)
            return PendingIntent.getBroadcast(
                this.applicationContext,
                0,
                intent,
                getFlags()
            )
        }
    }

    private fun getConfirmIntent() :PendingIntent{
        return Intent(ACTION_CONFIRM).let {
            it.setClass(this, this::class.java)
            PendingIntent.getService(this, 0, it, PendingIntentUtils.defaultFlags or FLAG_ONE_SHOT)
        }
    }

    private fun getCancelIntent() :PendingIntent{
        return Intent(ACTION_CANCEL).let {
            it.setClass(this, this::class.java)
            PendingIntent.getService(this, 0, it, PendingIntentUtils.defaultFlags or FLAG_ONE_SHOT)
        }
    }

    private fun getFlags() : Int {
        var flags = PendingIntent.FLAG_CANCEL_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags = flags or PendingIntent.FLAG_IMMUTABLE
        }
        return flags
    }

    private val showConfirmCallback = Runnable {
        refreshNotification(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(showConfirmCallback)
    }
}