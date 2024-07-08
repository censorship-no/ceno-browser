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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ie.equalit.ceno.AppPermissionCodes
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.home.HomeFragment.Companion.TAG
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.tooltip.CenoTooltip
import ie.equalit.ceno.tooltip.CenoTourStartOverlay
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.feature.readerview.view.ReaderViewControlsBar
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.CirclePromptFocal

/**
 * Fragment used for browsing the web within the main app.
 */
class BrowserFragment : BaseBrowserFragment(), UserInteractionHandler {
    private lateinit var tooltip: CenoTooltip
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
                                tooltip.dismiss()
                                showSourcesTooltip()
                            }
                            MaterialTapTargetPrompt.STATE_REVEALED -> {
                                tooltip.addExitButton() {
                                    requireComponents.cenoPreferences.nextTooltip = -1
                                    tooltip.dismiss()
                                }
                            }
                        }
                    },
                    onButtonPressListener = {
                        goToNextTooltip()
                    }
                )
                tooltip.tooltip?.show()
            }
            TOOLTIP_CLEAR_CENO -> {
                tooltip = CenoTooltip(
                    this,
                    R.id.action_image,
                    getString(R.string.onboarding_cleanup_title),
                    getString(R.string.onboarding_cleanup_text),
                    CirclePromptFocal(),
                    stopCaptureTouchOnFocal = true,
                    listener = { _, state ->
                        when (state) {
                            MaterialTapTargetPrompt.STATE_FINISHED-> {
                                requireComponents.cenoPreferences.nextTooltip += 1
                                tooltip.dismiss()
                            }
                            MaterialTapTargetPrompt.STATE_REVEALED -> {
                                tooltip.addExitButton() {
                                    requireComponents.cenoPreferences.nextTooltip = -1
                                    tooltip.dismiss()
                                }
                            }
                        }
                    },
                    onButtonPressListener = {
                        goToNextTooltip()
                    }
                )
                tooltip.tooltip?.show()
            }
            TOOLTIP_PERMISSION -> {
                CenoTourStartOverlay(
                    this,
                    true,
                    startListener = {
                        requireComponents.cenoPreferences.nextTooltip = -1
                        askForPermissions()
                    },
                    skipListener = {}
                ).show()
            }
        }
    }

    private fun goToNextTooltip() {
        requireComponents.cenoPreferences.nextTooltip += 1
        tooltip.dismiss()
        showSourcesTooltip()
    }

     override fun onCancelSourcesPopup() {
        showSourcesTooltip()
    }

    override fun onResume() {
        super.onResume()
        showSourcesTooltip()
    }


    private fun onHomeButtonClicked() {
        findNavController().navigate(R.id.action_global_home)
    }

    private fun askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            /* This is Android 13 or later, ask for permission POST_NOTIFICATIONS */
            allowPostNotifications()
        } else {
            /* This is NOT Android 13, just ask to disable battery optimization */
            disableBatteryOptimization()
        }
    }

    override fun onActivityResult(requestCode: Int, data: Intent?, resultCode: Int): Boolean {
        super.onActivityResult(requestCode, resultCode, data)
        if (requireComponents.permissionHandler.onActivityResult(requestCode, data, resultCode)) {
            Log.i(TAG, "Permission - Success")
        } else {
            Log.w(TAG, "Permission denied")
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppPermissionCodes.REQUEST_CODE_NOTIFICATION_PERMISSIONS) {
            requireComponents.ouinet.background.start()
            disableBatteryOptimization()
        } else {
            Log.e(TAG, "Unknown request code received: $requestCode")
        }
    }

    private fun disableBatteryOptimization() {
        requireComponents.permissionHandler.requestBatteryOptimizationsOff(requireActivity())
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun allowPostNotifications() {
        requireComponents.permissionHandler.requestPostNotificationsPermission(this)
    }

    companion object {
        const val TOOLTIP_CENO_SOURCES = 5
        const val TOOLTIP_CLEAR_CENO = 6
        const val TOOLTIP_PERMISSION = 7
    }
}
