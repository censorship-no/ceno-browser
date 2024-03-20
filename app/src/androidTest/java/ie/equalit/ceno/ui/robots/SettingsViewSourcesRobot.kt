package ie.equalit.ceno.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.helpers.click
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.hasCousin
import org.hamcrest.CoreMatchers

/**
 * Implementation of Robot Pattern for the settings privacy menu.
 */
class SettingsViewSourcesRobot {

    fun verifySourcesUpButton() = assertSourcesUpButton()
    fun verifySourcesSettings() = assertSourcesSettingsView()

    fun verifyWebsiteCheckbox(): ViewInteraction = assertWebsiteCheckbox()
    fun verifyWebsiteSummary(): ViewInteraction = assertWebsiteSummary()
    fun verifyPrivatelyCheckbox(): ViewInteraction = assertPrivatelyCheckbox()
    fun verifyPrivatelySummary(): ViewInteraction = assertPrivatelySummary()
    fun verifyPubliclyCheckbox(): ViewInteraction = assertPubliclyCheckbox()
    fun verifyPubliclySummary(): ViewInteraction = assertPubliclySummary()
    fun verifySharedCheckbox(): ViewInteraction = assertSharedCheckbox()
    fun verifySharedSummary(): ViewInteraction = assertSharedSummary()

    fun toggleWebsiteCheckbox() {
        websiteCheckbox().click()
    }
    fun togglePrivatelyCheckbox() {
        privatelyCheckbox().click()
    }
    fun togglePubliclyCheckbox() {
        publiclyCheckbox().click()
    }
    fun toggleSharedCheckbox() {
        sharedCheckbox().click()
    }

    fun setWebsiteSources(website: Boolean, private: Boolean, public: Boolean, shared : Boolean){
        if (!website) {
            verifyWebsiteCheckbox()
            toggleWebsiteCheckbox()
        }
        if (!private) {
            verifyPrivatelyCheckbox()
            togglePrivatelyCheckbox()
        }
        if (!public) {
            verifyPubliclyCheckbox()
            togglePubliclyCheckbox()
        }
        if (!shared) {
            verifySharedCheckbox()
            toggleSharedCheckbox()
        }
    }


    class Transition {
        fun settingsViewSearch(interact: SettingsViewSourcesRobot.() -> Unit): Transition {
            return Transition()
        }

        fun goBack(interact: SettingsViewRobot.() -> Unit): SettingsViewRobot.Transition {
            mDevice.pressBack()
            SettingsViewRobot().interact()
            return SettingsViewRobot.Transition()
        }
    }
}

private fun sourcesUpButton() = onView(ViewMatchers.withContentDescription("Navigate up"))

private fun sourcesSettingsView() = onView(ViewMatchers.withText(R.string.preferences_ceno_website_sources))

private fun websiteCheckbox() = onView(
    CoreMatchers.allOf(
        ViewMatchers.withId(android.R.id.checkbox),
        hasCousin(ViewMatchers.withText(R.string.preferences_ceno_sources_origin))
    )
)
private fun websiteSummary() = onView(ViewMatchers.withText(R.string.preferences_ceno_sources_origin_summary))
private fun privatelyCheckbox() = onView(
    CoreMatchers.allOf(
        ViewMatchers.withId(android.R.id.checkbox),
        hasCousin(ViewMatchers.withText(R.string.preferences_ceno_sources_private))
    )
)
private fun privatelySummary() = onView(ViewMatchers.withText(R.string.preferences_ceno_sources_private_summary))
private fun publiclyCheckbox() = onView(
    CoreMatchers.allOf(
        ViewMatchers.withId(android.R.id.checkbox),
        hasCousin(ViewMatchers.withText(R.string.preferences_ceno_sources_public))
    )
)
private fun publiclySummary() = onView(ViewMatchers.withText(R.string.preferences_ceno_sources_public_summary))
private fun sharedCheckbox() = onView(
    CoreMatchers.allOf(
        ViewMatchers.withId(android.R.id.checkbox),
        hasCousin(ViewMatchers.withText(R.string.preferences_ceno_sources_peers))
    )
)
private fun sharedSummary() = onView(ViewMatchers.withText(R.string.preferences_ceno_sources_peers_summary))

private fun assertSourcesUpButton() {
    mDevice.wait(Until.findObject(By.text("Navigate up")), TestAssetHelper.waitingTimeShort)
}
private fun assertSourcesSettingsView() = sourcesSettingsView()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

private fun assertWebsiteCheckbox() = websiteCheckbox()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertWebsiteSummary() = websiteSummary()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPrivatelyCheckbox() = privatelyCheckbox()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPrivatelySummary() = privatelySummary()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPubliclyCheckbox() = publiclyCheckbox()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPubliclySummary() = publiclySummary()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSharedCheckbox() = sharedCheckbox()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSharedSummary() = sharedSummary()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
