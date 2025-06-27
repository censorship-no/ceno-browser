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
import androidx.core.content.edit
import ie.equalit.ceno.ext.isFirstInstall
import ie.equalit.ceno.home.ouicrawl.OuicrawlSite
import ie.equalit.ceno.home.ouicrawl.OuicrawledSitesListItem
import kotlinx.serialization.json.Json

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

    fun shouldShowConsentDialog(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_show_metrics_consent_dialog), false
        )

    fun shouldBackdateCleanInsights(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_clean_insights_backdate), false
        )

    fun setUpdateSearchEngines(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_update_search_engines)
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit() {
                    putBoolean(key, value)
            }
    }

    fun setShowOnboarding(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_show_onboarding)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }
    fun setAllowNotifications(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_allow_notifications)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    fun setShowStandbyWarning(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_show_standby_warning)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    fun setShowDeveloperTools(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_show_developer_tools)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    fun setBackdateCleanInsights(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_clean_insights_backdate)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    fun setAppIcon(context: Context, value: String?) {
        val key = context.getString(R.string.pref_key_selected_app_icon)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putString(key, value)
            }
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
            .edit() {
                putBoolean(key, value)
            }
    }

    fun setDeleteBrowsingHistory(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_delete_browsing_history)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    fun setDeleteCookies(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_delete_cookies_now)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    fun setDeleteCache(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_delete_cache_now)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }
    fun setDeleteSitePermissions(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_delete_site_permissions)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
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
            .edit() {
                putBoolean(key, value)
            }
    }

    fun setCrashReportingPermissionValue(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_allow_crash_reporting)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }


    fun setCrashHappened(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_crash_happened)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    // duplicate function that uses commit() instead of apply()
    // This is necessary for the purpose of immediately saving crash logs locally when a crash happens
    fun setCrashHappenedCommit(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_crash_happened)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit(commit = true) {
                putBoolean(key, value)
            }
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
            .edit() {
                putBoolean(key, value)
            }
    }

    // duplicate function that uses commit() instead of apply()
    // This is necessary for the purpose of immediately saving crash logs locally when a crash happens

    fun logSuccessfulCrashEventCommit(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_crash_was_logged)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit(commit = true) {
                putBoolean(key, value)
            }
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
            .edit() {
                putString(key, list.joinToString(" "))
            }
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
            .edit() {
                putString(key, Gson().toJson(announcementData))
            }
    }

    fun clearAnnouncementData(context: Context) {
        val key = context.getString(R.string.pref_key_rss_announcement_data)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                remove(key)
            }
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

    fun isOuinetMetricsEnabled(context: Context) : Boolean {
        /*
        if (context.isFirstInstall()) {
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                context.getString(R.string.pref_key_metrics_ouinet), true
            )
        }
        */
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_metrics_ouinet), false
        )
    }

    fun setOuinetMetricsEnabled(context: Context, newValue:Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(context.getString(R.string.pref_key_metrics_ouinet), newValue)
            }
    }

    fun saveOuicrawlData(context: Context, ouicrawlData: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putString(context.getString(R.string.pref_key_ouicrawl_data), ouicrawlData)
            }
    }

    fun getOuicrawlData(context: Context) : List<OuicrawlSite>? {
        val ouicrawlData = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_ouicrawl_data), null)
        ouicrawlData?.let { response ->
            var ouicrawlSites = Json.decodeFromString<OuicrawledSitesListItem>(response).Sites
            //filter by
            return ouicrawlSites
        }
        return null
    }
}
