package ie.equalit.ceno.ui

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import ie.equalit.ceno.helpers.AndroidAssetDispatcher
import ie.equalit.ceno.helpers.BrowserActivityTestRule
import ie.equalit.ceno.helpers.MockBrowserDataHelper.createBookmarkItem
import ie.equalit.ceno.helpers.MockBrowserDataHelper.generateBookmarkFolder
import ie.equalit.ceno.helpers.RetryTestRule
import ie.equalit.ceno.helpers.TestAssetHelper
import ie.equalit.ceno.ui.robots.navigationToolbar
import ie.equalit.ceno.ui.robots.onboarding
import ie.equalit.ceno.ui.robots.standby
import junit.extensions.TestSetup
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BookmarksTest {
    private val bookmarksFolderName = "New Folder"
    private val testBookmark = object {
        var title: String = "Bookmark title"
        var url: String = "https://www.example.com"
    }

    private val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val activityTestRule = BrowserActivityTestRule()

    @Rule
    @JvmField
    val retryTestRule = RetryTestRule(1)

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply {
            dispatcher = AndroidAssetDispatcher()
            start()
        }
        standby {
        }.waitForStandbyIfNeeded()
        onboarding {
        }.skipOnboardingIfNeeded()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun verifyEmptyBookmarksMenuTest() {
        mDevice.waitForIdle()
        navigationToolbar  {
        }.openThreeDotMenu {
        }.openBookmarks {
            verifyBookmarksView()
            verifyAddFolderButton()
            verifyEmptyBookmarksView()
        }
    }

    @Test
    fun verifyAddBookmarkButtonTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(defaultWebPage.url) {
            verifyPageContent("Page content: 1")
        }
        navigationToolbar {
        }.openThreeDotMenu {
            verifyAddBookmarksButtonExists()
        }.bookmarkPage {
//            verifySnackBarText("Saved in “Bookmarks”")
        }
        navigationToolbar {
        }.openThreeDotMenu {
        }.openBookmarks {
            verifyBookmarksListView()
            verifyBookmarkTitle(defaultWebPage.title)
            verifyBookmarkedURL(defaultWebPage.url.toString())
            verifyBookmarkFavicon(defaultWebPage.url)
        }
    }

    @Test
    fun editBookmarksNameAndUrlTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        createBookmarkItem(defaultWebPage.url.toString(), defaultWebPage.title, null)
        navigationToolbar {
        }.openThreeDotMenu {
        }.openBookmarks {
            verifyBookmarksView()
            verifyBookmarkTitle(defaultWebPage.title)
        }.openThreeDotMenu(defaultWebPage.title) {
        }.clickEdit {
            verifyEditBookmarksView()
            changeBookmarkTitle(testBookmark.title)
            changeBookmarkUrl(testBookmark.url)
            saveEditBookmark()

            verifyBookmarksView()
            verifyBookmarkTitle(testBookmark.title)
        }.openBookmarkWithTitle(testBookmark.title) {
            verifyUrl("example.com")
        }
    }


    @Test
    fun copyBookmarkURLTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        createBookmarkItem(defaultWebPage.url.toString(), defaultWebPage.title, null)
        navigationToolbar {
        }.openThreeDotMenu {
        }.openBookmarks {
            verifyBookmarksView()
            verifyBookmarkTitle(defaultWebPage.title)
        }.openThreeDotMenu(defaultWebPage.title) {
        }.clickCopy {
//            verifySnackBarText(expectedText = "URL copied")
            navigateUp()
        }

        navigationToolbar {
        }.clickToolbar {
            longClickToolbar()
            clickPasteText()
            verifyLinkFromClipboard(defaultWebPage.url.toString())
        }
    }

    @Test
    fun shareBookmarkTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        createBookmarkItem(defaultWebPage.url.toString(), defaultWebPage.title, null)
        navigationToolbar {
        }.openThreeDotMenu {
        }.openBookmarks {
            verifyBookmarksView()
            verifyBookmarkTitle(defaultWebPage.title)
        }.openThreeDotMenu(defaultWebPage.title) {
        }.clickShare {
            verifyShareTabLayout()
        }
    }

    @Test
    fun openBookmarkInNewTabTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        createBookmarkItem(defaultWebPage.url.toString(), defaultWebPage.title, null)
        navigationToolbar {
        }.openThreeDotMenu {
        }.openBookmarks {
            verifyBookmarksView()
            verifyBookmarkTitle(defaultWebPage.title)
        }.openThreeDotMenu(defaultWebPage.title) {
        }.clickOpenInNewTab() {
            verifyUrl(defaultWebPage.displayUrl)
        }
        navigationToolbar {
        }.openTabTrayMenu {
            verifyRegularBrowsingTab()
        }
    }

    @Test
    fun openBookmarkInPersonalTabTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        createBookmarkItem(defaultWebPage.url.toString(), defaultWebPage.title, null)

        navigationToolbar {
        }.openThreeDotMenu {
        }.openBookmarks {
            verifyBookmarksView()
            verifyBookmarkTitle(defaultWebPage.title)
        }.openThreeDotMenu(defaultWebPage.title) {
        }.clickOpenInPersonalTab() {
            verifyPageLoaded()
            verifyUrl(defaultWebPage.displayUrl)
        }
        navigationToolbar {
        }.openTabTrayMenu {
            verifyPrivateBrowsingTab()
        }
    }
    @Test
    fun deleteBookmarkTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(defaultWebPage.url) {
            verifyPageContent("Page content: 1")
        }
        navigationToolbar {
        }.openThreeDotMenu {
            verifyAddBookmarksButtonExists()
        }.bookmarkPage {
        }
        navigationToolbar {
        }.openThreeDotMenu {
        }.openBookmarks {
            verifyBookmarksView()
            verifyBookmarkTitle(defaultWebPage.title)
        }.openThreeDotMenu(defaultWebPage.title) {
        }.clickDelete {
            verifyBookmarkIsDeleted(defaultWebPage.title)
        }
    }

    @Test
    fun createBookmarkFolderTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        createBookmarkItem(defaultWebPage.url.toString(), defaultWebPage.title, null)

        navigationToolbar {
        }.openThreeDotMenu {
        }.openBookmarks {
            verifyBookmarksListView()
        }.openThreeDotMenu(defaultWebPage.title) {
        }.clickEdit {
            clickParentFolderSelector()
            clickAddNewFolderButtonFromSelectFolderView()
            addNewFolderName(bookmarksFolderName)
            saveNewFolder()
            navigateUp()
            saveEditBookmark()
            selectFolder(bookmarksFolderName)
            verifyBookmarkedURL(defaultWebPage.url.toString())
        }
    }
