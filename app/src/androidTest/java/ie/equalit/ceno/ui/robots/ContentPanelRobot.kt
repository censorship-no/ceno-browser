/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.ui.robots

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.junit.Assert.assertTrue
import ie.equalit.ceno.helpers.TestAssetHelper.waitingTime

/**
 * Implementation of Robot Pattern for the content panel.
 */
class ContentPanelRobot {
    fun verifyShareContentPanel() = assertShareContentPanel()

    class Transition {
        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun contentPanel(interact: ContentPanelRobot.() -> Unit): Transition {
            mDevice.waitForIdle()
            return Transition()
        }
    }
}

private fun assertShareContentPanel() {
    mDevice.waitForIdle()
    assertTrue(mDevice.findObject(UiSelector().textContains("Share")).waitForExists(waitingTime))
    mDevice.pressBack()
}
