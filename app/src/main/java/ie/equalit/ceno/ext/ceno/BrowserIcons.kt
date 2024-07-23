/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.ext.ceno

import android.widget.ImageView
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.icons.IconRequest

/**
 * CENO: Ext to BrowserIcons for CENO Home TopSiteItemViewHolder
 */
fun BrowserIcons.loadIntoView(view: ImageView, url: String) = loadIntoView(view, IconRequest(url))
