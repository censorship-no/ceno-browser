/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("TooManyFunctions")

package ie.equalit.ceno.ui.robots

import android.view.KeyEvent
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.helpers.TestAssetHelper.waitingTime
import ie.equalit.ceno.helpers.TestHelper.packageName
import ie.equalit.ceno.helpers.click
import ie.equalit.ceno.helpers.hasCousin
import org.hamcrest.CoreMatchers.allOf

/**
 * Implementation of Robot Pattern for the settings menu.
 */
class SettingsViewRobot {
    fun verifySettingsViewExists() = assertSettingsView()

    fun verifyNavigateUp(): Unit = assertNavigateUpButton()

    fun verifyGeneralHeading(): ViewInteraction = assertGeneralHeading()
    fun verifyTrackingProtectionButton(): ViewInteraction = assertTrackingProtectionButton()
    fun verifyTrackingProtectionSummary(): ViewInteraction = assertTrackingProtectionSummary()
    fun verifySearchButton(): ViewInteraction = assertSearchButton()
    fun verifySearchSummary(): ViewInteraction = assertSearchSummary()
    fun verifyCustomizationButton(): ViewInteraction = assertCustomizationButton()
    fun verifyCustomizationSummary(): ViewInteraction = assertCustomizationButton()
    fun verifyOpenLinksInApps() = assertOpenLinksInApps()
    fun verifyMakeDefaultBrowserButton() = assertMakeDefaultBrowserButton()
    fun verifyAddOnsButton() = assertAddOnsButton()

    fun verifyBridgeModeToggle(): ViewInteraction = assertBridgeModeToggle()
    fun verifyBridgeModeSummary(): ViewInteraction = assertBridgeModeSummary()
    fun verifyDeleteBrowsingData(): ViewInteraction = assertDeleteBrowsingData()
    fun verifyDisableBatteryOptimization(): Unit = assertDisableBatteryOptimizationButton()
    fun verifyShowOnboarding(): ViewInteraction = assertShowOnboarding()
    fun verifyCrashReportingButton() = assertCrashReportingButton()
    fun verifyMetricsButton() = assertMetricsButton()

    fun verifyDataHeading(): ViewInteraction = assertDataHeading()
    fun verifyLocalCacheDisplay(): ViewInteraction = assertLocalCacheDisplay()
    fun verifyLocalCacheDefaultValue(): ViewInteraction = assertLocalCacheDefaultValue()
    fun verifyContentsSharedButton(): ViewInteraction = assertContentsSharedButton()
    fun verifyContentsSharedDefaultValue(): ViewInteraction = assertContentsSharedDefaultValue()
    fun verifyClearCachedContentButton(): ViewInteraction = assertClearCachedContentButton()
    fun verifyClearCachedContentSummary(): ViewInteraction = assertClearCachedContentSummary()

    fun verifyDeveloperToolsHeading() = assertDeveloperToolsHeading()
    fun verifyCenoNetworkDetailsButton(): ViewInteraction = assertCenoNetworkDetailsButton()
    fun verifyCenoNetworkDetailsSummary(): ViewInteraction = assertCenoNetworkDetailsSummary()
    fun verifyEnableLogFile(): ViewInteraction = assertEnableLogFile()
    fun verifyWebsiteSourcesButton(): ViewInteraction = assertWebsiteSourcesButton()
    fun verifyWebsiteSourcesSummary(): ViewInteraction = assertWebsiteSourcesSummary()
    fun verifyAdditionalDeveloperToolsButton() = assertAdditionalDeveloperToolsButton()
    fun verifyAdditionalDeveloperToolsButtonGone() = assertAdditionalDeveloperToolsButtonGone()
    fun verifyExportLogButton(): ViewInteraction = assertExportLogButton()
    fun verifyExportLogButtonGone(): ViewInteraction = assertExportLogButtonGone()

    fun verifyAboutHeading() = assertAboutHeading()

