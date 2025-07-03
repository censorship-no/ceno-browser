/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.components

import android.content.Context
import androidx.preference.PreferenceManager
import mozilla.components.feature.app.links.AppLinksInterceptor
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.getPreferenceKey

/**
 * Component group which encapsulates foreground-friendly services.
 */
class Services(
    private val context: Context
) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    val appLinksInterceptor by lazy {
        AppLinksInterceptor(
            context,
            launchInApp = {
                prefs.getBoolean(context.getPreferenceKey(R.string.pref_key_launch_external_app), false)
            },
        )
    }
}
