package ie.equalit.ceno.ui.robots

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.TestAssetHelper

/**
 * Implementation of Robot Pattern for the settings delete browsing data menu.
 */
class SettingsViewDeleteBrowsingDataRobot {

    fun verifyDeleteBrowsingDataUpButton() = assertDeleteBrowsingDataUpButton()
    fun verifyDeleteBrowsingDataSettings() = assertDeleteBrowsingDataSettingsView()

    class Transition {
        fun settingsViewSearch(interact: SettingsViewDeleteBrowsingDataRobot.() -> Unit): Transition {
            return Transition()
        }

        fun goBack(interact: SettingsViewRobot.() -> Unit): SettingsViewRobot.Transition {
            mDevice.pressBack()
            SettingsViewRobot().interact()
            return SettingsViewRobot.Transition()
        }
    }
}

private fun deleteBrowsingDataSettingsView() = Espresso.onView(ViewMatchers.withText(R.string.preferences_delete_browsing_data))

private fun assertDeleteBrowsingDataUpButton() {
    mDevice.wait(Until.findObject(By.text("Navigate up")), TestAssetHelper.waitingTimeShort)
}
private fun assertDeleteBrowsingDataSettingsView() = deleteBrowsingDataSettingsView()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