    fun verifyCenoBrowserServiceDisplay(): ViewInteraction = assertCenoBrowserServiceDisplay()
    fun verifyCenoVersionDisplay(): ViewInteraction = assertCenoVersionDisplay()
    fun verifyGeckoviewVersionDisplay(): ViewInteraction = assertGeckoviewVersionDisplay()
    fun verifyOuinetVersionDisplay(): ViewInteraction = assertOuinetVersionDisplay()
    fun verifyAboutEqualitieButton() = assertAboutEqualitieButton()
    fun verifySettingsRecyclerViewToExist() = waitForSettingsRecyclerViewToExist()
    fun verifyChangeLanguageButton() = assertChangeLanguageButton()
    fun verifyPermissionHeading(): ViewInteraction = assertPermissionsHeading()
    fun verifyAllowNotification(): Unit = assertAllowNotificationButton()

    fun clickOpenLinksInApps() = openLinksInAppsToggle().click()

    fun clickEnableLogFile() = enableLogFile().click()

    fun clickBridgeModeToggle() = bridgeModeToggle().click()
    fun clickChangeLanguageButton() {
        changeLanguageButton().click()
    }
    fun clickCenoVersionDisplay() = cenoVersionDisplay().click()

    fun waitForBridgeModeDialog() {
        mDevice.findObject(
            UiSelector()
                .textContains("Updating Bridge Mode settings"),
        ).waitUntilGone(waitingTime)
    }

    fun waitForThankYouDialog() {
        onView(withText(R.string.title_success)).check(matches(isDisplayed()))
        onView(withText(R.string.dialog_btn_positive_ok)).click()
    }

    fun clickDownRecyclerView(count: Int) {
        for (i in 1..count) {
            recycleView().perform(ViewActions.pressKey(KeyEvent.KEYCODE_DPAD_DOWN))
            Thread.sleep(250)
        }
    }

    fun clickUpRecyclerView(count: Int) {
        for (i in 1..count) {
            recycleView().perform(ViewActions.pressKey(KeyEvent.KEYCODE_DPAD_UP))
        }
    }

    class Transition {
        fun openSettingsViewPrivacy(interact: SettingsViewPrivacyRobot.() -> Unit):
                SettingsViewPrivacyRobot.Transition {
            privacyButton().click()
            SettingsViewPrivacyRobot().interact()
            return SettingsViewPrivacyRobot.Transition()
        }

        fun openSettingsViewSearch(interact: SettingsViewSearchRobot.() -> Unit):
                SettingsViewSearchRobot.Transition {
            searchButton().click()
            SettingsViewSearchRobot().interact()
            return SettingsViewSearchRobot.Transition()
        }

        fun openSettingsViewCustomization(interact: SettingsViewCustomizationRobot.() -> Unit):
                SettingsViewCustomizationRobot.Transition {
            customizationButton().click()
            SettingsViewCustomizationRobot().interact()
            return SettingsViewCustomizationRobot.Transition()
        }

        fun makeDefaultBrowser(interact: ExternalAppsRobot.() -> Unit):
                ExternalAppsRobot.Transition {
            makeDefaultBrowserButton().click()
            ExternalAppsRobot().interact()
            return ExternalAppsRobot.Transition()
        }

        fun openSettingsViewDeleteBrowsingData(interact: SettingsViewDeleteBrowsingDataRobot.() -> Unit):
                SettingsViewDeleteBrowsingDataRobot.Transition {
            deleteBrowsingDataButton().click()
            SettingsViewDeleteBrowsingDataRobot().interact()
            return SettingsViewDeleteBrowsingDataRobot.Transition()
        }

        fun openSettingsViewAboutPage(interact: SettingsViewAboutPageRobot.() -> Unit):
                SettingsViewAboutPageRobot.Transition {
            aboutEqualitieButton().click()
            SettingsViewAboutPageRobot().interact()
            return SettingsViewAboutPageRobot.Transition()
        }

        fun openSettingsViewSources(interact: SettingsViewSourcesRobot.() -> Unit):
                SettingsViewSourcesRobot.Transition {
            websiteSourcesButton().click()
            SettingsViewSourcesRobot().interact()
            return SettingsViewSourcesRobot.Transition()
        }

        fun openSettingsViewNetworkDetails(interact: SettingsViewNetworkDetailsRobot.() -> Unit):
                SettingsViewNetworkDetailsRobot.Transition {
            cenoNetworkDetailsButton().click()
            SettingsViewNetworkDetailsRobot().interact()
            return SettingsViewNetworkDetailsRobot.Transition()
        }

