package ie.equalit.ceno.helpers

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.browser.storage.sync.PlacesBookmarksStorage

object MockBrowserDataHelper {
    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * Adds a new bookmark item, visible in the Bookmarks folder.
     *
     * @param url The URL of the bookmark item to add. URLs should use the "https://example.com" format.
     * @param title The title of the bookmark item to add.
     * @param position Example for the position param: 1u, 2u, etc.
     * @param parentGuid The parent guid of the bookmark item to add.
     * BookmarkRoot.Mobile.id is the root id for mobile bookmarks.
     */
    fun createBookmarkItem(
        url: String,
        title: String,
        position: UInt?,
        parentGuid: String = BookmarkRoot.Mobile.id,
    ) {
        runBlocking {
            PlacesBookmarksStorage(context)
                .addItem(
                    parentGuid,
                    url,
                    title,
                    position,
                )
        }
    }

    /**
     * Adds a new bookmark folder, visible in the Bookmarks folder.
     *
     * @param parentGuid The parent guid of the bookmark folder to add.
     * BookmarkRoot.Mobile.id is the root id for mobile bookmarks.
     * @param title The title of the bookmark folder to add.
     * @param position Example for the position param: null, 1u, 2u, etc.
     * @return The guid of the newly created bookmark folder.
     */
    fun generateBookmarkFolder(
        parentGuid: String = BookmarkRoot.Mobile.id,
        title: String,
        position: UInt?,
    ): String {
        return runBlocking {
            PlacesBookmarksStorage(context)
                .addFolder(
                    parentGuid = parentGuid,
                    title = title,
                    position = position,
                )
        }
    }
}