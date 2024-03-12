package ie.equalit.ceno.ui

import android.os.Build
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.ui.robots.clickContinue
import ie.equalit.ceno.ui.robots.denyPermissions
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
            Screengrab.screenshot("fragment_onboarding")
            clickContinue()

            waitForContinueButton()
            Thread.sleep(1000)
            Screengrab.screenshot("fragment_onboarding_public_pvt")
            clickContinue()

            waitForContinueButton()
            Thread.sleep(1000)
            Screengrab.screenshot("fragment_onboarding_info")
            clickContinue()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                waitForContinueButton()
                Thread.sleep(1000)
                Screengrab.screenshot("fragment_onboarding_battery")
                clickContinue()

                denyPermissions()

                waitForContinueButton()
                Thread.sleep(1000)
                Screengrab.screenshot("fragment_onboarding_warning")
                clickContinue()
            }
        }
    }
}