        fun openSettingsViewDeveloperTools(interact: SettingsViewDeveloperToolsRobot.() -> Unit):
                SettingsViewDeveloperToolsRobot.Transition {
            additionalDeveloperToolsButton().click()
            SettingsViewDeveloperToolsRobot().interact()
            return SettingsViewDeveloperToolsRobot.Transition()
        }

        fun openSettingsViewMetrics(interact: SettingsViewMetricsRobot.() -> Unit):
                SettingsViewMetricsRobot.Transition {
            metricsButton().click()
            SettingsViewMetricsRobot().interact()
            return SettingsViewMetricsRobot.Transition()
        }

        fun goBack(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            mDevice.pressBack()
            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }
    }
}

private fun waitForSettingsRecyclerViewToExist() {
    mDevice.findObject(UiSelector().resourceId("$packageName:id/recycler_view"))
        .waitForExists(
            waitingTime,
        )
}

private fun assertSettingsView() {
    // verify that we are in the correct settings view
    onView(withText(R.string.settings))
        .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
}

private fun recycleView() = onView(withId(R.id.recycler_view))
private fun generalHeading() = onView(withText(R.string.general_category))
private fun trackingProtectionButton() = onView(withText(R.string.tracker_category))
private fun trackingProtectionSummary() = onView(withText(R.string.preferences_privacy_summary))
private fun searchButton() = onView(withText(R.string.set_default_search_engine))
private fun searchSummary() = onView(withText("DuckDuckGo selected"))
//TODO: check for different search engines when they are set
private fun customizationButton() = onView(allOf(isClickable(), withChild(withText(R.string.preferences_customization))))
private fun customizationSummary() = onView(withText(R.string.preferences_customization_summary))
private fun openLinksInAppsToggle() = Espresso.onView(allOf(withId(R.id.switchWidget), hasCousin(withText(R.string.open_links_in_apps))))
private fun makeDefaultBrowserButton() = Espresso.onView(withText(R.string.preferences_make_default_browser))

private fun addOnsButton() = onView(withText(R.string.preferences_add_ons))

private fun bridgeModeToggle() = onView(allOf(withId(R.id.switchWidget), hasCousin(withText(R.string.preferences_ceno_bridge_announcement))))
private fun bridgeModeSummary() = onView(withText(R.string.bridge_mode_ip_warning_text))
private fun deleteBrowsingDataButton() =  onView(withText(R.string.preferences_delete_browsing_data))
private fun showOnboardingToggle() = onView(allOf(withId(R.id.switchWidget), hasCousin(withText(R.string.preferences_show_onboarding))))

private fun crashReportingButton() = onView(allOf(withId(R.id.switchWidget), hasCousin(withText(R.string.preferences_allow_crash_reporting))))
private fun metricsButton() = onView(allOf(withId(R.id.switchWidget), hasCousin(withText(R.string.preferences_allow_clean_insights_tracking))))

private fun dataHeading() = onView(withText(R.string.ceno_data_category))
private fun localCacheDisplay() = onView(withText(R.string.preferences_ceno_cache_size))
private fun localCacheDefaultValue() = onView(withText("O B"))
private fun contentsSharedButton() = onView(withText(R.string.preferences_ceno_groups_count))
private fun contentsSharedDefaultValue() = onView(withText("0 sites"))
private fun clearCachedContentButton() = onView(withText(R.string.preferences_clear_ceno_cache))
private fun clearCachedContentSummary() = onView(withText(R.string.preferences_clear_ceno_cache_summary))

private fun developerToolsHeading() = Espresso.onView(withText(R.string.developer_tools_category))
private fun cenoNetworkDetailsButton() = onView(withText(R.string.preferences_ceno_network_config))
private fun cenoNetworkDetailsSummary() = onView(withText(R.string.preferences_ceno_network_config_summary))
private fun enableLogFile() = onView(allOf(withId(R.id.switchWidget), hasCousin(withText(R.string.preferences_ceno_enable_log))))
private fun privacyButton() = onView(withText(R.string.tracker_category))
private fun additionalDeveloperToolsButton() = onView(withText(R.string.preferences_additional_developer_tools))
private fun exportLogButton() = onView(withText(R.string.preferences_ceno_export_android_logs))

