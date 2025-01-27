package ie.equalit.ceno.bookmarks

import ie.equalit.ceno.browser.BrowsingMode
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType

class BookmarkFragmentInteractor(
    private val bookmarksController: BookmarkController,
):BookmarkViewInteractor {
    override fun onBookmarksChanged(node: BookmarkNode) {
        bookmarksController.handleBookmarkChanged(node)
    }

    override fun onSelectionModeSwitch(mode: BookmarkFragmentState.Mode) {
        TODO("Not yet implemented")
    }

    override fun onEditPressed(node: BookmarkNode) {
        bookmarksController.handleBookmarkEdit(node)
    }

    override fun onAllBookmarksDeselected() {
        TODO("Not yet implemented")
    }

    override fun onCopyPressed(item: BookmarkNode) {
        require(item.type == BookmarkNodeType.ITEM)
        item.url?.let {
            bookmarksController.handleCopyUrl(item)
        }
    }

    override fun onSharePressed(item: BookmarkNode) {
        require(item.type == BookmarkNodeType.ITEM)
        item.url?.let {
            bookmarksController.handleBookmarkSharing(item)
        }
    }

    override fun onOpenInNormalTab(item: BookmarkNode) {
        require(item.type == BookmarkNodeType.ITEM)
        item.url?.let {
            bookmarksController.handleOpeningBookmark(item, BrowsingMode.Normal)
        }
    }

    override fun onOpenInPersonalTab(item: BookmarkNode) {
        require(item.type == BookmarkNodeType.ITEM)
        item.url?.let {
            bookmarksController.handleOpeningBookmark(item, BrowsingMode.Personal)
        }
    }

    override fun onDelete(nodes: Set<BookmarkNode>) {
        if (nodes.find { it.type == BookmarkNodeType.SEPARATOR } != null) {
            throw IllegalStateException("Cannot delete separators")
        }
        val eventType = when (nodes.singleOrNull()?.type) {
            BookmarkNodeType.ITEM,
            BookmarkNodeType.SEPARATOR,
                -> BookmarkRemoveType.SINGLE
            BookmarkNodeType.FOLDER -> BookmarkRemoveType.FOLDER
            null -> BookmarkRemoveType.MULTIPLE
        }
        if (eventType == BookmarkRemoveType.FOLDER) {
            bookmarksController.handleBookmarkFolderDeletion(nodes)
        } else {
            bookmarksController.handleBookmarkDeletion(nodes, eventType)
        }
    }

    override fun onBackPressed() {
        TODO("Not yet implemented")
    }

    override fun onSearch() {
        TODO("Not yet implemented")
    }

    override fun open(item: BookmarkNode) {
        TODO("Not yet implemented")
    }

    override fun select(item: BookmarkNode) {
        TODO("Not yet implemented")
    }

    override fun deselect(item: BookmarkNode) {
        TODO("Not yet implemented")
    }
}