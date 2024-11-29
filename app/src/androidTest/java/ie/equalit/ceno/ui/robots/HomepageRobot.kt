/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.TestHelper.packageName
import ie.equalit.ceno.helpers.click

/**
 * Implementation of Robot Pattern for the navigation toolbar menu.
 */
class HomepageRobot {
    fun verifyCenoNetworkStatusIcon() = assertCenoNetworkStatusIcon()

    fun openCenoNetworkStatusDialog() = cenoNetworkStatusIcon().click()
    fun verifyCenoNetworkStatusDialog() = assertCenoNetworkStatusDialog()

    class Transition {

        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun openPersonalHomepage(interact: HomepageRobot.() -> Unit): Transition {
            personalModeToggle().click()
            HomepageRobot().interact()
            return Transition()
        }
    }
}

fun homepage(interact: HomepageRobot.() -> Unit): HomepageRobot.Transition {
    HomepageRobot().interact()
    return HomepageRobot.Transition()
}

private fun personalModeToggle() = mDevice.findObject(UiSelector().resourceId("$packageName:id/personal_mode_card"))
private fun cenoNetworkStatusIcon() =  onView(withId(R.id.ceno_network_status_icon))

private fun assertCenoNetworkStatusIcon() {
    cenoNetworkStatusIcon().check(matches(isDisplayed()))
}
private fun assertCenoNetworkStatusDialog() {
    onView(withText(R.string.ceno_network_status_title)).check(matches(isDisplayed()))
    onView(withText(R.string.dialog_btn_positive_ok)).click()
}