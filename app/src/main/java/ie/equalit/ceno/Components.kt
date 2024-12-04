/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.components.Core
import ie.equalit.ceno.components.Metrics
import ie.equalit.ceno.components.Ouinet
import ie.equalit.ceno.components.PermissionHandler
import ie.equalit.ceno.components.Services
import ie.equalit.ceno.components.UseCases
import ie.equalit.ceno.components.Utilities
import ie.equalit.ceno.components.WebExtensionPort
import ie.equalit.ceno.components.ceno.AppStore
import ie.equalit.ceno.components.ceno.appstate.AppState
import ie.equalit.ceno.ext.ceno.sort
import ie.equalit.ceno.utils.CenoPreferences
import mozilla.components.support.base.android.NotificationsDelegate
import org.cleaninsights.sdk.CleanInsights

/**
 * Provides access to all components.
 */
class Components(private val context: Context) {
    val core by lazy { Core(context) }
    val useCases by lazy {
        UseCases(
            context,
            core.engine,
            core.store,
            core.shortcutManager,
            core.cenoTopSitesStorage
        )
    }

    val utils by lazy {
        Utilities(
            context,
            core.store,
            useCases.sessionUseCases,
            useCases.searchUseCases,
            useCases.tabsUseCases,
            useCases.customTabsUseCases
        )
    }
    val services by lazy { Services(context) }
    /* CENO F-Droid: Do not use firebase push */
    //val push by lazy { Push(context, analytics.crashReporter) }

    private val notificationManagerCompat = NotificationManagerCompat.from(context)

    val notificationsDelegate: NotificationsDelegate by lazy {
        NotificationsDelegate(
            notificationManagerCompat,
        )
    }

    /* CENO: Allow access to CENO SharedPreference wrapper through components*/
    val cenoPreferences by lazy { CenoPreferences(context) }

    /* CENO: Initialize AppStore with cached top sites */
    val appStore by lazy {
        AppStore(
            initialState = AppState(
                topSites = core.cenoTopSitesStorage.cachedTopSites.sort(),
                mode = BrowsingMode.Normal
            )
        )
    }
    val ouinet by lazy { Ouinet(context) }
    val permissionHandler by lazy { PermissionHandler(context) }

    val webExtensionPort by lazy { WebExtensionPort(context) }

    val metrics by lazy { Metrics(context) }
}