//    @Test
//    fun navigateBookmarksFoldersTest() {
//        navigationToolbar {
//        }.openThreeDotMenu {
//        }.openBookmarks {
//            createFolder("1")
//            waitForBookmarksFolderContentToExist("Bookmarks", "1")
//            selectFolder("1")
//            verifyCurrentFolderTitle("1")
//            createFolder("2")
//            waitForBookmarksFolderContentToExist("1", "2")
//            selectFolder("2")
//            verifyCurrentFolderTitle("2")
//            navigateUp()
//            waitForBookmarksFolderContentToExist("1", "2")
//            verifyCurrentFolderTitle("1")
//            mDevice.pressBack()
//            verifyBookmarksMenuView()
//        }
//    }
    @Test
    fun deleteBookmarkInEditModeTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        createBookmarkItem(defaultWebPage.url.toString(), defaultWebPage.title, null)

        navigationToolbar {
        }.openThreeDotMenu {
        }.openBookmarks {
            verifyBookmarksListView()
        }.openThreeDotMenu(defaultWebPage.title) {
        }.clickEdit {
            clickDeleteInEditModeButton()
            cancelDeletion()
            clickDeleteInEditModeButton()
            confirmDeletion()
            verifyBookmarkIsDeleted("Test_Page_1")
        }
    }

    @Test
    fun deleteBookmarkFoldersTest() {
        val website = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        val myFolder = generateBookmarkFolder(title = "My Folder", position = null)
        generateBookmarkFolder(myFolder, title = "My Folder 2", position = null)
        createBookmarkItem(website.url.toString(), website.title, null, myFolder)

        navigationToolbar {
        }.openThreeDotMenu {
        }.openBookmarks {
        }.openThreeDotMenu("My Folder") {
        }.clickDelete {
            cancelFolderDeletion()
            verifyFolderTitle("My Folder")
        }.openThreeDotMenu("My Folder") {
        }.clickDelete {
            confirmFolderDeletion()
            verifyBookmarkIsDeleted("My Folder")
            verifyBookmarkIsDeleted("My Folder 2")
            verifyBookmarkIsDeleted("Test_Page_1")
            navigateUp()
        }
    }
}