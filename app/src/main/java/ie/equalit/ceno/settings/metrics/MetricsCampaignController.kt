/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings.metrics

import android.content.Context
import android.widget.Toast
import ie.equalit.ceno.Components
import ie.equalit.ceno.R.string.clean_insights_successful_opt_out
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.settings.Settings.setCleanInsightsEnabled
import ie.equalit.ceno.utils.sentry.SentryOptionsConfiguration
import io.sentry.android.core.SentryAndroid

interface MetricsCampaignController {
    fun crashReporting(newValue: Boolean)
    fun autoTracker(newValue: Boolean, callback : (Boolean) -> Unit)
    fun campaignOne(newValue: Boolean, callback: (Boolean) -> Unit)
    fun campaignTwo()
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

    override fun autoTracker(newValue: Boolean, callback : (Boolean) -> Unit) {
        if (newValue) {
            components.metrics.autoTracker.launchCampaign(context, showLearnMore = false) { granted ->
                callback(granted)
            }
        } else {
            components.metrics.autoTracker.disableCampaign {
                setCleanInsightsEnabled(context, false)
                Toast.makeText(
                    context,
                    context.getString(clean_insights_successful_opt_out),
                    Toast.LENGTH_LONG,
                ).show()
            }
            callback(false)
        }
    }

    override fun campaignOne(newValue: Boolean, callback : (Boolean) -> Unit) {
        if (newValue) {
            components.metrics.campaign001.launchCampaign(context) { granted ->
               callback(granted)
            }
        } else {
            components.metrics.campaign001.disableCampaign {
                setCleanInsightsEnabled(context, false)
                Toast.makeText(
                    context,
                    context.getString(clean_insights_successful_opt_out),
                    Toast.LENGTH_LONG,
                ).show()
            }
            callback(false)
        }
    }

    override fun campaignTwo() {
    }
}