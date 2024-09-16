/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.ui.robots

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import ie.equalit.ceno.helpers.TestHelper.packageName

/**
 * Implementation of Robot Pattern for the navigation toolbar menu.
 */
class HomepageRobot {

    class Transition {

        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun openPersonalHomepage(interact: HomepageRobot.() -> Unit): HomepageRobot.Transition {
            personalModeToggle().click()
            HomepageRobot().interact()
            return HomepageRobot.Transition()
        }
    }
}

fun homepage(interact: HomepageRobot.() -> Unit): HomepageRobot.Transition {
    HomepageRobot().interact()
    return HomepageRobot.Transition()
}

private fun personalModeToggle() = mDevice.findObject(UiSelector().resourceId("$packageName:id/personal_mode_card"))
