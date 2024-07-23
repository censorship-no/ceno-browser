/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.utils

//import mozilla.components.service.contile.ContileTopSitesProvider
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.ext.getPreferenceKey
import mozilla.components.feature.sitepermissions.SitePermissionsRules.Action
import mozilla.components.feature.sitepermissions.SitePermissionsRules.AutoplayAction
import mozilla.components.support.ktx.android.content.PreferencesHolder
import mozilla.components.support.ktx.android.content.booleanPreference
import mozilla.components.support.ktx.android.content.intPreference
import java.security.InvalidParameterException

/**
 * A simple wrapper for SharedPreferences that makes reading preference a little bit easier.
 * @param appContext Reference to application context.
 */
@Suppress("LargeClass", "TooManyFunctions")
class CenoPreferences(private val appContext: Context) : PreferencesHolder {

    companion object {
        const val CENO_PREFERENCES = "ceno_preferences"

        private const val BLOCKED_INT = 0
        private const val ASK_TO_ALLOW_INT = 1
        private const val ALLOWED_INT = 2
        private const val CFR_COUNT_CONDITION_FOCUS_INSTALLED = 1
        private const val CFR_COUNT_CONDITION_FOCUS_NOT_INSTALLED = 3
        private const val INACTIVE_TAB_MINIMUM_TO_SHOW_AUTO_CLOSE_DIALOG = 20

        const val FOUR_HOURS_MS = 60 * 60 * 4 * 1000L
        const val ONE_DAY_MS = 60 * 60 * 24 * 1000L
        const val THREE_DAYS_MS = 3 * ONE_DAY_MS
        const val ONE_WEEK_MS = 60 * 60 * 24 * 7 * 1000L
        const val ONE_MONTH_MS = (60 * 60 * 24 * 365 * 1000L) / 12

        /**
         * The minimum number a search groups should contain.
         * Filtering is applied depending on the [historyImprovementFeatures] flag value.
         */
        const val SEARCH_GROUP_MINIMUM_SITES: Int = 2

        // The maximum number of top sites to display.
        const val TOP_SITES_MAX_COUNT = 16

        /**
         * Only fetch top sites from the [ContileTopSitesProvider] when the number of default and
         * pinned sites are below this maximum threshold.
         */
        const val TOP_SITES_PROVIDER_MAX_THRESHOLD = 8

        private fun Action.toInt() = when (this) {
            Action.BLOCKED -> BLOCKED_INT
            Action.ASK_TO_ALLOW -> ASK_TO_ALLOW_INT
            Action.ALLOWED -> ALLOWED_INT
        }

        private fun AutoplayAction.toInt() = when (this) {
            AutoplayAction.BLOCKED -> BLOCKED_INT
            AutoplayAction.ALLOWED -> ALLOWED_INT
        }

        private fun Int.toAction() = when (this) {
            BLOCKED_INT -> Action.BLOCKED
            ASK_TO_ALLOW_INT -> Action.ASK_TO_ALLOW
            ALLOWED_INT -> Action.ALLOWED
            else -> throw InvalidParameterException("$this is not a valid SitePermissionsRules.Action")
        }

        private fun Int.toAutoplayAction() = when (this) {
            BLOCKED_INT -> AutoplayAction.BLOCKED
            ALLOWED_INT -> AutoplayAction.ALLOWED
            // Users from older versions may have saved invalid values. Migrate them to BLOCKED
            ASK_TO_ALLOW_INT -> AutoplayAction.BLOCKED
            else -> throw InvalidParameterException("$this is not a valid SitePermissionsRules.AutoplayAction")
        }
    }

    override val preferences: SharedPreferences =
        appContext.getSharedPreferences(CENO_PREFERENCES, MODE_PRIVATE)


    var defaultTopSitesAdded by booleanPreference(
        appContext.getPreferenceKey(R.string.default_top_sites_added),
        default = false
    )

    val topSitesMaxLimit by intPreference(
        appContext.getPreferenceKey(R.string.pref_key_top_sites_max_limit),
        default = TOP_SITES_MAX_COUNT
    )

    var sharedPrefsReload by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_shared_prefs_reload),
        default = false
    )

    var sharedPrefsUpdate by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_shared_prefs_update),
        default = false
    )

    var showBridgeAnnouncementCard by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_bridge_announcement),
        default = true
    )

    var isBridgeCardExpanded by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_bridge_card_expanded),
        default = true
    )

    var isModeCardExpanded by booleanPreference(
        appContext.getPreferenceKey(R.string.pref_key_mode_card_expanded),
        default = true
    )

    /**
     * Save browsing mode in preferences
     * From Fenix
     */
    var lastKnownBrowsingMode: BrowsingMode = BrowsingMode.Normal
        get() {
            val lastKnownModeWasPersonal = preferences.getBoolean(
                appContext.getPreferenceKey(R.string.pref_last_known_browsing_mode_personal),
                false,
            )

            return if (lastKnownModeWasPersonal) {
                BrowsingMode.Personal
            } else {
                BrowsingMode.Normal
            }
        }
        set(value) {
            val lastKnownModeWasPersonal = (value == BrowsingMode.Personal)

            preferences.edit()
                .putBoolean(
                    appContext.getPreferenceKey(R.string.pref_last_known_browsing_mode_personal),
                    lastKnownModeWasPersonal,
                )
                .apply()

            field = value
        }
}