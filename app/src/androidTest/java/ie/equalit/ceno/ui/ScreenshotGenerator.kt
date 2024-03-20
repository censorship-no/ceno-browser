package ie.equalit.ceno.ui

import android.os.Build
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.ui.robots.clickContinue
import ie.equalit.ceno.ui.robots.denyPermissions
import ie.equalit.ceno.ui.robots.navigationToolbar
import ie.equalit.ceno.ui.robots.onboarding
import ie.equalit.ceno.ui.robots.waitForContinueButton
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar
import tools.fastlane.screengrab.locale.LocaleTestRule


@RunWith(JUnit4::class)
class ScreenshotGenerator {

    @get:Rule
    var activityRule = ActivityScenarioRule(BrowserActivity::class.java)


    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @Before
    fun setUp() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        CleanStatusBar.enableWithDefaults()
    }

    @After
    fun after() {
        CleanStatusBar.disable()
    }

    @Test
    fun testTakeScreenshots() {
        onboarding {
            Thread.sleep(1000)
            Screengrab.screenshot("000_fragment_onboarding")
            clickContinue()

            waitForContinueButton()
            Thread.sleep(1000)
            Screengrab.screenshot("001_fragment_onboarding_public_pvt")
            clickContinue()

            waitForContinueButton()
            Thread.sleep(1000)
            Screengrab.screenshot("002_fragment_onboarding_info")
            clickContinue()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                waitForContinueButton()
                Thread.sleep(1000)
                Screengrab.screenshot("003_fragment_onboarding_battery")
                clickContinue()

                denyPermissions()

                waitForContinueButton()
                Thread.sleep(1000)
                Screengrab.screenshot("004_fragment_onboarding_warning")
                clickContinue()
            }
            navigationToolbar {
                Thread.sleep(1000)
                Screengrab.screenshot("005_fragment_home")
            }.openThreeDotMenu {
                Thread.sleep(1000)
                Screengrab.screenshot("006_fragment_home_threedot")
            }.openSettings {
                Thread.sleep(1000)
                Screengrab.screenshot("007_preferences_general")
                clickDownRecyclerView(20)
                Thread.sleep(1000)
                Screengrab.screenshot("008_preferences_data")
                clickDownRecyclerView(4)
                Screengrab.screenshot("009_preferences_developertools")
            }.goBack {
            }.openThreeDotMenu {
            }.openSettings {
            }.openSettingsViewSearch {
                Thread.sleep(1000)
                Screengrab.screenshot("010_search_engine_settings")
            }.goBack {
            }.openSettingsViewCustomization {
                Thread.sleep(1000)
                Screengrab.screenshot("011_customization_preferences")
            }.openSettingsViewChangeAppIcon {
                Thread.sleep(1000)
                Screengrab.screenshot("012_fragment_change_icon")
            }.goBack {
                clickSetAppTheme()
                Thread.sleep(1000)
                Screengrab.screenshot("013_customization_preferences_setapptheme")
                clickCancelDialog()
                clickDefaultBehavior()
                Thread.sleep(1000)
                Screengrab.screenshot("014_customization_preferences_defaultbehavior")
                clickCancelDialog()
            }.goBack {
            }.openSettingsViewDeleteBrowsingData {
                Thread.sleep(1000)
                Screengrab.screenshot("015_fragment_delete_browsing_data")
            }.goBack {
                clickDownRecyclerView(24)
            }.openSettingsViewNetworkDetails {
                Thread.sleep(1000)
                Screengrab.screenshot("016_network_detail_preference")
            }.goBack {
            }.openSettingsViewSources {
                Thread.sleep(1000)
                Screengrab.screenshot("017_sources_preferences")
            }.goBack{
                Thread.sleep(1000)
            }.openSettingsViewAboutPage {
                Thread.sleep(1000)
                Screengrab.screenshot("018_fragment_about")
            }
        }
    }
}

