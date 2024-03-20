package ie.equalit.ceno.ui.robots

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.helpers.click
import ie.equalit.ceno.R

/**
 * Implementation of Robot Pattern for the settings customization menu.
 */
class SettingsViewCustomizationRobot {

    fun verifyCustomizationUpButton() = assertCustomizationUpButton()
    fun verifyCustomizationSettings() = assertCustomizationSettingsView()

    fun verifyChangeAppIconButton() = assertChangeAppIconButton()
    fun verifySetAppThemeButton() = assertSetAppThemeButton()
    fun verifySetAppThemeSummary() = assertSetAppThemeSummary()
    fun verifyDefaultBehaviorButton() = assertDefaultBehaviorButton()
    fun verifyDefaultBehaviorSummary() = assertDefaultBehaviorSummary()

    fun clickSetAppTheme() {
        setAppThemeButton().click()
    }

    fun clickDefaultBehavior() {
        defaultBehaviorButton().click()
    }

    fun clickCancelDialog() {
        cancelDialogButton().click()
    }

    class Transition {
        fun settingsViewCustomization(interact: SettingsViewCustomizationRobot.() -> Unit): Transition {
            return Transition()
        }

        fun openSettingsViewChangeAppIcon(interact: SettingsViewChangeAppIconRobot.() -> Unit):
                SettingsViewChangeAppIconRobot.Transition {
            changeAppIconButton().click()
            SettingsViewChangeAppIconRobot().interact()
            return SettingsViewChangeAppIconRobot.Transition()
        }

        fun goBack(interact: SettingsViewRobot.() -> Unit): SettingsViewRobot.Transition {
            mDevice.pressBack()
            SettingsViewRobot().interact()
            return SettingsViewRobot.Transition()
        }
    }
}
private fun customizationSettingsView() = Espresso.onView(ViewMatchers.withText(R.string.preferences_customization))

private fun changeAppIconButton() = Espresso.onView(ViewMatchers.withText(R.string.preferences_change_app_icon))
private fun setAppThemeButton() = Espresso.onView(ViewMatchers.withText(R.string.preferences_theme))
private fun setAppThemeSummary() = Espresso.onView(ViewMatchers.withText(R.string.preferences_theme_summary))
private fun defaultBehaviorButton() = Espresso.onView(ViewMatchers.withText(R.string.preferences_clear_behavior))
private fun defaultBehaviorSummary() = Espresso.onView(ViewMatchers.withText(R.string.preferences_clear_behavior_summary))

private fun assertCustomizationUpButton() {
    mDevice.wait(Until.findObject(By.text("Navigate up")), TestAssetHelper.waitingTimeShort)
}
private fun assertCustomizationSettingsView() = customizationSettingsView()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertChangeAppIconButton() = changeAppIconButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSetAppThemeButton() = setAppThemeButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSetAppThemeSummary() = setAppThemeSummary()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertDefaultBehaviorButton() = defaultBehaviorButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertDefaultBehaviorSummary() = defaultBehaviorSummary()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun cancelDialogButton() = mDevice.findObject(
    UiSelector().resourceId("android:id/button2"),
)

