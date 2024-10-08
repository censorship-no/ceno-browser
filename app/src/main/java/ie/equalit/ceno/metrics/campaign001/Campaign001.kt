package ie.equalit.ceno.metrics.campaign001

import android.content.Context
import ie.equalit.ceno.settings.Settings
import org.cleaninsights.sdk.CleanInsights
import org.cleaninsights.sdk.Feature

class Campaign001(private val cleanInsights: CleanInsights, private val campaignId: String) {

    fun launchCampaign(context : Context, callback : (Boolean) -> Unit) {

        val dialog = ConsentRequestDialog(context)

        dialog.show() { granted ->
            if (granted) {
                cleanInsights.grant(campaignId)
                if (Settings.isCleanInsightsDeviceTypeIncluded(context)) {
                    cleanInsights.grant(Feature.Ua)
                }
                if (Settings.isCleanInsightsDeviceLocaleIncluded(context)) {
                    cleanInsights.grant(Feature.Lang)
                }
            }
            else {
                cleanInsights.deny(campaignId)
            }
            Settings.setCleanInsightsEnabled(context, granted)
            callback.invoke(granted)
        }
    }

    fun measureEvent(startupCount: Long, startupTime: Double) {
        cleanInsights.measureEvent(
            category = "app-state",
            action = "ouinet-startup-success",
            campaignId = campaignId,
            name = "app_startup_count",
            value = startupCount.toDouble()
        )
        cleanInsights.measureEvent(
            category = "app-state",
            action = "ouinet-startup-success",
            campaignId = campaignId,
            name = "actual_ouinet_startup_time",
            value = startupTime
        )
    }

    fun promptSurvey(context : Context, callback : (Double) -> Unit) {

        val dialog = StartupSurveyDialog(context)

        dialog.show() { value ->
            cleanInsights.measureEvent(
                category = "user-feedback",
                action = "ouinet-startup-success",
                campaignId = campaignId,
                name = "perceived_ouinet_startup_time",
                value = value
            )
            callback.invoke(value)
        }
    }

    fun disableCampaign(callback : () -> Unit) {
        cleanInsights.deny(campaignId)
        callback.invoke()
    }

}