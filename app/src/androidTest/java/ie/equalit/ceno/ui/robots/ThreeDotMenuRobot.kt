/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("TooManyFunctions")

package ie.equalit.ceno.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.uiautomator.UiSelector
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.TestAssetHelper.waitingTime
import ie.equalit.ceno.helpers.TestHelper.packageName
import ie.equalit.ceno.helpers.click
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import junit.framework.AssertionFailedError

/**
 * Implementation of Robot Pattern for three dot menu.
 */
class ThreeDotMenuRobot {

    fun verifyThreeDotMenuExists() = threeDotMenuRecyclerViewExists()

    fun verifyBackButtonExists() = assertBackButton()
    fun verifyForwardButtonExists() = assertForwardButton()
    fun verifyReloadButtonExists() = assertRefreshButton()
    fun verifyStopButtonExists() = assertStopButton()
    fun verifyShareButtonExists() = assertShareButton()
    fun verifyRequestDesktopSiteToggleExists() = assertRequestDesktopSiteToggle()
    fun verifyAddToHomescreenButtonExists() = assertAddToHomescreenButton()
    fun verifyFindInPageButtonExists() = assertFindInPageButton()
    fun verifyAddOnsButtonExists() = assertAddOnsButton()
    fun verifySyncedTabsButtonExists() = assertSyncedTabsButton()
    fun verifyReportIssueExists() = assertReportIssueButton()
    fun verifyOpenSettingsExists() = assertSettingsButton()

    fun verifyShareButtonDoesntExist() = assertShareButtonDoesntExist()
    fun verifyRequestDesktopSiteToggleDoesntExist() = assertRequestDesktopSiteToggleDoesntExist()
    fun verifyFindInPageButtonDoesntExist() = assertFindInPageButtonDoesntExist()
    fun verifyForwardButtonDoesntExist() = assertForwardButtonDoesntExist()
    fun verifyReloadButtonDoesntExist() = assertRefreshButtonDoesntExist()
    fun verifyStopButtonDoesntExist() = assertStopButtonDoesntExist()
    fun verifyAddToHomescreenButtonDoesntExist() = assertAddToHomescreenButtonDoesntExist()

    fun verifyAddToShortcutsButtonDoesntExist() = assertAddToShortcutsButtonDoesntExist()

    fun verifyRequestDesktopSiteIsTurnedOff() = assertRequestDesktopSiteIsTurnedOff()
    fun verifyRequestDesktopSiteIsTurnedOn() = assertRequestDesktopSiteIsTurnedOn()

    fun verifyClearCenoButtonExists() = assertClearCenoButton()
    fun verifyAddToShortcutsButtonExists() = assertAddToShortcutsButton()
    fun verifyRemoveFromShortcutsButtonExists() = assertRemoveFromShortcutsButton()
    fun verifyHttpsByDefaultButtonExists() = assertHttpsByDefaultButton()
    fun verifyUblockOriginButtonExists() = assertUblockOriginButton()

    class Transition {

