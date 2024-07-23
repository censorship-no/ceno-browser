package ie.equalit.ceno.components

import mozilla.components.browser.state.action.EngineAction
import mozilla.components.browser.state.selector.findTabOrCustomTab
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.EngineSession.LoadUrlFlags
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.support.ktx.util.URLStringUtils

class CustomLoadUrlUseCase internal constructor(
    private val store: BrowserStore,
    private val onNoTab: (String) -> TabSessionState,
) : SessionUseCases.LoadUrlUseCase {

    var onNoSelectedTab: (String) -> Unit = {}

    /**
     * Loads the provided URL using the currently selected session. If
     * there's no selected session a new session will be created using
     * [onNoTab].
     *
     * @param url The URL to be loaded using the selected session.
     * @param flags The [LoadUrlFlags] to use when loading the provided url.
     * @param additionalHeaders the extra headers to use when loading the provided url.
     */
    override operator fun invoke(
        url: String,
        flags: LoadUrlFlags,
        additionalHeaders: Map<String, String>?,
    ) {
        val sessionId = store.state.selectedTabId
        if (store.state.selectedTab == null) {
            /* TODO: rewriting URL is workaround for a narrow problem, it was not possible to
            *  open a website via p2p mechanism from the home page without forcing HTTPS
            *  should find better solution that doesn't rewrite url or force HTTPS */
            val httpsUrl = if (url.contains("localhost"))
            /* Don't rewrite if fetching local resource */
                url
            else
                "https://${URLStringUtils.toDisplayUrl(url)}"
            onNoSelectedTab.invoke(httpsUrl)
        } else {
            this.invoke(url, sessionId, flags, additionalHeaders)
        }
    }

    /**
     * Loads the provided URL using the specified session. If no session
     * is provided the currently selected session will be used. If there's
     * no selected session a new session will be created using [onNoTab].
     *
     * @param url The URL to be loaded using the provided session.
     * @param sessionId the ID of the session for which the URL should be loaded.
     * @param flags The [LoadUrlFlags] to use when loading the provided url.
     * @param additionalHeaders the extra headers to use when loading the provided url.
     */
    operator fun invoke(
        url: String,
        sessionId: String? = null,
        flags: LoadUrlFlags = LoadUrlFlags.none(),
        additionalHeaders: Map<String, String>? = null,
    ) {
        val loadSessionId = sessionId
            ?: store.state.selectedTabId
            ?: onNoTab.invoke(url).id


        val tab = store.state.findTabOrCustomTab(loadSessionId)
        val engineSession = tab?.engineState?.engineSession

        // If we already have an engine session load Url directly to prevent
        // context switches.
        if (engineSession != null) {
            val parentEngineSession = if (tab is TabSessionState) {
                tab.parentId?.let { store.state.findTabOrCustomTab(it)?.engineState?.engineSession }
            } else {
                null
            }
            engineSession.loadUrl(
                url = url,
                parent = parentEngineSession,
                flags = flags,
                additionalHeaders = additionalHeaders,
            )
            store.dispatch(
                EngineAction.OptimizedLoadUrlTriggeredAction(
                    loadSessionId,
                    url,
                    flags,
                    additionalHeaders,
                ),
            )
        } else {
            store.dispatch(
                EngineAction.LoadUrlAction(
                    loadSessionId,
                    url,
                    flags,
                    additionalHeaders,
                ),
            )
        }
    }
}