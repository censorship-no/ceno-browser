package ie.equalit.ceno.ui.robots

import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.helpers.click

/**
 * Implementation of Robot Pattern for the settings privacy menu.
 */
class SettingsViewSearchRobot {

    fun verifySearchUpButton() = assertSearchUpButton()
    fun verifySearchSettings() = assertSearchSettingsView()

    fun verifyGetSearchSuggestionsToggle(): ViewInteraction = assertGetSearchSuggestionsToggle()
    fun verifySearchSuggestionsSummary(): ViewInteraction = assertSearchSuggestionsSummary()

    fun toggleGetSearchSuggestions() {
        getSearchSuggestionsToggle().click()
    }

    class Transition {
        fun settingsViewSearch(interact: SettingsViewSearchRobot.() -> Unit): Transition {
            return Transition()
        }

        fun goBack(interact: SettingsViewRobot.() -> Unit): SettingsViewRobot.Transition {
            mDevice.pressBack()
            SettingsViewRobot().interact()
            return SettingsViewRobot.Transition()
        }
    }
}

private fun searchUpButton() = Espresso.onView(ViewMatchers.withContentDescription("Navigate up"))

private fun searchSettingsView() = Espresso.onView(ViewMatchers.withText("Choose search engine"))

private fun getSearchSuggestionsToggle() = Espresso.onView(ViewMatchers.withText("Get search suggestions"))
private fun searchSuggestionsSummary() = Espresso.onView(ViewMatchers.withText("Ceno will send what you type in the address bar to your search engine"))
private fun assertSearchUpButton() {
    mDevice.wait(Until.findObject(By.text("Navigate up")), TestAssetHelper.waitingTimeShort)
}
private fun assertSearchSettingsView() = searchSettingsView()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertGetSearchSuggestionsToggle() = getSearchSuggestionsToggle()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSearchSuggestionsSummary() = searchSuggestionsSummary()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
