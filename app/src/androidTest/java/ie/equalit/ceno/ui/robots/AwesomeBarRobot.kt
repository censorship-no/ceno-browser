/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.allOf
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.Constants.LONG_CLICK_DURATION
import ie.equalit.ceno.helpers.TestAssetHelper.waitingTime
import ie.equalit.ceno.helpers.TestHelper.packageName

/**
 * Implementation of Robot Pattern for awesomebar.
 */
class AwesomeBarRobot {

    fun verifySearchSuggestion(searchSuggestionTitle: String) = assertSearchSuggestion(searchSuggestionTitle)
    fun verifyLinkFromClipboard(clipboardLink: String) = assertLinkFromClipboard(clipboardLink)
    fun verifyPastedToolbarText(expectedText: String) = assertPastedToolbarText(expectedText)

    fun typeText(searchTerm: String) {
        mDevice.findObject(
            UiSelector()
                .textContains("Search or enter address"),
        ).waitForExists(waitingTime)
        awesomeBar().setText(searchTerm)
    }

    fun clickClearToolbarButton() =
        clearToolbarButton().also {
            it.waitForExists(waitingTime)
            it.click()
        }

    fun longClickToolbar() {
        mDevice.waitForWindowUpdate(packageName, waitingTime)
        mDevice.findObject(UiSelector().resourceId("$packageName:id/awesomeBar"))
            .waitForExists(waitingTime)
        mDevice.findObject(UiSelector().resourceId("$packageName:id/toolbar"))
            .waitForExists(waitingTime)
        val toolbar = mDevice.findObject(By.res("$packageName:id/toolbar"))
        toolbar.click(LONG_CLICK_DURATION)
    }

    fun clickPasteText() {
        // Click again Copy from the text selection toolbar
        mDevice.findObject(UiSelector().textContains("Paste")).waitForExists(waitingTime)
        var pasteText = mDevice.findObject(By.textContains("Paste"))
        if (pasteText == null) {
            mDevice.findObject(UiSelector().textContains("PASTE")).waitForExists(waitingTime)
            pasteText = mDevice.findObject(By.textContains("PASTE"))
        }
        pasteText.click()
    }

    fun pasteAndLoadCopiedLink() {
        clickClearToolbarButton()
        longClickToolbar()
        clickPasteText()
        mDevice.pressEnter()
        mDevice.waitForWindowUpdate(packageName, waitingTime)
    }

    class Transition {

        fun openWebPage(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }

        fun clickSearchSuggestion(searchSuggestionTitle: String, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            val searchSuggestion = mDevice.findObject(UiSelector().textContains(searchSuggestionTitle))
            searchSuggestion.clickAndWaitForNewWindow()

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }
}

fun browserScreen(interact: AwesomeBarRobot.() -> Unit): AwesomeBarRobot.Transition {
    AwesomeBarRobot().interact()
    return AwesomeBarRobot.Transition()
}

private fun awesomeBar() =
    mDevice.findObject(UiSelector().resourceId("$packageName:id/mozac_browser_toolbar_edit_url_view"))

private fun clearToolbarButton() =
    mDevice.findObject(UiSelector().resourceId("$packageName:id/mozac_browser_toolbar_clear_view"))

private fun assertSearchSuggestion(searchSuggestionTitle: String) {
    mDevice.waitForIdle()
    assertTrue(
        mDevice.findObject(UiSelector().textContains(searchSuggestionTitle))
            .waitForExists(waitingTime),
    )
}

private fun assertLinkFromClipboard(clipboardLink: String) {
    mDevice.waitForIdle()
    mDevice.findObject(UiSelector().resourceId("$packageName:id/awesomeBar"))
        .waitForExists(waitingTime)
    mDevice.findObject(UiSelector().resourceId("$packageName:id/mozac_browser_awesomebar_title"))
        .waitForExists(waitingTime)
    assertTrue(
        mDevice.findObject(UiSelector().textContains(clipboardLink))
            .waitForExists(waitingTime),
    )
}

private fun assertPastedToolbarText(expectedText: String) {
    mDevice.findObject(UiSelector().resourceId("$packageName:id/toolbar"))
        .waitForExists(waitingTime)
    mDevice.findObject(UiSelector().resourceId("$packageName:id/mozac_browser_toolbar_url_view"))
        .waitForExists(waitingTime)
    onView(
        allOf(
            withSubstring(expectedText),
            withId(R.id.mozac_browser_toolbar_edit_url_view),
        ),
    ).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
}
