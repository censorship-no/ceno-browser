package ie.equalit.ceno.components.ceno
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import mozilla.components.concept.engine.webextension.WebExtensionRuntime
import mozilla.components.support.base.log.logger.Logger

/**
 * Feature to enable https-by-default
 */
object HttpsByDefaultWebExt {
    private val logger = Logger("https-by-default")

    internal const val HTTPS_BY_DEFAULT_EXTENSION_ID = "https-by-default@robwu.nl"
    internal const val HTTPS_BY_DEFAULT_EXTENSION_URL =
        "resource://android/assets/addons/https-by-default/firefox/"

    /**
     * Installs the web extension in the runtime through the WebExtensionRuntime install method
     */
    fun install(runtime: WebExtensionRuntime) {
        runtime.installBuiltInWebExtension(
            HTTPS_BY_DEFAULT_EXTENSION_ID, HTTPS_BY_DEFAULT_EXTENSION_URL,
            onSuccess = {
                logger.debug("Installed HTTPS by default WebExtension: ${it.id}")
            },
            onError = { throwable ->
                logger.error("Failed to install HTTPS by default WebExtension:", throwable)
            }
        )
    }
}
