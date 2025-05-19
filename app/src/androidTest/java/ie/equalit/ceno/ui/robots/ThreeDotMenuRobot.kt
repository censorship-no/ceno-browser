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
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.UiSelector
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.TestAssetHelper.waitingTime
import ie.equalit.ceno.helpers.TestHelper.packageName
import ie.equalit.ceno.helpers.click
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import junit.framework.AssertionFailedError
import org.hamcrest.CoreMatchers.allOf

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
    fun verifyEnableReaderViewButton() = assertEnableReaderViewButton()
    fun verifyDisableReaderViewButton() = assertDisableReaderViewButton()
    fun verifyReaderViewButtonDoesntExist() = assertReaderViewButtonDoesntExist()
    fun verifyAddBookmarksButtonExists() = assertAddBookmarksButtonExists()

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

        fun openBookmarks(interact: BookmarksRobot.() -> Unit): BookmarksRobot.Transition {
            bookmarksButton().click()
            mDevice.waitForIdle()
            BookmarksRobot().interact()
            return BookmarksRobot.Transition()
        }

        fun bookmarkPage(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            addOrRemoveBookmarksButton().click()

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
        /*
        fun openSyncedTabs(interact: SyncedTabsRobot.() -> Unit): SyncedTabsRobot.Transition {
            mDevice.findObject(UiSelector().text("Synced Tabs")).waitForExists(waitingTime)
            syncedTabsButton().click()

            SyncedTabsRobot().interact()
            return SyncedTabsRobot.Transition()
        }
        */

        fun goBack(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            backButton().click()
            mDevice.findObject(
                UiSelector()
                    .resourceId("$packageName:id/mozac_browser_toolbar_progress"),
            ).waitUntilGone(waitingTime)
            mDevice.waitForIdle()

            BrowserRobot().interact()
            return BrowserRobot.Transition()
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

        fun clickReaderViewButton(interact: ReaderViewRobot.() -> Unit): ReaderViewRobot.Transition {
            enableReaderViewButton().click()
            mDevice.waitForWindowUpdate(packageName, waitingTime)
            ReaderViewRobot().interact()
            return ReaderViewRobot.Transition()
        }

        fun clickDisableReaderViewButton(interact: ReaderViewRobot.() -> Unit): ReaderViewRobot.Transition {
            disableReaderViewButton().click()
            mDevice.waitForWindowUpdate(packageName, waitingTime)
            ReaderViewRobot().interact()
            return ReaderViewRobot.Transition()
        }
    }
}

private fun threeDotMenuRecyclerViewExists() {
    onView(withId(R.id.mozac_browser_menu_recyclerView)).check(matches(isDisplayed()))
}

private fun threeDotMenuButton() = onView(withId(R.id.mozac_browser_toolbar_menu))
private fun backButton() = onView(allOf(withContentDescription("Back"), isDisplayed()))
private fun forwardButton() = onView(allOf(withContentDescription("Forward"), isDisplayed()))
private fun refreshButton() = onView(withContentDescription("Refresh"))
private fun stopButton() = onView(withContentDescription("Stop"))
private fun shareButton() = onView(allOf(withText(R.string.browser_menu_share), isDisplayed()))
private fun requestDesktopSiteToggle() = onView(withText("Request desktop site"))
private fun findInPageButton() = onView(withText("Find in page"))
private fun reportIssueButton() = onView(withText("Report issue"))
private fun settingsButton() = onView(withText(R.string.browser_menu_settings))
private fun addToHomescreenButton() = onView(withText("Add to Home screen"))
private fun syncedTabsButton() = onView(withText("Synced Tabs"))
private fun clearCenoButton() = onView(withText("Clear Ceno"))
private fun addToShortcutsButton() = onView(withText("Add to shortcuts"))
private fun removeFromShortcutsButton() = onView(withText("Remove from shortcuts"))
private fun httpsByDefaultButton() = onView(withText("HTTPS by default"))
private fun ublockOriginButton() = onView(withText("uBlock Origin"))

private fun enableReaderViewButton() = onView(withText(R.string.browser_menu_enable_reader_view))
private fun disableReaderViewButton() = onView(withText(R.string.browser_menu_disable_reader_view))

private fun addOrRemoveBookmarksButton() = onView(withContentDescription(R.string.add_or_remove_bookmark))
private fun bookmarksButton() = onView(withText(R.string.library_bookmarks))

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
private fun assertReaderViewButtonDoesntExist() = enableReaderViewButton()
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
private fun assertEnableReaderViewButton() = enableReaderViewButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertDisableReaderViewButton() = disableReaderViewButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertAddBookmarksButtonExists() = addOrRemoveBookmarksButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
