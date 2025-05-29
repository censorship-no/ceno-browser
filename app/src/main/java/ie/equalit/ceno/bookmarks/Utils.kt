package ie.equalit.ceno.bookmarks

import android.content.Context
import ie.equalit.ceno.R
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType

fun rootTitles(context: Context): Map<String, String> = mapOf(
        "root" to context.getString(R.string.library_bookmarks),
        "mobile" to context.getString(R.string.library_bookmarks),
        "unfiled" to context.getString(R.string.library_desktop_bookmarks_unfiled)
    )

/**
 * Checks to see if a [BookmarkNode] is a [BookmarkRoot] and if so, returns the user-friendly
 * translated version of its title.
 *
 * @param context The [Context] used in resolving strings.
 * @param node The [BookmarkNode] to resolve a title for.
 * @param rootTitles A map of [BookmarkRoot] titles to their user-friendly strings. Default is defaults.
 */
fun friendlyRootTitle(
    context: Context,
    node: BookmarkNode,
    rootTitles: Map<String, String> = rootTitles(context),
) = when {
    !node.inRoots() -> node.title
    rootTitles.containsKey(node.title) -> rootTitles[node.title]
    else -> node.title
}

data class BookmarkNodeWithDepth(val depth: Int, val node: BookmarkNode, val parent: String?)

fun BookmarkNode.flatNodeList(excludeSubtreeRoot: String?, depth: Int = 0): List<BookmarkNodeWithDepth> {
    if (this.type != BookmarkNodeType.FOLDER || this.guid == excludeSubtreeRoot) {
        return emptyList()
    }
    val newList = listOf(BookmarkNodeWithDepth(depth, this, this.parentGuid))
    return newList + children
        ?.filter { it.type == BookmarkNodeType.FOLDER }
        ?.flatMap { it.flatNodeList(excludeSubtreeRoot = excludeSubtreeRoot, depth = depth + 1) }
        .orEmpty()
}