        fun goForward(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            forwardButton().click()
            mDevice.findObject(
                UiSelector()
                    .resourceId("$packageName:id/mozac_browser_toolbar_progress"),
            ).waitUntilGone(waitingTime)
            mDevice.waitForIdle()

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun refreshPage(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            refreshButton().click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun doStop(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            stopButton().click()
            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }

        fun clickShareButton(interact: ShareOverlayRobot.() -> Unit): ShareOverlayRobot.Transition {
            shareButton().click()
            mDevice.waitForIdle()
            ShareOverlayRobot().interact()
            return ShareOverlayRobot.Transition()
        }

        @Suppress("SwallowedException")
        fun switchRequestDesktopSiteToggle(
            interact: NavigationToolbarRobot.() -> Unit,
        ): NavigationToolbarRobot.Transition {
            try {
                mDevice.findObject(UiSelector().textContains("Request desktop site"))
                    .waitForExists(waitingTime)
                requestDesktopSiteToggle().click()
                mDevice.waitForIdle()
                assertTrue(
                    mDevice.findObject(
                        UiSelector()
                            .resourceId("$packageName:id/mozac_browser_menu_recyclerView"),
                    ).waitUntilGone(waitingTime),
                )
            } catch (e: AssertionFailedError) {
                println("Failed to click request desktop toggle")
                // If the click didn't succeed the main menu remains displayed and should be dismissed
                mDevice.pressBack()
                threeDotMenuButton().click()
                mDevice.findObject(UiSelector().textContains("Request desktop site"))
                    .waitForExists(waitingTime)
                // Click again the Request desktop site toggle
                requestDesktopSiteToggle().click()
                mDevice.waitForIdle()
                assertTrue(
                    mDevice.findObject(
                        UiSelector()
                            .resourceId("$packageName:id/mozac_browser_menu_recyclerView"),
                    ).waitUntilGone(waitingTime),
                )
            }
            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }

        fun openFindInPage(interact: FindInPagePanelRobot.() -> Unit): FindInPagePanelRobot.Transition {
            findInPageButton().click()
            FindInPagePanelRobot().interact()
            return FindInPagePanelRobot.Transition()
        }

        fun reportIssue(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            reportIssueButton().click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun openSettings(interact: SettingsViewRobot.() -> Unit): SettingsViewRobot.Transition {
            settingsButton().click()
            mDevice.waitForIdle()
            SettingsViewRobot().interact()
            return SettingsViewRobot.Transition()
        }

        fun openAddonsManager(interact: AddonsManagerRobot.() -> Unit): AddonsManagerRobot.Transition {
            addOnsButton().click()

            AddonsManagerRobot().interact()
            return AddonsManagerRobot.Transition()
        }

        /*
        fun openSyncedTabs(interact: SyncedTabsRobot.() -> Unit): SyncedTabsRobot.Transition {
            mDevice.findObject(UiSelector().text("Synced Tabs")).waitForExists(waitingTime)
            syncedTabsButton().click()

            SyncedTabsRobot().interact()
            return SyncedTabsRobot.Transition()
        }
        */

        fun goBack(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            mDevice.pressBack()

            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }

        fun openAddToHomeScreen(interact: AddToHomeScreenRobot.() -> Unit): AddToHomeScreenRobot.Transition {
            mDevice.findObject(UiSelector().text("Add to Home screen")).waitForExists(waitingTime)
            addToHomescreenButton().click()

            AddToHomeScreenRobot().interact()
            return AddToHomeScreenRobot.Transition()
        }

        // TODO: these should return Robots for testing the extension popups
        fun openUblockOrigin(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            ublockOriginButton().click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun openHttpsByDefault(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            httpsByDefaultButton().click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }
}

private fun threeDotMenuRecyclerViewExists() {
    onView(withId(R.id.mozac_browser_menu_recyclerView)).check(matches(isDisplayed()))
}

private fun threeDotMenuButton() = onView(withId(R.id.mozac_browser_toolbar_menu))
private fun backButton() = onView(ViewMatchers.withContentDescription("Back"))
private fun forwardButton() = onView(ViewMatchers.withContentDescription("Forward"))
private fun refreshButton() = onView(ViewMatchers.withContentDescription("Refresh"))
private fun stopButton() = onView(ViewMatchers.withContentDescription("Stop"))
private fun shareButton() = onView(ViewMatchers.withText(R.string.browser_menu_share))
private fun requestDesktopSiteToggle() = onView(ViewMatchers.withText("Request desktop site"))
private fun findInPageButton() = onView(ViewMatchers.withText("Find in page"))
private fun reportIssueButton() = onView(ViewMatchers.withText("Report issue"))
private fun settingsButton() = onView(ViewMatchers.withText("Settings"))
private fun addToHomescreenButton() = onView(ViewMatchers.withText("Add to Home screen"))
private fun addOnsButton() = onView(ViewMatchers.withText("Add-ons"))
private fun syncedTabsButton() = onView(ViewMatchers.withText("Synced Tabs"))
private fun clearCenoButton() = onView(ViewMatchers.withText("Clear Ceno"))
private fun addToShortcutsButton() = onView(ViewMatchers.withText("Add to shortcuts"))
private fun removeFromShortcutsButton() = onView(ViewMatchers.withText("Remove from shortcuts"))
private fun httpsByDefaultButton() = onView(ViewMatchers.withText("HTTPS by default"))
private fun ublockOriginButton() = onView(ViewMatchers.withText("uBlock Origin"))

private fun assertShareButtonDoesntExist() = shareButton().check(ViewAssertions.doesNotExist())
private fun assertRequestDesktopSiteToggleDoesntExist() =
    requestDesktopSiteToggle().check(ViewAssertions.doesNotExist())
private fun assertFindInPageButtonDoesntExist() = findInPageButton().check(ViewAssertions.doesNotExist())
private fun assertForwardButtonDoesntExist() = forwardButton().check(ViewAssertions.doesNotExist())
private fun assertRefreshButtonDoesntExist() = refreshButton().check(ViewAssertions.doesNotExist())
private fun assertStopButtonDoesntExist() = stopButton().check(ViewAssertions.doesNotExist())
private fun assertAddToHomescreenButtonDoesntExist() = addToHomescreenButton()
    .check(ViewAssertions.doesNotExist())

private fun assertAddToShortcutsButtonDoesntExist() = addToShortcutsButton()
    .check(ViewAssertions.doesNotExist())

private fun assertBackButton() = backButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertForwardButton() = forwardButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertRefreshButton() = refreshButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertStopButton() = stopButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertShareButton() = shareButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertRequestDesktopSiteToggle() = requestDesktopSiteToggle()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertAddToHomescreenButton() = addToHomescreenButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertFindInPageButton() = findInPageButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertAddOnsButton() = addOnsButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSyncedTabsButton() = syncedTabsButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertReportIssueButton() = reportIssueButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSettingsButton() = settingsButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertRequestDesktopSiteIsTurnedOff() {
    assertFalse(
        mDevice.findObject(UiSelector().textContains("Request desktop site")).isChecked,
    )
}
private fun assertRequestDesktopSiteIsTurnedOn() {
    assertTrue(
        mDevice.findObject(UiSelector().textContains("Request desktop site")).isChecked,
    )
}
private fun assertClearCenoButton() = clearCenoButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertAddToShortcutsButton() = addToShortcutsButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertRemoveFromShortcutsButton() = removeFromShortcutsButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertHttpsByDefaultButton() = httpsByDefaultButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertUblockOriginButton() = ublockOriginButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
