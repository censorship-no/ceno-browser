package ie.equalit.ceno.ui

import android.os.Build
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.ui.robots.clickNext
import ie.equalit.ceno.ui.robots.clickPermissions
import ie.equalit.ceno.ui.robots.denyPermissions
import ie.equalit.ceno.ui.robots.givePermissions
import ie.equalit.ceno.ui.robots.homepage
import ie.equalit.ceno.ui.robots.navigationToolbar
import ie.equalit.ceno.ui.robots.onboarding
import ie.equalit.ceno.ui.robots.standby
import ie.equalit.ceno.ui.robots.waitForNextTooltipButton
import ie.equalit.ceno.ui.robots.waitForPermissionsTooltip
import org.junit.After
import org.junit.Before
import org.junit.Ignore
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
        standby {
        }.waitForStandbyIfNeeded()
        onboarding {
            Thread.sleep(1000)
            Screengrab.screenshot("000_tooltip_begin_tour")
            //click get started
            beginTooltipsTour()

            waitForNextTooltipButton()
            Thread.sleep(1000)
            Screengrab.screenshot("001_tooltip_browsing_modes")
            clickNext()

            waitForNextTooltipButton()
            Thread.sleep(1000)
            Screengrab.screenshot("002_tooltip_shortcuts")
            clickNext()

            waitForNextTooltipButton()
            Thread.sleep(1000)
            Screengrab.screenshot("003_tooltip_address_bar")
            clickNext()

            waitForNextTooltipButton()
            Thread.sleep(1000)
            Screengrab.screenshot("004_tooltip_ceno_sources")
            clickNext()

            waitForNextTooltipButton()
            Thread.sleep(1000)
            Screengrab.screenshot("005_tooltip_clear_ceno")
            clickNext()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                //wait for permissions tooltip
                waitForPermissionsTooltip()
                Thread.sleep(1000)
                Screengrab.screenshot("006_tooltip_permissions")
                clickPermissions()

                // TODO: on Android 13, clicking deny double clicks the continue btn?
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                    givePermissions()
                }
                else {
                    denyPermissions()
//                    waitForNextTooltipButton()
//                    Thread.sleep(1000)
//                    Screengrab.screenshot("004_fragment_onboarding_warning")
//                    clickContinue()
                }
            }
            navigationToolbar {
                Thread.sleep(1000)
                Screengrab.screenshot("007_fragment_home")
            }.openThreeDotMenu {
                Thread.sleep(1000)
                Screengrab.screenshot("008_fragment_home_threedot")
            }.openSettings {
                // TODO: improve how all the settings are captured
                Thread.sleep(1000)
                Screengrab.screenshot("009_preferences_general")
                clickDownRecyclerView(20)
                Thread.sleep(1000)
                Screengrab.screenshot("010_preferences_data")
                clickDownRecyclerView(6)
                Thread.sleep(1000)
                Screengrab.screenshot("011_preferences_developertools")
            }.goBack {
            }.openThreeDotMenu {
            }.openSettings {
            }.openSettingsViewSearch {
                Thread.sleep(1000)
                Screengrab.screenshot("012_search_engine_settings")
            }.goBack {
            }.openSettingsViewCustomization {
                Thread.sleep(1000)
                Screengrab.screenshot("013_customization_preferences")
            }.openSettingsViewChangeAppIcon {
                Thread.sleep(1000)
                Screengrab.screenshot("014_fragment_change_icon")
            }.goBack {
                clickSetAppTheme()
                Thread.sleep(1000)
                Screengrab.screenshot("015_customization_preferences_setapptheme")
                clickCancelDialog()
                clickDefaultBehavior()
                Thread.sleep(1000)
                Screengrab.screenshot("016_customization_preferences_defaultbehavior")
                clickCancelDialog()
            }.goBack {
            }.goBack {
            }.openThreeDotMenu {
            }.openSettings {
                clickDownRecyclerView(13)
                Thread.sleep(1000)
            }.openSettingsViewDeleteBrowsingData {
                Thread.sleep(1000)
                Screengrab.screenshot("017_fragment_delete_browsing_data")
            }.goBack {
            }.goBack {
            }.openThreeDotMenu {
            }.openSettings {
                clickDownRecyclerView(18)
                Thread.sleep(1000)
            }.openSettingsViewNetworkDetails {
                Thread.sleep(1000)
                // TODO: hide public IP address info in this screenshot
                //Screengrab.screenshot("018_network_detail_preference")
            }.goBack {
            }.goBack {
            }.openThreeDotMenu {
            }.openSettings {
                clickDownRecyclerView(21)
                Thread.sleep(1000)
            }.openSettingsViewSources {
                Thread.sleep(1000)
                Screengrab.screenshot("019_sources_preferences")
            }.goBack{
            }.goBack {
            }.openThreeDotMenu {
            }.openSettings {
                clickDownRecyclerView(26)
                Thread.sleep(1000)
            }.openSettingsViewAboutPage {
                Thread.sleep(1000)
                Screengrab.screenshot("020_fragment_about")
            }.goBack{
            }.goBack {
            }.openTabTrayMenu {
            }.openNewTab {
            }
            homepage {
            }.openPersonalHomepage {
                Thread.sleep(1000)
                Screengrab.screenshot("021_fragment_personal_home")
            }
        }
    }
}

