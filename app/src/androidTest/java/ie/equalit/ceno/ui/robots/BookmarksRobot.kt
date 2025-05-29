package ie.equalit.ceno.ui.robots

import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.uiautomator.By.res
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import ie.equalit.ceno.R
import ie.equalit.ceno.helpers.TestAssetHelper.waitingTime
import ie.equalit.ceno.helpers.TestAssetHelper.waitingTimeShort
import ie.equalit.ceno.helpers.TestHelper.packageName
import ie.equalit.ceno.helpers.click
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not

class BookmarksRobot {
    fun verifyBookmarksView() = assertBookmarksView()
    fun verifyAddFolderButton() = assertAddFolderButton()
    fun verifyEmptyBookmarksView() = assertEmptyBookmarkView()
    fun verifyBookmarksListView() = assertBookmarksListView()
    fun verifyEditBookmarksView() = assertEditBookmarksView()

    fun verifyBookmarkTitle(title:String) = assertBookmarkTitle(title)
    fun verifyBookmarkedURL(url : String) = assertBookmarkUrl(url)
    fun verifyBookmarkFavicon(url : Uri) = assertBookmarkFavicon(url.toString())

    fun verifyFolderTitle(title: String) {
        mDevice.findObject(UiSelector().text(title)).waitForExists(waitingTime)
        onView(withText(title)).check(matches(isDisplayed()))
    }

    private fun assertBookmarksView() {
        onView(withText(R.string.library_bookmarks)).check(matches(isDisplayed()))
    }

    private fun assertAddFolderButton() {
        addBookmarksFolderButton().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    private fun assertEmptyBookmarkView() {
        onView(withId(R.id.bookmarks_empty_view)).check(matches(isDisplayed()))
    }
    private fun assertBookmarksListView() {
        onView(withId(R.id.bookmark_list)).check(matches(isDisplayed()))
    }
    private fun assertBookmarkTitle(title: String) {
        onView(withText(title)).check(matches(isDisplayed()))
    }
    private fun assertBookmarkUrl(url: String) {
        onView(withText(url)).check(matches(isDisplayed()))
    }
    private fun assertBookmarkFavicon(url:String) = onView(
        allOf(
            withId(R.id.favicon),
            withParent(
                withParent(
                    withChild(allOf(withId(R.id.url), withText(url))),
                ),
            ),
        )
    ).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

    private fun assertEditBookmarksView() {
        bookmarkNameEditBox().check(matches(isDisplayed()))
        bookmarkUrlEditBox().check(matches(isDisplayed()))
        saveBookmarkButton().check(matches(isDisplayed()))
        deleteInEditModeButton().check(matches(isDisplayed()))
    }

    fun navigateUp() {
        goBackButton().click()
    }

    fun changeBookmarkTitle(newTitle: String) {
        bookmarkNameEditBox().perform(clearText())
        bookmarkNameEditBox().perform(typeText(newTitle))
    }

    fun changeBookmarkUrl(newUrl: String) {
        bookmarkUrlEditBox().perform(clearText())
        bookmarkUrlEditBox().perform(typeText(newUrl))
    }
    fun saveEditBookmark() {
        saveBookmarkButton().click()
        verifyBookmarksListView()
    }

    fun verifyBookmarkIsDeleted(expectedTitle: String) {
        onView(withText(expectedTitle)).check(doesNotExist())
    }

    fun clickParentFolderSelector() {
        bookmarkFolderSelector().click()
    }
    fun clickAddNewFolderButtonFromSelectFolderView() {
        addFolderButton().click()
    }
    fun addNewFolderName(name:String) {
        addFolderTitleField().click()
        addFolderTitleField().perform(replaceText(name))
    }
    fun saveNewFolder() {
        saveFolderButton().click()
    }
    fun selectFolder(folderName:String) {
        onView(withText(folderName)).click()
    }
    fun createFolder(name:String, parent:String? = null) {
        addBookmarksFolderButton().click()
        addNewFolderName(name)
        if (!parent.isNullOrBlank()) {
            setParentFolder(parent)
        }
        saveNewFolder()
    }
    fun setParentFolder(parentName: String) {
        clickParentFolderSelector()
        selectFolder(parentName)
        navigateUp()
    }

    fun clickDeleteInEditModeButton() {
        deleteInEditModeButton().click()
    }
    fun cancelDeletion() {
        onView(withText(R.string.dialog_cancel)).check(matches(isDisplayed()))
        onView(withText(R.string.dialog_cancel)).click()
    }

    fun confirmDeletion() {
        onView(withText(R.string.dialog_btn_positive_ok)).check(matches(isDisplayed()))
        onView(withText(R.string.dialog_btn_positive_ok)).click()
    }

    fun cancelFolderDeletion() {
        onView(withText(R.string.delete_browsing_data_prompt_cancel))
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
            .click()
    }
    fun confirmFolderDeletion() {
        onView(withText(R.string.delete_browsing_data_prompt_allow)).check(matches(isDisplayed()))
        onView(withText(R.string.delete_browsing_data_prompt_allow)).click()
    }
    fun waitForBookmarksFolderContentToExist(parentFolderName: String, childFolderName: String) {
        mDevice.findObject(UiSelector().text(parentFolderName)).waitForExists(waitingTime)
        mDevice.findObject(UiSelector().text(childFolderName)).waitForExists(waitingTime)
    }

    fun verifyCurrentFolderTitle(title: String) {
        mDevice.findObject(
            UiSelector().resourceId("$packageName:id/navigationToolbar")
                .textContains(title),
        ).waitForExists(waitingTimeShort)
        onView(
            allOf(
                withText(title),
                withParent(withId(R.id.action_bar)),
            ),
        ).check(matches(isDisplayed()))
    }
    class Transition {
        fun openThreeDotMenu(bookmark: String, interact: ThreeDotMenuBookmarksRobot.() -> Unit): ThreeDotMenuBookmarksRobot.Transition {
            mDevice.wait(Until.findObject(res("$packageName:id/overflow_menu")), waitingTime)
            threeDotMenu(bookmark).click()

            ThreeDotMenuBookmarksRobot().interact()
            return ThreeDotMenuBookmarksRobot.Transition()
        }

        fun openBookmarkWithTitle(bookmarkTitle: String, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            mDevice.findObject(UiSelector().resourceId("$packageName:id/title").text(bookmarkTitle))
                .also {
                    it.waitForExists(waitingTime)
                    it.clickAndWaitForNewWindow(waitingTimeShort)
                }

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

    }
}

private fun threeDotMenu(bookmark: String) = onView(
    allOf(
        withId(R.id.overflow_menu),
        hasSibling(withText(bookmark)),
    ),
)

private fun addBookmarksFolderButton() = onView(withId(R.id.add_bookmark_folder))
private fun goBackButton() = onView(withContentDescription("Navigate up"))
private fun bookmarkNameEditBox() = onView(withId(R.id.bookmarkNameEdit))
private fun bookmarkUrlEditBox() = onView(withId(R.id.bookmarkUrlEdit))
private fun saveBookmarkButton() = onView(withId(R.id.save_bookmark_button))
private fun deleteInEditModeButton() = onView(withId(R.id.delete_bookmark_button))
private fun bookmarkFolderSelector() = onView(withId(R.id.bookmarkParentFolderSelector))

private fun addFolderTitleField() = onView(withId(R.id.bookmarkNameEdit))
private fun saveFolderButton() = onView(withId(R.id.confirm_add_folder_button))
private fun addFolderButton() = onView(withId(R.id.add_folder_button))