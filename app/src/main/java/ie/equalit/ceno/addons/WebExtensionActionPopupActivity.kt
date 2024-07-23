/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.addons

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.components
import mozilla.components.browser.state.action.WebExtensionAction
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.engine.window.WindowRequest
import mozilla.components.lib.state.ext.consumeFrom

/**
 * An activity to show the pop up action of a web extension.
 */
class WebExtensionActionPopupActivity : AppCompatActivity(), EngineSession.Observer {
    private lateinit var webExtensionId: String
    private var engineSession: EngineSession? = null
    private var addonSettingsEngineView: EngineView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_on_settings)

        webExtensionId = requireNotNull(intent.getStringExtra("web_extension_id"))
        intent.getStringExtra("web_extension_name")?.let {
            title = it
        }

        addonSettingsEngineView = findViewById<View>(R.id.addonSettingsEngineView) as EngineView

        val session = engineSession
        if (session != null) {
            addonSettingsEngineView?.render(session)
            consumePopupSession()
        } else {
            (findViewById<View>(R.id.addonSettingsEngineView).consumeFrom(
                components.core.store,
                this
            ) { state ->
                state.extensions[webExtensionId]?.let { extState ->
                    extState.popupSession?.let {
                        if (engineSession == null) {
                            addonSettingsEngineView?.render(it)
                            consumePopupSession()
                            engineSession = it
                        }
                    }
                }
            })
        }
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {

        webExtensionId = requireNotNull(intent.getStringExtra("web_extension_id"))

        engineSession = components.core.store.state.extensions[webExtensionId]?.popupSession

        return when (name) {
            EngineView::class.java.name -> components.core.engine.createView(context, attrs)
                .asView()

            else -> super.onCreateView(parent, name, context, attrs)
        }
    }

    override fun onStart() {
        super.onStart()
        engineSession?.register(this)
    }

    override fun onStop() {
        super.onStop()
        engineSession?.unregister(this)
    }

    override fun onWindowRequest(windowRequest: WindowRequest) {
        if (windowRequest.type == WindowRequest.Type.CLOSE) {
            onBackPressed()
        } else {
            /* CENO: Handle links in popups by loading the requested url in a new tab and closing the popup */
            components.useCases.tabsUseCases.selectOrAddTab(windowRequest.url)
            onBackPressed()
        }
    }

    private fun consumePopupSession() {
        components.core.store.dispatch(
            WebExtensionAction.UpdatePopupSessionAction(webExtensionId, popupSession = null),
        )
    }
}
