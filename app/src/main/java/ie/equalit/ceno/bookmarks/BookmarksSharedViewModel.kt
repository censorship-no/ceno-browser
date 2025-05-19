package ie.equalit.ceno.bookmarks

import androidx.lifecycle.ViewModel
import mozilla.components.concept.storage.BookmarkNode


/**
 * [ViewModel] that shares data between various bookmarks fragments.
 */
class BookmarksSharedViewModel : ViewModel() {
    /**
     * The currently selected bookmark root.
     */
    var selectedFolder: BookmarkNode? = null
}