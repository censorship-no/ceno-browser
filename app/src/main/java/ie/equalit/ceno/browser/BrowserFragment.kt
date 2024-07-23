/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.browser

//import mozilla.components.feature.toolbar.WebExtensionToolbarFeature
//import ie.equalit.ceno.getComponents
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import ie.equalit.ceno.R
import ie.equalit.ceno.settings.Settings
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.support.base.feature.UserInteractionHandler

/**
 * Fragment used for browsing the web within the main app.
 */
class BrowserFragment : BaseBrowserFragment(), UserInteractionHandler {
    /* Removing WebExtension toolbar feature, see below for more details
    private val webExtToolbarFeature = ViewBoundFeatureWrapper<WebExtensionToolbarFeature>()
     */

    private val toolbar: BrowserToolbar
        get() = requireView().findViewById(R.id.toolbar)

    /*
    override val shouldUseComposeUI: Boolean
        get() = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(
            getString(R.string.pref_key_compose_ui),
            false,
        )
     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val homeAction = BrowserToolbar.Button(
            imageDrawable = ResourcesCompat.getDrawable(
                resources,
                R.drawable.mozac_ic_home_24,
                null
            )!!,
            contentDescription = requireContext().getString(R.string.browser_toolbar_home),
            iconTintColorResource = themeManager.getIconColor(),
            listener = ::onHomeButtonClicked,
        )


        if (Settings.shouldShowHomeButton(requireContext())) {
            toolbar.addNavigationAction(homeAction)
        }

        /*
         * Remove WebExtension toolbar feature because
         * we don't want the browserAction button in toolbar and
         * the pageAction button created by it didn't work anyway
         */
        /*
        webExtToolbarFeature.set(
            feature = WebExtensionToolbarFeature(
                toolbar,
                requireContext().components.core.store,
            ),
            owner = this,
            view = view,
        )
        */
        binding.sessionControlRecyclerView.visibility = View.GONE
        binding.swipeRefresh.visibility = View.VISIBLE
    }

    private fun onHomeButtonClicked() {
        findNavController().navigate(R.id.action_global_home)
    }
}
