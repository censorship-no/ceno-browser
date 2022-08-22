/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.cenoV2.components.ceno.appstate

import mozilla.components.feature.top.sites.TopSite
import mozilla.components.lib.state.Action
import ie.equalit.cenoV2.components.ceno.AppStore

/** CENO: Ported from Fenix, significantly stripped down
 *  since TopSites is the only currently supported AppState
 * [Action] implementation related to [AppStore].
 */
sealed class AppAction : Action {

    data class Change(
        val topSites: List<TopSite>,
    ) :
        AppAction()

    data class TopSitesChange(val topSites: List<TopSite>) : AppAction()
}