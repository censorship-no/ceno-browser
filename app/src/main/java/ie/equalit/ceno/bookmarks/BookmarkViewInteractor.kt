package ie.equalit.ceno.bookmarks

import mozilla.components.concept.storage.BookmarkNode

interface BookmarkViewInteractor {

    /**
     * Swaps the head of the bookmarks tree, replacing it with a new, updated bookmarks tree.
     *
     * @param node the head node of the new bookmarks tree
     */
    fun onBookmarksChanged(node: BookmarkNode)

    /**
     * Switches the current bookmark multi-selection mode.
     *
     * @param mode the multi-select mode to switch to
     */
    fun onSelectionModeSwitch(mode: BookmarkFragmentState.Mode)

    /**
     * Opens up an interface to edit a bookmark node.
     *
     * @param node the bookmark node to edit
     */
    fun onEditPressed(node: BookmarkNode)

    /**
     * De-selects all bookmark nodes, clearing the multi-selection mode.
     *
     */
    fun onAllBookmarksDeselected()

    /**
     * Copies the URL of a bookmark item to the copy-paste buffer.
     *
     * @param item the bookmark item to copy the URL from
     */
    fun onCopyPressed(item: BookmarkNode)

    /**
     * Opens the share sheet for a bookmark item.
     *
     * @param item the bookmark item to share
     */
    fun onSharePressed(item: BookmarkNode)

    /**
     * Opens a bookmark item in a new tab.
     *
     * @param item the bookmark item to open in a new tab
     */
    fun onOpenInNormalTab(item: BookmarkNode)

    /**
     * Opens a bookmark item in a private tab.
     *
     * @param item the bookmark item to open in a personal tab
     */
    fun onOpenInPersonalTab(item: BookmarkNode)

    /**
     * Deletes a set of bookmark nodes.
     *
     * @param nodes the bookmark nodes to delete
     */
    fun onDelete(nodes: Set<BookmarkNode>)

    /**
     * Handles back presses for the bookmark screen, so navigation up the tree is possible.
     *
     */
    fun onBackPressed()

    /**
     * Handles when search is tapped
     */
    fun onSearch()

    /**
     * Called when an item is tapped to open it.
     * @param item the tapped item to open.
     */
    fun open(item: BookmarkNode)

    /**
     * Called when an item is long pressed and selection mode is started,
     * or when selection mode has already started an an item is tapped.
     * @param item the item to select.
     */
    fun select(item: BookmarkNode)

    /**
     * Called when a selected item is tapped in selection mode and should no longer be selected.
     * @param item the item to deselect.
     */
    fun deselect(item: BookmarkNode)
}