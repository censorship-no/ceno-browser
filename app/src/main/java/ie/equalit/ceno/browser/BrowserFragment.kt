/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.browser

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import ie.equalit.ceno.AppPermissionCodes
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.tooltip.CenoTooltip
import ie.equalit.ceno.tooltip.CenoTourStartOverlay
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.support.base.feature.UserInteractionHandler
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.CirclePromptFocal

/**
 * Fragment used for browsing the web within the main app.
 */
class BrowserFragment : BaseBrowserFragment(), UserInteractionHandler {
    private var tooltip: CenoTooltip? = null
    private var startTooltip:CenoTourStartOverlay? = null
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
        requireComponents.metrics.autoTracker.measureVisit(listOf(TAG))
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
        binding.swipeRefresh.visibility = View.VISIBLE
    }

    private fun showSourcesTooltip() {
        when (requireComponents.cenoPreferences.nextTooltip) {
            TOOLTIP_CENO_SOURCES -> {
                tooltip = CenoTooltip(
                    this,
                    R.id.mozac_browser_toolbar_tracking_protection_indicator,
                    getString(R.string.tooltip_sources_title),
                    getString(R.string.tooltip_sources_description),
                    CirclePromptFocal(),
                    isAutoFinish = true,
                    listener = { _, state ->
                        when (state) {
                            MaterialTapTargetPrompt.STATE_FINISHED-> {
                                requireComponents.cenoPreferences.nextTooltip += 1
                                tooltip?.dismiss()
                            }
                            MaterialTapTargetPrompt.STATE_FOCAL_PRESSED-> {
                                tooltip?.dismiss()
                            }
                            MaterialTapTargetPrompt.STATE_REVEALED -> {
                                tooltip?.addButtons {
                                    exitCenoTour()
                                }
                            }
                        }
                    },
                    onNextButtonPressListener = {
                        goToNextTooltip()
                    }
                )
                tooltip?.tooltip?.show()
            }
            TOOLTIP_CLEAR_CENO -> {
                tooltip = CenoTooltip(
                    this,
                    R.id.action_image,
                    getString(R.string.onboarding_cleanup_title),
                    getString(R.string.onboarding_cleanup_text),
                    CirclePromptFocal(),
                    stopCaptureTouchOnFocal = true,
                    buttonText = if (requireComponents.permissionHandler.shouldShowPermissionsTooltip())
                        R.string.btn_next
                    else
                        R.string.onboarding_finish_button,
                    listener = { _, state ->
                        when (state) {
                            MaterialTapTargetPrompt.STATE_FINISHED-> {
                                requireComponents.cenoPreferences.nextTooltip += 1
                                tooltip?.dismiss()
                            }
                            MaterialTapTargetPrompt.STATE_REVEALED -> {
                                tooltip?.addButtons(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                                    exitCenoTour()
                                }
                            }
                        }
                    },
                    onNextButtonPressListener = {
                        if (requireComponents.permissionHandler.shouldShowPermissionsTooltip())
                            goToNextTooltip()
                        else {
                            exitCenoTour()
                        }
                    }
                )
                tooltip?.tooltip?.show()
            }
            TOOLTIP_PERMISSION -> {
                startTooltip = CenoTourStartOverlay(
                    this,
                    true,
                    startListener = {
                        requireComponents.cenoPreferences.nextTooltip = -1
                        Settings.setShowOnboarding(requireContext(), false)
                        askForPermissions()
                    },
                    skipListener = {}
                )
                startTooltip?.show()
            }
        }
    }

    private fun exitCenoTour() {
        tooltip?.dismiss()
        if (requireComponents.permissionHandler.shouldShowPermissionsTooltip()) {
            requireComponents.cenoPreferences.nextTooltip = TOOLTIP_PERMISSION
            showSourcesTooltip()
        }
        else {
            requireComponents.cenoPreferences.nextTooltip = -1
        }
        Settings.setShowOnboarding(requireContext(), false)
    }

    private fun goToNextTooltip() {
        requireComponents.cenoPreferences.nextTooltip += 1
        tooltip?.dismiss()
        showSourcesTooltip()
    }

     override fun onCancelSourcesPopup() {
        showSourcesTooltip()
    }

    override fun onResume() {
        super.onResume()
        showSourcesTooltip()
    }

    override fun onPause() {
        super.onPause()
        tooltip?.dismiss()
        startTooltip?.remove()
    }


    private fun onHomeButtonClicked() {
        findNavController().navigate(R.id.action_global_home)
    }

    private fun askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            /* This is Android 13 or later, ask for permission POST_NOTIFICATIONS */
            requireComponents.permissionHandler.requestPostNotificationsPermission(this)
        } else {
            /* This is NOT Android 13, just ask to disable battery optimization */
            requireComponents.permissionHandler.requestBatteryOptimizationsOff(requireActivity())
        }
    }

    companion object {
        private const val TAG = "BrowserFragment"
        const val TOOLTIP_CENO_SOURCES = 5
        const val TOOLTIP_CLEAR_CENO = 6
        const val TOOLTIP_PERMISSION = 7
    }
}
