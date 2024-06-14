/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ie.equalit.ceno.tabs

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import ie.equalit.ceno.R

object FailedToRetrieveResource {
    /**
     * Load and generate a localized error page
     */
    fun createErrorPage(
        context: Context
    ): String {

        return context.resources.openRawResource(R.raw.server500)
            .bufferedReader()
            .use { it.readText() }
            .replace("%body_theme%", if(isDarkThemeEnabled()) "#39393B" else "#FFFFFF")
            .replace("%text_theme%", if(isDarkThemeEnabled()) "#FFFFFF" else "#000000")
            .replace("%page_title%", context.getString(R.string.error_page_title))
            .replace("%error_page_header%", context.getString(R.string.error_page_header))
            .replace("%error_page_sub_header%", context.getString(R.string.error_page_sub_header))
            .replace("%direct_from_website%", context.getString(R.string.ceno_sources_direct_from_website))
            .replace("%using_ceno_network%", context.getString(R.string.ceno_sources_via_ceno_network))
            .replace("%from_ceno_cache%", context.getString(R.string.ceno_sources_shared_by_ceno_cache))
            .replace("%learn_more%", context.getString(R.string.error_page_learn_more))
            .replace("%error_page_learn_more_header%", context.getString(R.string.error_page_learn_more_header))
            .replace("%error_page_learn_more_1%", context.getString(R.string.error_page_learn_more_1))
            .replace("%error_page_learn_more_2%", context.getString(R.string.error_page_learn_more_2))
            .replace("%error_page_learn_more_3%", context.getString(R.string.error_page_learn_more_3))
    }

    private fun isDarkThemeEnabled(): Boolean {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> true
        }
    }
}
