package ie.equalit.ceno.metrics.campaign001

import android.content.Context
import androidx.preference.PreferenceManager
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.isDatePast
import ie.equalit.ceno.settings.Settings
import org.cleaninsights.sdk.CleanInsights
import org.cleaninsights.sdk.Feature
import androidx.core.content.edit

class Campaign001(private val cleanInsights: CleanInsights) {

    fun launchCampaign(context : Context, callback : (Boolean) -> Unit) {

        val dialog = ConsentRequestDialog(context)

        dialog.show() { granted ->
            if (granted) {
                cleanInsights.grant(ID)
                if (Settings.isCleanInsightsDeviceTypeIncluded(context)) {
                    cleanInsights.grant(Feature.Ua)
                }
                if (Settings.isCleanInsightsDeviceLocaleIncluded(context)) {
                    cleanInsights.grant(Feature.Lang)
                }
            }
            else {
                cleanInsights.deny(ID)
            }
            Settings.setCleanInsightsEnabled(context, granted)
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

    fun promptSurvey(context : Context, callback : (Double) -> Unit) {

        val dialog = StartupSurveyDialog(context)

        dialog.show() { value ->
            cleanInsights.measureEvent(
                category = "user-feedback",
                action = "ouinet-startup-success",
                campaignId = ID,
                name = "perceived_ouinet_startup_time",
                value = value
            )
            callback.invoke(value)
        }
    }

    fun disableCampaign(callback : () -> Unit) {
        cleanInsights.deny(ID)
        callback.invoke()
    }

    fun setPromptCompleted(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_ci_campaign001_prompt_completed)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    fun isPromptCompleted(context: Context) : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_ci_campaign001_prompt_completed), false
        )
    }

    fun setSurveyCompleted(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_ci_campaign001_survey_completed)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    fun isSurveyCompleted(context: Context) : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_ci_campaign001_survey_completed), false
        )
    }

    fun isExpired() : Boolean {
       return EXPIRATION_DATE.isDatePast()
    }

    companion object {
        const val ID = "ouinet-startup-time"
        const val ASK_FOR_ANALYTICS_LIMIT = 5
        const val ASK_FOR_SURVEY_LIMIT = 10
        const val EXPIRATION_DATE = "2025-01-31 23:59:59"
    }
}