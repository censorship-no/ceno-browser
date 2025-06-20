/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings.metrics

import android.content.Context
import android.util.Log
import ie.equalit.ceno.Components
import ie.equalit.ceno.settings.CenoSettings
import ie.equalit.ceno.settings.OuinetKey
import ie.equalit.ceno.settings.OuinetResponseListener
import ie.equalit.ceno.settings.OuinetValue
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.utils.sentry.SentryOptionsConfiguration
import io.sentry.android.core.SentryAndroid

interface MetricsCampaignController {
    fun crashReporting(newValue: Boolean)
    fun ouinetMetrics(newValue: Boolean)
}

@Suppress("LongParameterList")
class DefaultMetricsCampaignController(
    private var context: Context,
    private val components: Components
) : MetricsCampaignController {

    override fun crashReporting(newValue: Boolean) {
        Settings.setCrashReportingPermissionValue(
            context,
            newValue
        )

        // Re-initialize Sentry-Android
        SentryAndroid.init(
            context,
            SentryOptionsConfiguration.getConfig(context)
        )

        // Re-allow post-crash permissions nudge
        // This should ALWAYS be turned on when this permission state is toggled
        Settings.toggleCrashReportingPermissionNudge(
            context,
            true
        )
    }

    override fun ouinetMetrics(newValue: Boolean) {
        //web api call
        CenoSettings.ouinetClientRequest(
            context = context,
            key = OuinetKey.CENO_METRICS,
            newValue = if (newValue) OuinetValue.ENABLE else OuinetValue.DISABLE,
            stringValue = null,
            object : OuinetResponseListener {
                override fun onSuccess(message: String, data: Any?) {
                    Settings.setOuinetMetricsEnabled(context, newValue)
                }

                override fun onError() {
                    Log.e("Ouinet-Metrics", "Failed to set metrics to newValue: $newValue")
                }
            },
            forMetrics = true
        )
    }
}