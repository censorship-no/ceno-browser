package ie.equalit.ceno.components

import android.content.Context
import ie.equalit.ceno.metrics.autotracker.AutoTracker
import ie.equalit.ceno.metrics.campaign001.Campaign001
import ie.equalit.ceno.settings.Settings
import mozilla.components.support.base.log.logger.Logger
import org.cleaninsights.sdk.CleanInsights
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Metrics (context: Context) {
    lateinit var autoTracker: AutoTracker
    lateinit var campaign001 : Campaign001

    init {
        if (Settings.shouldBackdateCleanInsights(context)) {
            Logger.debug("Backdating Clean Insights metrics")
            val file = File(context.filesDir, "cleaninsights.json")
            val contents = file.readText()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal: Calendar = Calendar.getInstance()
            val today = dateFormat.format(cal.time)

            cal.add(Calendar.DATE, +1)
            val tomorrow = dateFormat.format(cal.time)

            cal.add(Calendar.DATE, -2)
            val yesterday = dateFormat.format(cal.time)

            cal.add(Calendar.DATE, -1)
            val dayBefore = dateFormat.format(cal.time)

            val backdated = contents
                .replace(yesterday, dayBefore)
                .replace(today, yesterday)
                .replace(tomorrow, today)
            FileOutputStream(file).use {
                it.write(backdated.toByteArray())
            }
            Settings.setBackdateCleanInsights(context, false)
        }
    }

    fun initAutoTracker(cleanInsights: CleanInsights) {
        autoTracker = AutoTracker(cleanInsights)
    }

    fun initCampaign001(cleanInsights: CleanInsights) {
        campaign001 = Campaign001(cleanInsights)
    }
}