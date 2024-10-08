/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("DEPRECATION")

package ie.equalit.ceno.ui

import android.os.Build
import androidx.core.net.toUri
import androidx.test.filters.SdkSuppress
import androidx.test.rule.GrantPermissionRule
import ie.equalit.ceno.helpers.BrowserActivityTestRule
import ie.equalit.ceno.helpers.LogHelper
import ie.equalit.ceno.helpers.RetryTestRule
import ie.equalit.ceno.ui.robots.mDevice
import ie.equalit.ceno.ui.robots.navigationToolbar
import ie.equalit.ceno.ui.robots.onboarding
import ie.equalit.ceno.ui.robots.standby
import okhttp3.internal.wait
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

/**
 *   Tests for verifying the settings view options exist as expected:
 * - Appears when the settings submenu is tapped
 * - Expected options are displayed as listed below
 */

class SettingsViewTest {
    /* ktlint-disable no-blank-line-before-rbrace */ // This imposes unrgeadable grouping.

    // Grant the app access to the camera so that we can test the Firefox Accounts QR code reader
    @Rule @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.CAMERA)

    @get:Rule val browserActivityTestRule = BrowserActivityTestRule()

    @Rule
    @JvmField
    val retryTestRule = RetryTestRule(1)

    @Before
    fun setUp() {
        onboarding {
        }.skipOnboardingIfNeeded()
        standby {
        }.waitForStandbyIfNeeded()
    }

    // This test verifies settings view items are all in place
    @Test
    fun settingsItemsTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            verifySettingsRecyclerViewToExist()
            verifyNavigateUp()
            verifyGeneralHeading()

            // Get one item ahead of what we are verifying
            // this is a workaround because the verify doesn't
            // wait long enough for text to appear.
            clickDownRecyclerView(2)
            verifySearchButton()
            verifySearchSummary()

            clickDownRecyclerView(1)
            verifyCustomizationButton()
            verifyCustomizationSummary()

            clickDownRecyclerView(1)
            verifyOpenLinksInApps()

            clickDownRecyclerView(1)
            verifyMakeDefaultBrowserButton()

            clickDownRecyclerView(1)
            // extra click down for Add-ons option,
            // which will be hidden soon

            clickDownRecyclerView(1)
            verifyBridgeModeToggle()
            verifyBridgeModeSummary()

            clickDownRecyclerView(1)
            verifyShowOnboarding()

            clickDownRecyclerView(1)
            verifyCrashReportingButton()

            clickDownRecyclerView(1)
            verifyDeleteBrowsingData()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                clickDownRecyclerView(1)
                Thread.sleep(5000)
                verifyDisableBatteryOptimization()
            }

            clickDownRecyclerView(2)
            verifyDataHeading()
            verifyLocalCacheDisplay()
            verifyLocalCacheDefaultValue()
            verifyContentsSharedButton()
            verifyContentsSharedDefaultValue()

            clickDownRecyclerView(1)
            verifyClearCachedContentButton()
            verifyClearCachedContentSummary()

            clickDownRecyclerView(1)
            verifyDeveloperToolsHeading()

            clickDownRecyclerView(1)
            verifyRemoteDebugging()

            clickDownRecyclerView(1)
            // extra click down for Custom Add-on Collection option,
            // which will be hidden soon

            clickDownRecyclerView(1)
            verifyCenoNetworkDetailsButton()
            verifyCenoNetworkDetailsSummary()

            clickDownRecyclerView(1)
            verifyEnableLogFile()

            clickDownRecyclerView(1)
            verifyTrackingProtectionButton()
            verifyTrackingProtectionSummary()

            clickDownRecyclerView(1)
            verifyWebsiteSourcesButton()
            verifyWebsiteSourcesSummary()

            clickDownRecyclerView(1)
            verifyAboutHeading()

            clickDownRecyclerView(1)
            verifyCenoBrowserServiceDisplay()

            clickDownRecyclerView(1)
            verifyGeckoviewVersionDisplay()

            clickDownRecyclerView(1)
            verifyOuinetVersionDisplay()

            clickDownRecyclerView(1)
            verifyAboutEqualitieButton()
            // TODO: check if that the displayed values match some patterns
        }
    }

    /*
    @Test
    fun privacySettingsItemsTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
        }.openSettingsViewPrivacy {
            verifyPrivacyUpButton()
            verifyPrivacySettings()
            verifyTrackingProtectionHeading()
            verifyTPEnableInNormalBrowsing()
            verifyTPEnableinPrivateBrowsing()
            verifyDataChoicesHeading()
            verifyUseTelemetryToggle()
            verifyTelemetrySummary()
        }
    }
     */

    @Test
    fun setDefaultBrowserTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
        }.makeDefaultBrowser {
            verifyAndroidDefaultApps()
        }
    }

    @Test
    fun remoteDebuggingViaUSB() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(14)
            Thread.sleep(5000)
            verifyRemoteDebugging()
            toggleRemoteDebuggingOn()
            toggleRemoteDebuggingOff()
            toggleRemoteDebuggingOn()
        }
    }

    @Test
    fun aboutReferenceBrowserTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(24)
            Thread.sleep(5000)
        }.openSettingsViewAboutPage {
            verifyAboutBrowser()
        }
    }

    /* Can't check further because after creating the custom add-on collection
    the currently running process is terminated see:
    /blob/master/app/src/main/java/org/mozilla/reference/browser/settings/SettingsFragment.kt#L217
    Confirming the custom add-on collection creation or trying to continue testing afterwards
    will cause the test instrumentation process to crash */
    @Test
    @Ignore("Custom add-on are disabled right now due to breaking changes with android-component-v128")
    fun customAddonsCollectionTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(15)
            Thread.sleep(5000)
            verifyCustomAddonCollectionButton()
            clickCustomAddonCollectionButton()
            verifyCustomAddonCollectionPanelExist()
        }
    }

    @Test
    @Ignore("Disabled - too dependent on third-party UI, find some other not terrible app to test against")
    fun openLinksInAppsTest() {
        val url = "m.youtube.com"
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            verifyOpenLinksInApps()
            clickOpenLinksInApps()
        }.goBack {
        }.enterUrlAndEnterToBrowser(url.toUri()) {
            clickOpenInAppPromptButton()
        }.checkExternalApps {
            verifyYouTubeApp()
        }
        mDevice.pressHome()
    }

    @Test
    fun sourcesSettingsItemsTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(19)
            Thread.sleep(5000)
        }.openSettingsViewSources {
            verifySourcesUpButton()
            verifySourcesSettings()
            verifyWebsiteCheckbox()
            verifyWebsiteSummary()
            verifyPrivatelyCheckbox()
            verifyPrivatelySummary()
            verifyPubliclyCheckbox()
            verifyPubliclySummary()
            verifySharedCheckbox()
            verifySharedSummary()
        }
    }

    @Test
    fun networkDetailsSettingsItemsTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(16)
            verifyCenoNetworkDetailsButton()
            Thread.sleep(5000)
        }.openSettingsViewNetworkDetails {
            verifySourcesUpButton()
            verifyNetworkDetailsSettings()
            clickDownRecyclerView(3)
            verifyGeneralHeading()
            verifyOuinetProtocolDisplay()
            verifyReachabilityStatusDisplay()
            verifyUpnpStatusDisplay()
            clickDownRecyclerView(3)
            verifyUdpHeading()
            verifyLocalUdpEndpointsDisplay()
            verifyExternalUdpEndpointsDisplay()
            verifyPublicUdpEndpointsDisplay()
            clickDownRecyclerView(2)
            verifyBtBootstrapsHeading()
            verifyExtraBtBootstrapsButton()
        }
    }

    // TODO: log assertion is unreliable on Android 14+
    @SdkSuppress(maxSdkVersion = 33)
    @Test
    fun enableLogFileTest() {
        navigationToolbar {
        }.openThreeDotMenu {
            verifyOpenSettingsExists()
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(17)
            Thread.sleep(5000)
            verifyEnableLogFile()
            clickEnableLogFile()
            Thread.sleep(5000)
        }.goBack {
            assert(LogHelper.findInLogs("[DEBUG]", 10000))
        }
    }

    @SdkSuppress(maxSdkVersion = 33)
    @Test
    fun disableLogFileTest() {
        enableLogFileTest()
        navigationToolbar {
        }.openThreeDotMenu {
            verifyOpenSettingsExists()
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(17)
            Thread.sleep(5000)
            verifyEnableLogFile()
            clickEnableLogFile()
            Thread.sleep(5000)
        }.goBack {
            Thread.sleep(5000)
            assert(!LogHelper.findInLogs("[DEBUG]", 10000))
        }
    }

    @Test
    fun enableBridgeModeTest() {
        /* This is a regression test for a bug found in MR !127, see this comment for more info
        * https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/127#note_1795759444
        * */
        navigationToolbar {
        }.openThreeDotMenu {
            verifyOpenSettingsExists()
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(17)
            Thread.sleep(5000)
            verifyEnableLogFile()
            clickEnableLogFile()
            clickUpRecyclerView(8)
            Thread.sleep(2000)
            verifyBridgeModeToggle()
            verifyBridgeModeSummary()
            clickBridgeModeToggle()
            waitForBridgeModeDialog()
            Thread.sleep(5000)
            assert(LogHelper.findInLogs("[DEBUG] Bep5Client: Got pong from injectors, announcing as helper (bridge)"))
            waitForThankYouDialog()
        }
    }

    @SdkSuppress(maxSdkVersion = 33)
    @Test
    fun logLevelDebugAfterConnectivityChangeTest() {
        enableLogFileTest()
        mDevice.executeShellCommand("svc wifi disable")
        Thread.sleep(5000)
        mDevice.executeShellCommand("svc wifi enable")
        Thread.sleep(15000)
        assert(LogHelper.findInLogs("[INFO] Log level set to: DEBUG"))
    }

    @SdkSuppress(maxSdkVersion = 33)
    @Test
    fun logLevelInfoAfterConnectivityChangeTest() {
        disableLogFileTest()
        mDevice.executeShellCommand("svc wifi disable")
        Thread.sleep(5000)
        mDevice.executeShellCommand("svc wifi enable")
        Thread.sleep(15000)
        assert(LogHelper.findInLogs("[INFO] Log level set to: INFO", 20000))
    }
}
