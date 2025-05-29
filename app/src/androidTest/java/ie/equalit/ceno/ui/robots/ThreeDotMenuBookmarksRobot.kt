package ie.equalit.ceno.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withText
import ie.equalit.ceno.helpers.click

class ThreeDotMenuBookmarksRobot {

    class Transition {

        fun clickEdit(interact: BookmarksRobot.() -> Unit): BookmarksRobot.Transition {
            editButton().click()

            BookmarksRobot().interact()
            return BookmarksRobot.Transition()
        }

        fun clickCopy(interact: BookmarksRobot.() -> Unit): BookmarksRobot.Transition {
            copyButton().click()

            BookmarksRobot().interact()
            return BookmarksRobot.Transition()
        }

        fun clickShare(interact: ShareOverlayRobot.() -> Unit): ShareOverlayRobot.Transition {
            shareButton().click()

            ShareOverlayRobot().interact()
            return ShareOverlayRobot.Transition()
        }

        fun clickOpenInNewTab(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            openInNewTabButton().click()

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun clickOpenInPersonalTab(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            openInPersonalTabButton().click()

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun clickDelete(interact: BookmarksRobot.() -> Unit): BookmarksRobot.Transition {
            deleteButton().click()

            BookmarksRobot().interact()
            return BookmarksRobot.Transition()
        }
    }
}

private fun editButton() = onView(withText("Edit"))

private fun copyButton() = onView(withText("Copy"))

private fun shareButton() = onView(withText("Share"))

private fun openInNewTabButton() = onView(withText("Open in new tab"))

private fun openInPersonalTabButton() = onView(withText("Open in personal tab"))

private fun deleteButton() = onView(withText("Delete"))