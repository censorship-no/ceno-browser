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
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.ui.translateName

/**
 * An activity to show the settings of an add-on.
 */
class AddonSettingsActivity : AppCompatActivity() {

    private lateinit var optionsPageUrl: String
    private lateinit var engineSession: EngineSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_on_settings)

        val addon = requireNotNull(intent.getParcelableExtra<Addon>("add_on"))
        optionsPageUrl = requireNotNull(addon.installedState?.optionsPageUrl)

        title = addon.translateName(this)

        val addonSettingsEngineView = findViewById<View>(R.id.addonSettingsEngineView) as EngineView
        addonSettingsEngineView.render(engineSession)
        engineSession.loadUrl(optionsPageUrl)
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {

        engineSession = components.core.engine.createSession()

        return when (name) {
            EngineView::class.java.name -> components.core.engine.createView(context, attrs)
                .asView()

            else -> super.onCreateView(parent, name, context, attrs)
        }
    }

    override fun onDestroy() {
        engineSession.close()
        super.onDestroy()
    }
}
