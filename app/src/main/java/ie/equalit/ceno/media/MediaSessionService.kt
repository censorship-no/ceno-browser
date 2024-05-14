/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.media

import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.media.service.AbstractMediaSessionService
import ie.equalit.ceno.ext.components
import mozilla.components.support.base.android.NotificationsDelegate

/**
 * [AbstractMediaSessionService] implementation for injecting [BrowserStore] singleton.
 */
class MediaSessionService : AbstractMediaSessionService() {
    override val store: BrowserStore by lazy { components.core.store }
    override val crashReporter = null
    override val notificationsDelegate: NotificationsDelegate by lazy { components.notificationsDelegate }
}
