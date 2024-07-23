package ie.equalit.ceno.utils.sentry

import android.content.Context
import ie.equalit.ceno.settings.Settings
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroidOptions

object SentryOptionsConfiguration {

    /**
     * This extension function returns the default configuration for the Ceno browser
     */

    fun getConfig(context: Context): Sentry.OptionsConfiguration<SentryAndroidOptions> {

        // The snippet below makes sure the DEFAULT value of the crash report permission is never true
        if (!Settings.isCrashReportingPermissionGranted(context)) {
            Settings.setCrashReportingPermissionValue(context, false)
        }

        return Sentry.OptionsConfiguration<SentryAndroidOptions> {
            it.dsn = "https://313d457370cee4729e88117e6096c042@sentry.ouinet.work/5"
            it.isEnableUserInteractionTracing = true
            it.isAttachScreenshot = false
            it.isAttachViewHierarchy = true
            it.sampleRate = 1.0
            it.profilesSampleRate = 1.0
            it.isAnrEnabled = false
            it.addEventProcessor(SentryEventProcessor(context))
        }
    }

}