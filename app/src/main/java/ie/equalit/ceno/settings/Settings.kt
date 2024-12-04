/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings

import android.annotation.SuppressLint
import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.isDateMoreThanXDaysAway
import ie.equalit.ceno.home.RssAnnouncementResponse
import ie.equalit.ceno.settings.changeicon.appicons.AppIcon

object Settings {
    fun shouldShowOnboarding(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_show_onboarding), false
        )

    fun shouldShowStandbyWarning(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_show_standby_warning), false
        )

    fun shouldShowHomeButton(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_show_home_button), false
        )

    fun shouldShowSearchSuggestions(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_show_search_suggestions), false
        )

    fun shouldUpdateSearchEngines(context: Context): Boolean =
            PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                    context.getString(R.string.pref_key_update_search_engines), false
            )

    fun shouldShowDeveloperTools(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_show_developer_tools), false
        )

    fun shouldBackdateCleanInsights(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_clean_insights_backdate), false
        )

    fun setUpdateSearchEngines(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_update_search_engines)
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(key, value)
                .apply()
    }

    fun setShowOnboarding(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_show_onboarding)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }
    fun setAllowNotifications(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_allow_notifications)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun setShowStandbyWarning(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_show_standby_warning)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun setShowDeveloperTools(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_show_developer_tools)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun setBackdateCleanInsights(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_clean_insights_backdate)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun setAppIcon(context: Context, value: String?) {
        val key = context.getString(R.string.pref_key_selected_app_icon)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(key, value)
            .apply()
    }

    fun appIcon(context: Context) : AppIcon? {
        val componentName =  PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_selected_app_icon), AppIcon.DEFAULT.componentName
        )
        return componentName?.let { AppIcon.from(it) }
    }

    fun getAppTheme(context: Context) : Int {
        val themeString = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.pref_key_theme), context.getString(R.string.preferences_theme_default)
        )
        return themeString!!.toInt()
    }

    fun deleteOpenTabs(context: Context) : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_delete_open_tabs), false
        )
    }

    fun deleteBrowsingHistory(context: Context) : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_delete_browsing_history), false
        )
    }

    fun deleteCookies(context: Context) : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_delete_cookies_now), false
        )
    }

    fun deleteCache(context: Context) : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_delete_cache_now), false
        )
    }

    fun deleteSitePermissions(context: Context) : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_delete_site_permissions), false
        )
    }

    fun setDeleteOpenTabs(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_delete_open_tabs)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun setDeleteBrowsingHistory(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_delete_browsing_history)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun setDeleteCookies(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_delete_cookies_now)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun setDeleteCache(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_delete_cache_now)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }
    fun setDeleteSitePermissions(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_delete_site_permissions)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun showCrashReportingPermissionNudge(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_crash_happened), false
        ) && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_show_crash_reporting_permission), true
        )

    fun toggleCrashReportingPermissionNudge(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_show_crash_reporting_permission)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun setCrashReportingPermissionValue(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_allow_crash_reporting)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun getLaunchCount(context: Context) : Long {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(
            context.getString(R.string.pref_key_app_launch_count), 0
        )
    }

    fun incrementLaunchCount(context: Context) {
        val key = context.getString(R.string.pref_key_app_launch_count)
        var currentValue = getLaunchCount(context)
        if (currentValue == Long.MAX_VALUE) currentValue = 0
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putLong(key, currentValue + 1)
            .apply()
    }

    fun setCleanInsightsEnabled(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_clean_insights_enabled)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun isCleanInsightsEnabled(context: Context) : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_clean_insights_enabled), false
        )
    }

    fun setCleanInsightsDeviceType(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_clean_insights_include_device_type)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun isCleanInsightsDeviceTypeIncluded(context: Context) : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_clean_insights_include_device_type), false
        )
    }

    fun setCleanInsightsDeviceLocale(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_clean_insights_include_device_locale)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    fun isCleanInsightsDeviceLocaleIncluded(context: Context) : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_clean_insights_include_device_locale), false
        )
    }

    fun setCrashHappened(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_crash_happened)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    // duplicate function that uses commit() instead of apply()
    // This is necessary for the purpose of immediately saving crash logs locally when a crash happens
    @SuppressLint("ApplySharedPref")
    fun setCrashHappenedCommit(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_crash_happened)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .commit()
    }

    fun isCrashReportingPermissionGranted(context: Context) : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_allow_crash_reporting), false
        )
    }

    fun alwaysAllowCrashReporting(context: Context) {
        setCrashHappened(context, false) // reset the value of lastCrash
        setCrashReportingPermissionValue(context, true)
    }

    fun neverAllowCrashReporting(context: Context) {
        setCrashHappened(context, false) // reset the value of lastCrash
        toggleCrashReportingPermissionNudge(context, false)
        setCrashReportingPermissionValue(context, false)
    }

    fun wasCrashSuccessfullyLogged(context: Context) : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_crash_was_logged), false
        )
    }

    fun logSuccessfulCrashEvent(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_crash_was_logged)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    // duplicate function that uses commit() instead of apply()
    // This is necessary for the purpose of immediately saving crash logs locally when a crash happens
    @SuppressLint("ApplySharedPref")
    fun logSuccessfulCrashEventCommit(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_crash_was_logged)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .commit()
    }

    fun isAnnouncementExpirationDisabled(context: Context) : Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_rss_announcement_expire_disable), false
        )
    }

    fun getSwipedAnnouncementGuids(context: Context): List<String>? {
        val guids = PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_rss_past_announcement_data), null
        ) ?: return null

        return guids.split(" ")
    }

    fun addSwipedAnnouncementGuid(context: Context, guid : String) {
        val key = context.getString(R.string.pref_key_rss_past_announcement_data)

        val list = (getSwipedAnnouncementGuids(context)?.toMutableList() ?: mutableListOf())
        list.add(guid)

        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(key, list.joinToString(" "))
            .apply()
    }

    fun getAnnouncementData(context: Context) : RssAnnouncementResponse? {
        val localValue = PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_rss_announcement_data), null
        )

        val swipedGuids = getSwipedAnnouncementGuids(context)

        Gson().fromJson(localValue, RssAnnouncementResponse::class.java)?.let { rssAnnouncementResponse ->

            val response = RssAnnouncementResponse(
                title = rssAnnouncementResponse.title,
                link = rssAnnouncementResponse.link,
                text = rssAnnouncementResponse.text,
                items = buildList {
                    rssAnnouncementResponse.items.forEach {
                        val pubDate : String = it.guid.split("/")[1]
                        val isExpired = pubDate.isDateMoreThanXDaysAway(30) && !isAnnouncementExpirationDisabled(context)
                        if((swipedGuids == null || !swipedGuids.contains(it.guid)) && !isExpired) {
                            add(it)
                        }
                    }
                }
            )
            return if(response.items.isEmpty()) null else response

        }

        return null

    }

    fun saveAnnouncementData(context: Context, announcementData: RssAnnouncementResponse?) {
        val key = context.getString(R.string.pref_key_rss_announcement_data)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(key, Gson().toJson(announcementData))
            .apply()
    }

    fun clearAnnouncementData(context: Context) {
        val key = context.getString(R.string.pref_key_rss_announcement_data)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .remove(key)
            .apply()
    }

    fun getRSSAnnouncementUrl(context: Context, locale : String) :String {
        val baseUrl = context.getString(R.string.ceno_base_url)
        val source = PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_rss_announcement_source), context.getString(R.string.preferences_announcement_source_default)
        )
        return when (source) {
            "1" -> "${baseUrl}/${locale}/rss-announce.xml"
            "2" -> "${baseUrl}/${locale}/rss-announce-draft.xml"
            "3" -> "${baseUrl}/${locale}/rss-announce-archive.xml"
            else -> "${baseUrl}/${locale}/rss-announce.xml"
        }
    }

}
