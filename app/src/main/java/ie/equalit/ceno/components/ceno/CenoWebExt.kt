package ie.equalit.ceno.components.ceno
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import mozilla.components.concept.engine.webextension.WebExtensionRuntime
import mozilla.components.support.base.log.logger.Logger

/**
 * Feature to enable CENO Settings extension.
 */
object CenoWebExt {
    private val logger = Logger("equalitie-ceno")

    internal const val CENO_EXTENSION_ID = "ceno@equalit.ie"
    internal const val CENO_EXTENSION_URL = "resource://android/assets/addons/ceno/"

    /**
     * Installs the web extension in the runtime through the WebExtensionRuntime install method
     */
    fun install(runtime: WebExtensionRuntime) {
        runtime.installBuiltInWebExtension(
            CENO_EXTENSION_ID, CENO_EXTENSION_URL,
            onSuccess = {
                logger.debug("Installed CENO webextension: ${it.id}")
            },
            onError = { throwable ->
                logger.error("Failed to install CENO webextension:", throwable)
            }
        )
    }
}
