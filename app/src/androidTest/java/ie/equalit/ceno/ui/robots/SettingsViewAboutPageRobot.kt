package ie.equalit.ceno.ui.robots

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.helpers.TestHelper
import junit.framework.Assert.assertTrue

/**
 * Implementation of Robot Pattern for the settings delete browsing data menu.
 */
class SettingsViewAboutPageRobot {

    fun verifyAboutPageUpButton() = assertAboutPageUpButton()
    fun verifyAboutPageSettings() = assertAboutPageSettingsView()

    fun verifyAboutBrowser() {
        assertTrue(
            mDevice.findObject(UiSelector().resourceId("${TestHelper.packageName}:id/about_content"))
                .waitForExists(TestAssetHelper.waitingTime),
        )
        assertTrue(
            mDevice.findObject(UiSelector().resourceId("${TestHelper.packageName}:id/version_info"))
                .waitForExists(TestAssetHelper.waitingTime),
        )
    }

    class Transition {
        fun settingsViewSearch(interact: SettingsViewAboutPageRobot.() -> Unit): Transition {
            return Transition()
        }

        fun goBack(interact: SettingsViewRobot.() -> Unit): SettingsViewRobot.Transition {
            mDevice.pressBack()
            SettingsViewRobot().interact()
            return SettingsViewRobot.Transition()
        }
    }
}

private fun aboutPageSettingsView() = Espresso.onView(ViewMatchers.withText(R.string.preferences_about_page))

private fun assertAboutPageUpButton() {
    mDevice.wait(Until.findObject(By.text("Navigate up")), TestAssetHelper.waitingTimeShort)
}
private fun assertAboutPageSettingsView() = aboutPageSettingsView()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
