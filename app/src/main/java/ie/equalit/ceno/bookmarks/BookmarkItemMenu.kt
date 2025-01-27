package ie.equalit.ceno.bookmarks

import android.content.Context
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.components
import mozilla.components.browser.menu2.BrowserMenuController
import mozilla.components.concept.menu.MenuController
import mozilla.components.concept.menu.candidate.TextMenuCandidate
import mozilla.components.concept.menu.candidate.TextStyle
import mozilla.components.concept.storage.BookmarkNodeType
import mozilla.components.support.ktx.android.content.getColorFromAttr

class BookmarkItemMenu (
    private val context: Context,
    private val onItemTapped: (Item) -> Unit
) {
    enum class Item {
        Edit,
        Copy,
        Share,
        OpenInNewTab,
        OpenInPersonalTab,
        Delete,
        ;
    }
    val menuController: MenuController by lazy { BrowserMenuController() }

    internal suspend fun menuItems(itemType: BookmarkNodeType, itemId: String): List<TextMenuCandidate> {
        val hasAtLeastOneChild = !context.components.core.bookmarksStorage.getTree(itemId)?.children.isNullOrEmpty()

        return listOfNotNull(
            if (itemType != BookmarkNodeType.SEPARATOR) {
                TextMenuCandidate(
                    text = context.getString(R.string.bookmark_menu_edit_button),
                ) {
                    onItemTapped.invoke(Item.Edit)
                }
            } else {
                null
            },
            if (itemType == BookmarkNodeType.ITEM) {
                TextMenuCandidate(
                    text = context.getString(R.string.bookmark_menu_copy_button),
                ) {
                    onItemTapped.invoke(Item.Copy)
                }
            } else {
                null
            },
            if (itemType == BookmarkNodeType.ITEM) {
                TextMenuCandidate(
                    text = context.getString(R.string.bookmark_menu_share_button),
                ) {
                    onItemTapped.invoke(Item.Share)
                }
            } else {
                null
            },
            if (itemType == BookmarkNodeType.ITEM) {
                TextMenuCandidate(
                    text = context.getString(R.string.bookmark_menu_open_in_new_tab_button),
                ) {
                    onItemTapped.invoke(Item.OpenInNewTab)
                }
            } else {
                null
            },
            if (itemType == BookmarkNodeType.ITEM) {
                TextMenuCandidate(
                    text = context.getString(R.string.bookmark_menu_open_in_private_tab_button),
                ) {
                    onItemTapped.invoke(Item.OpenInPersonalTab)
                }
            } else {
                null
            },
            TextMenuCandidate(
                text = context.getString(R.string.bookmark_menu_delete_button),
                textStyle = TextStyle(color = context.getColorFromAttr(R.attr.textWarning)),
            ) {
                onItemTapped.invoke(Item.Delete)
            },
        )
    }

    /**
     * Update the menu items for the type of bookmark.
     */
    suspend fun updateMenu(itemType: BookmarkNodeType, itemId: String) {
        menuController.submitList(menuItems(itemType, itemId))
    }
}
