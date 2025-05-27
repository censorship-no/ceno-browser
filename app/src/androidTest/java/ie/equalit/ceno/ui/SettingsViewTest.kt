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
            verifyBridgeModeToggle()
            verifyBridgeModeSummary()

            clickDownRecyclerView(1)
            verifyShowOnboarding()

            clickDownRecyclerView(1)
            verifyBackgroundMetricsButton()

            clickDownRecyclerView(1)
            verifyDeleteBrowsingData()

            clickDownRecyclerView(1)
            verifyChangeLanguageButton()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                clickDownRecyclerView(2)
                verifyPermissionHeading()
                Thread.sleep(5000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    verifyAllowNotification()
                    clickDownRecyclerView(1)
                }
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
            verifyWebsiteSourcesButton()
            verifyWebsiteSourcesSummary()

            clickDownRecyclerView(1)
            verifyTrackingProtectionButton()
            verifyTrackingProtectionSummary()

            clickDownRecyclerView(1)
            verifyCenoNetworkDetailsButton()
            verifyCenoNetworkDetailsSummary()

            clickDownRecyclerView(1)
            verifyEnableLogFile()

            clickDownRecyclerView(1)
            verifyAboutHeading()

            clickDownRecyclerView(1)
            verifyCenoBrowserServiceDisplay()

            clickDownRecyclerView(1)
            verifyCenoVersionDisplay()

            clickDownRecyclerView(1)
            verifyGeckoviewVersionDisplay()

            clickDownRecyclerView(1)
            verifyOuinetVersionDisplay()

            clickDownRecyclerView(1)
            verifyAboutEqualitieButton()
            // TODO: check if that the displayed values match some patterns
        }
    }

    @Test
    fun privacySettingsItemsTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(17)
            verifyTrackingProtectionButton()
            Thread.sleep(5000)
        }.openSettingsViewPrivacy {
            verifyPrivacyUpButton()
            verifyTrackingProtectionHeading()
            verifyTPEnableInNormalBrowsing()
            verifyTPEnableinPrivateBrowsing()
        }
    }

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
            clickDownRecyclerView(16)
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
            clickDownRecyclerView(18)
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

    @Test
    fun developerToolsSettingsItemsTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(24)
            verifyCenoVersionDisplay()
            for (i in 0..8) {
                clickCenoVersionDisplay()
                Thread.sleep(2000)
            }
            Thread.sleep(5000)
            verifyAdditionalDeveloperToolsButton()
        }.openSettingsViewDeveloperTools {
            verifyDeveloperToolsUpButton()
            verifyDeveloperToolsHeading()
            verifyRemoteDebugging()
            toggleRemoteDebuggingOn()
            toggleRemoteDebuggingOff()
            verifyExportOuinetLog()
            clickExportOuinetLog()
            verifyExportOuinetLogDescription()
            verifyExportOuinetLogDownload()
            verifyExportOuinetLogView()
            mDevice.pressBack()
            verifyAnnouncementSource()
            verifyAnnouncementSourceSummary()
            clickAnnouncementSource()
            verifyAnnouncementOption1()
            verifyAnnouncementOption2()
            verifyAnnouncementOption3()
            clickCancelDialog()
            verifyAnnouncementExpiration()
            toggleAnnouncementExpirationOn()
            toggleAnnouncementExpirationOff()
        }.goBack {
            Thread.sleep(5000)
            verifyCenoVersionDisplay()
            for (i in 0..8) {
                clickCenoVersionDisplay()
                Thread.sleep(2000)
            }
            Thread.sleep(5000)
            verifyAdditionalDeveloperToolsButtonGone()
        }
    }

    // TODO: log assertion is unreliable on Android 8- and Android 14+
    @SdkSuppress(minSdkVersion = 28, maxSdkVersion = 33)
    @Test
    fun enableLogFileTest() {
        navigationToolbar {
        }.openThreeDotMenu {
            verifyOpenSettingsExists()
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(20)
            Thread.sleep(5000)
            verifyEnableLogFile()
            clickEnableLogFile()
            Thread.sleep(5000)
            verifyExportLogButton()
        }.goBack {
        }.enterUrlAndEnterToBrowser("https://example.com".toUri()){
            // Do something that will generate ouinet logs
            Thread.sleep(5000)
        }
        assert(LogHelper.findInLogs("[DEBUG]", 10000))
    }

    @SdkSuppress(minSdkVersion = 28, maxSdkVersion = 33)
    @Test
    fun disableLogFileTest() {
        enableLogFileTest()
        navigationToolbar {
        }.openThreeDotMenu {
            verifyOpenSettingsExists()
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(20)
            Thread.sleep(5000)
            verifyEnableLogFile()
            clickEnableLogFile()
            Thread.sleep(5000)
            verifyExportLogButtonGone()
        }.goBack {
        }.enterUrlAndEnterToBrowser("https://ouinet.work".toUri()){
            // Do something else that will generate ouinet logs
            Thread.sleep(5000)
        }
        assert(!LogHelper.findInLogs("[DEBUG]", 10000))
    }

    // TODO: also relies on log assertion, which is unreliable on Android 14+
    @SdkSuppress(minSdkVersion = 28)
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
            clickDownRecyclerView(20)
            Thread.sleep(5000)
            verifyEnableLogFile()
            clickEnableLogFile()
        }.goBack {
        }.openThreeDotMenu {
        }.openSettings {
            Thread.sleep(2000)
            verifyBridgeModeToggle()
            verifyBridgeModeSummary()
            clickBridgeModeToggle()
            waitForBridgeModeDialog()
            Thread.sleep(15000)
            waitForThankYouDialog()
        }.goBack {
        }.openThreeDotMenu {
        }.openSettings {
            // Settings page must be reopened for ouinet status API
            // to be requested again and this message to be logged
            Thread.sleep(1000)
            assert(LogHelper.findInLogs("bridge_announcement=true"))
        }
    }

    @SdkSuppress(minSdkVersion = 28, maxSdkVersion = 33)
    @Test
    fun logLevelDebugAfterConnectivityChangeTest() {
        enableLogFileTest()
        mDevice.executeShellCommand("svc wifi disable")
        Thread.sleep(5000)
        mDevice.executeShellCommand("svc wifi enable")
        Thread.sleep(15000)
        assert(LogHelper.findInLogs("[INFO] Log level set to: DEBUG"))
    }

    @SdkSuppress(minSdkVersion = 28, maxSdkVersion = 33)
    @Test
    fun logLevelInfoAfterConnectivityChangeTest() {
        disableLogFileTest()
        mDevice.executeShellCommand("svc wifi disable")
        Thread.sleep(5000)
        mDevice.executeShellCommand("svc wifi enable")
        Thread.sleep(15000)
        assert(LogHelper.findInLogs("[INFO] Log level set to: INFO", 30000))
    }

    @Test
    fun metricsSettingsItemsTest() {
        navigationToolbar {
        }.openThreeDotMenu {
            verifyOpenSettingsExists()
        }.openSettings {
            Thread.sleep(5000)
            clickDownRecyclerView(11)
            Thread.sleep(2000)
            verifyBackgroundMetricsButton()
        }.openSettingsViewMetrics {
            Thread.sleep(1000)
            verifyCrashReportingButton()
            verifyMetricsButton()
        }
    }
}
