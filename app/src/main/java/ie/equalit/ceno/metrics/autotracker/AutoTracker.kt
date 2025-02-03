package ie.equalit.ceno.metrics.autotracker

import android.content.Context
import androidx.preference.PreferenceManager
import ie.equalit.ceno.R
import ie.equalit.ceno.settings.Settings
import org.cleaninsights.sdk.CleanInsights
import org.cleaninsights.sdk.Feature

class AutoTracker(private val cleanInsights: CleanInsights) {

    fun launchCampaign(context : Context, showLearnMore: Boolean, callback : (Boolean) -> Unit) {

        val dialog = ConsentRequestDialog(context, showLearnMore)

        dialog.show() { granted ->
            if (granted) {
                cleanInsights.grant(ID)
                if (Settings.isCleanInsightsDeviceTypeIncluded(context)) {
                    cleanInsights.grant(Feature.Ua)
                }
                else {
                    cleanInsights.deny(Feature.Ua)
                }
                if (Settings.isCleanInsightsDeviceLocaleIncluded(context)) {
                    cleanInsights.grant(Feature.Lang)
                }
                else {
                    cleanInsights.deny(Feature.Lang)
                }
            }
            else {
                cleanInsights.deny(ID)
            }
            Settings.setMetricsAutoTrackerEnabled(context, granted)
            callback.invoke(granted)
        }
    }

    fun measureEvent(startupTime: Double) {
        cleanInsights.measureEvent(
            category = "app-state",
            action = "ouinet-startup-success",
            campaignId = ID,
            name = "actual_ouinet_startup_time",
            value = startupTime
        )
    }

    fun measureVisit(scenePath : List<String>) {
        cleanInsights.measureVisit(
            scenePath = scenePath,
            campaignId = ID,
        )
    }

    fun disableCampaign(callback : () -> Unit) {
        cleanInsights.deny(ID)
        callback.invoke()
    }

    fun setPromptCompleted(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_metrics_autotracker_prompt_completed)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun isPromptCompleted(context: Context) : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_metrics_autotracker_prompt_completed), false
        )
    }

    companion object {
        const val ID = "visits"
        const val ASK_FOR_ANALYTICS_LIMIT = 5
    }
}