private fun websiteSourcesButton() = onView(withText(R.string.preferences_ceno_website_sources))
private fun websiteSourcesSummary() = onView(withText(R.string.preferences_website_sources_summary))

private fun aboutHeading() = onView(allOf(withText(R.string.about_category), withParent(withId(R.id.recycler_view))))
private fun cenoBrowserServiceDisplay() = onView(withText(R.string.preferences_about_ceno_browser_service))
private fun cenoVersionDisplay() = onView(withText(R.string.preferences_about_ceno))
private fun geckoviewVersionDisplay() = onView(withText(R.string.preferences_about_geckoview))
private fun ouinetVersionDisplay() = onView(withText(R.string.preferences_about_ouinet))
private fun changeLanguageButton() = onView(withText(R.string.preferences_change_language))
private fun permissionsHeading() = onView(withText(R.string.ceno_permissions_category))

private fun aboutEqualitieButton() = onView(allOf(isClickable(), withChild(withText(R.string.preferences_about_page))))

private fun assertGeneralHeading() = generalHeading()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertTrackingProtectionButton() = trackingProtectionButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertTrackingProtectionSummary() = trackingProtectionSummary()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSearchButton() = searchButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSearchSummary() = searchSummary()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCustomizationButton() = customizationButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCustomizationSummary() = customizationSummary()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertNavigateUpButton() {
    mDevice.wait(Until.findObject(By.text("Navigate up")), TestAssetHelper.waitingTimeShort)
}
private fun assertOpenLinksInApps() = openLinksInAppsToggle()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMakeDefaultBrowserButton() = makeDefaultBrowserButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertAddOnsButton() = addOnsButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertBridgeModeToggle() = bridgeModeToggle()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertBridgeModeSummary() = bridgeModeSummary()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertDeleteBrowsingData() = deleteBrowsingDataButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertDisableBatteryOptimizationButton() {
    mDevice.wait(Until.findObject(By.text("Battery Optimization")), waitingTime)
}
private fun assertShowOnboarding() = showOnboardingToggle()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCrashReportingButton() = crashReportingButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMetricsButton() = metricsButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertDataHeading() = dataHeading()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPermissionsHeading() = permissionsHeading()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertLocalCacheDisplay() = localCacheDisplay()
private fun assertLocalCacheDefaultValue() = localCacheDefaultValue()
private fun assertContentsSharedButton() = contentsSharedButton()
private fun assertContentsSharedDefaultValue() = contentsSharedDefaultValue()
private fun assertClearCachedContentButton() = clearCachedContentButton()
private fun assertClearCachedContentSummary() = clearCachedContentSummary()

private fun assertDeveloperToolsHeading() = developerToolsHeading()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCenoNetworkDetailsButton() = cenoNetworkDetailsButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCenoNetworkDetailsSummary() = cenoNetworkDetailsSummary()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertEnableLogFile() = enableLogFile()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertWebsiteSourcesButton() = websiteSourcesButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertWebsiteSourcesSummary() = websiteSourcesSummary()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertAdditionalDeveloperToolsButton() = additionalDeveloperToolsButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertAdditionalDeveloperToolsButtonGone() = additionalDeveloperToolsButton()
    .check(doesNotExist())
private fun assertExportLogButton() = exportLogButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertExportLogButtonGone() = exportLogButton()
    .check(doesNotExist())

private fun assertAboutHeading() { aboutHeading()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
}
private fun assertCenoBrowserServiceDisplay() = cenoBrowserServiceDisplay()
private fun assertCenoVersionDisplay() = cenoVersionDisplay()
private fun assertGeckoviewVersionDisplay() = geckoviewVersionDisplay()
private fun assertOuinetVersionDisplay() = ouinetVersionDisplay()
private fun assertAboutEqualitieButton() = aboutEqualitieButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertChangeLanguageButton() = changeLanguageButton().check(matches( isDisplayed()))
private fun assertAllowNotificationButton() {
    mDevice.wait(Until.findObject(By.text("Notifications")), waitingTime)
}
