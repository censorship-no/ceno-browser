package ie.equalit.ceno.bookmarks

import androidx.annotation.WorkerThread
import mozilla.appservices.places.BookmarkRoot
import mozilla.appservices.places.uniffi.PlacesApiException
import mozilla.components.concept.storage.BookmarksStorage
import mozilla.components.concept.storage.HistoryStorage

//copied from Fenix
/**
 * Use cases that allow for modifying and retrieving bookmarks.
 */
class BookmarksUseCase (
    bookmarksStorage: BookmarksStorage,
    historyStorage: HistoryStorage,
){
    class AddBookmarksUseCase internal constructor(private val storage: BookmarksStorage) {

        /**
         * Adds a new bookmark with the provided [url] and [title].
         *
         * @return The result if the operation was executed or not. A bookmark may not be added if
         * one with the identical [url] already exists.
         */
        @WorkerThread
        suspend operator fun invoke(
            url: String,
            title: String,
            position: UInt? = null,
            parentGuid: String? = null,
        ): Boolean {
            return try {
                val canAdd = storage.getBookmarksWithUrl(url).firstOrNull { it.url == url } == null

                if (canAdd) {
                    storage.addItem(
                        parentGuid ?: BookmarkRoot.Mobile.id,
                        url = url,
                        title = title,
                        position = position,
                    )
                }
                canAdd
            } catch (e: PlacesApiException.UrlParseFailed) {
                false
            }
        }
    }

    val addBookmark by lazy { AddBookmarksUseCase(bookmarksStorage) }
}