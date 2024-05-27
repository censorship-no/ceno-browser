/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ie.equalit.ceno.tabs

import android.content.Context
import androidx.annotation.RawRes
import ie.equalit.ceno.R

object FailedToRetrieveResource {
    /**
     * Load and generate a private browsing page for the given url and html/css resources
     */
    fun createPrivateBrowsingPage(
        context: Context,
        url: String,
        @RawRes htmlRes: Int = R.raw.private_mode, // to change
        @RawRes cssRes: Int = R.raw.private_style, // to change
    ): String {
        val css = context.resources.openRawResource(cssRes).bufferedReader().use {
            it.readText()
        }

        return context.resources.openRawResource(htmlRes)
            .bufferedReader()
            .use { it.readText() }
            .replace("%pageTitle%", context.getString(R.string.error_page_title))
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
}
