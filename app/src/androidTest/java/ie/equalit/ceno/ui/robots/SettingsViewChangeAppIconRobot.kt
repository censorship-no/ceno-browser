package ie.equalit.ceno.ui.robots

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.R

/**
 * Implementation of Robot Pattern for the settings change app icon menu.
 */
class SettingsViewChangeAppIconRobot {

    fun verifyChangeAppIconUpButton() = assertChangeAppIconUpButton()
    fun verifyChangeAppIconSettings() = assertChangeAppIconSettingsView()

    class Transition {
        fun settingsViewSearch(interact: SettingsViewChangeAppIconRobot.() -> Unit): Transition {
            return Transition()
        }

        fun goBack(interact: SettingsViewCustomizationRobot.() -> Unit): SettingsViewCustomizationRobot.Transition {
            mDevice.pressBack()
            SettingsViewCustomizationRobot().interact()
            return SettingsViewCustomizationRobot.Transition()
        }
    }
}

private fun changeAppIconSettingsView() = Espresso.onView(ViewMatchers.withText(R.string.preferences_change_app_icon))

private fun assertChangeAppIconUpButton() {
    mDevice.wait(Until.findObject(By.text("Navigate up")), TestAssetHelper.waitingTimeShort)
}
private fun assertChangeAppIconSettingsView() = changeAppIconSettingsView()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))


