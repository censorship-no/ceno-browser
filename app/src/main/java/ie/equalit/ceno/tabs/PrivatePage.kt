/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ie.equalit.ceno.tabs

import android.content.Context
import androidx.annotation.RawRes
import ie.equalit.ceno.R

object PrivatePage {
    /**
     * Load and generate a private browsing page for the given url and html/css resources
     */
    fun createPrivateBrowsingPage(
        context: Context,
        url: String,
        @RawRes htmlRes: Int = R.raw.private_mode,
        @RawRes cssRes: Int = R.raw.private_style,
    ): String {
        val css = context.resources.openRawResource(cssRes).bufferedReader().use {
            it.readText()
        }

        return context.resources.openRawResource(htmlRes)
            .bufferedReader()
            .use { it.readText() }
            .replace("%pageTitle%", context.getString(R.string.private_browsing_title))
            /* CENO: Change the text being displayed on the about:privatebrowsing page */
            .replace("%pageBody%", context.getString(R.string.ceno_private_browsing_body))
            .replace(
                "%privateBrowsingSupportUrl%",
                context.getString(R.string.ceno_mode_manual_link)
            )
            .replace("%css%", css)
    }
}
