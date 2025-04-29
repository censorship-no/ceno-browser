package ie.equalit.ceno.bookmarks

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.NavGraphDirections
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.standby.StandbyFragment
import kotlinx.coroutines.CoroutineScope
import mozilla.components.concept.engine.prompt.ShareData
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.feature.tabs.TabsUseCases

interface BookmarkController {
    fun handleBookmarkChanged(item: BookmarkNode)
    fun handleBookmarkTapped(item: BookmarkNode)
    fun handleBookmarkExpand(folder: BookmarkNode)
    fun handleSelectionModeSwitch()
    fun handleBookmarkEdit(node: BookmarkNode)
    fun handleBookmarkSelected(node: BookmarkNode)
    fun handleBookmarkDeselected(node: BookmarkNode)
    fun handleAllBookmarksDeselected()
    fun handleCopyUrl(item: BookmarkNode)
    fun handleBookmarkSharing(item: BookmarkNode)
    fun handleOpeningBookmark(item: BookmarkNode, mode: BrowsingMode)
    fun handleOpeningFolderBookmarks(folder: BookmarkNode, mode: BrowsingMode)
    /**
     * Handle bookmark nodes deletion
     * @param nodes The set of nodes to be deleted.
     * @param removeType Type of removal.
     */
    fun handleBookmarkDeletion(nodes: Set<BookmarkNode>, removeType: BookmarkRemoveType)
    fun handleBookmarkFolderDeletion(nodes: Set<BookmarkNode>)
    fun handleBackPressed()
    fun handleSearch()
}
/**
 * Type of bookmark nodes deleted.
 */
enum class BookmarkRemoveType {
    SINGLE, MULTIPLE, FOLDER
}

class DefaultBookmarkController(
    private val activity: BrowserActivity,
    private val navController: NavController,
    private val clipboardManager: ClipboardManager?,
    private val scope: CoroutineScope,
    private val store: BookmarkFragmentStore,
    private val sharedViewModel: BookmarksSharedViewModel,
    private val tabsUseCases: TabsUseCases?,
    private val loadBookmarkNode: suspend (String, Boolean) -> BookmarkNode?,
//    private val showSnackbar: (String) -> Unit,
    private val deleteBookmarkNodes: (Set<BookmarkNode>, BookmarkRemoveType) -> Unit,
//    private val deleteBookmarkFolder: (Set<BookmarkNode>) -> Unit,
//    private val showTabTray: () -> Unit,
) : BookmarkController {
    override fun handleBookmarkChanged(item: BookmarkNode) {
        sharedViewModel.selectedFolder = item
        store.dispatch(BookmarkFragmentAction.Change(item))
    }

    override fun handleBookmarkTapped(item: BookmarkNode) {
        TODO("Not yet implemented")
    }

    override fun handleBookmarkExpand(folder: BookmarkNode) {
        TODO("Not yet implemented")
    }

    override fun handleSelectionModeSwitch() {
        TODO("Not yet implemented")
    }

    override fun handleBookmarkEdit(node: BookmarkNode) {
        navController.navigate(
            R.id.action_bookmarkFragment_to_editBookmarkFragment,
            bundleOf(BookmarkFragment.BOOKMARK_GUID to node.guid)
        )
    }

    override fun handleBookmarkSelected(node: BookmarkNode) {
        TODO("Not yet implemented")
    }

    override fun handleBookmarkDeselected(node: BookmarkNode) {
        TODO("Not yet implemented")
    }

    override fun handleAllBookmarksDeselected() {
        TODO("Not yet implemented")
    }

    override fun handleCopyUrl(item: BookmarkNode) {
        val urlClipData = ClipData.newPlainText(item.url, item.url)
        clipboardManager?.setPrimaryClip(urlClipData)
    }

    override fun handleBookmarkSharing(item: BookmarkNode) {
        val directions = NavGraphDirections.actionGlobalShareFragment(
            arrayOf(
                ShareData(
                    url = item.url,
                    title = item.title,
                ),
            )
        )
        navController.navigate(directions)
    }

    override fun handleOpeningBookmark(item: BookmarkNode, mode: BrowsingMode) {
        activity.openToBrowser(url = item.url, true, mode == BrowsingMode.Personal)
    }

    override fun handleOpeningFolderBookmarks(folder: BookmarkNode, mode: BrowsingMode) {
        TODO("Not yet implemented")
    }

    override fun handleBookmarkDeletion(nodes: Set<BookmarkNode>, removeType: BookmarkRemoveType) {
        deleteBookmarkNodes(nodes, removeType)
    }

    override fun handleBookmarkFolderDeletion(nodes: Set<BookmarkNode>) {
        TODO("Not yet implemented")
    }

    override fun handleBackPressed() {
        TODO("Not yet implemented")
    }

    override fun handleSearch() {
        TODO("Not yet implemented")
    }

}