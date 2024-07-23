/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.home.topsites

import ie.equalit.ceno.ext.ceno.sort
import ie.equalit.ceno.utils.CenoPreferences
import mozilla.components.feature.top.sites.TopSite
import mozilla.components.feature.top.sites.view.TopSitesView

class DefaultTopSitesView(
    val settings: CenoPreferences
) : TopSitesView {

    override fun displayTopSites(topSites: List<TopSite>) {
        topSites.sort()
    }
}
