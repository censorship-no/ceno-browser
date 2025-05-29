package ie.equalit.ceno.bookmarks.edit

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.R
import ie.equalit.ceno.bookmarks.BookmarkNodeWithDepth
import ie.equalit.ceno.bookmarks.BookmarksSharedViewModel
import ie.equalit.ceno.bookmarks.WebsiteListItemView
import ie.equalit.ceno.bookmarks.flatNodeList
import mozilla.components.concept.storage.BookmarkNode

class SelectBookmarkFolderAdapter(private val sharedViewModel: BookmarksSharedViewModel) :
    ListAdapter<BookmarkNodeWithDepth, SelectBookmarkFolderAdapter.BookmarkFolderViewHolder>(DiffCallback)  {

    fun updateData(tree: BookmarkNode?, hideFolderGuid: String?) {
        val updatedData = tree
            ?.flatNodeList(hideFolderGuid)
            .orEmpty()

        submitList(updatedData)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkFolderViewHolder {
        val view = WebsiteListItemView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }

        return BookmarkFolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkFolderViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(item, selected = item.node.isSelected()) { node ->
            val lastSelectedItemPosition = getSelectedItemIndex()

            sharedViewModel.toggleSelection(node)

            notifyItemChanged(position)
            lastSelectedItemPosition
                ?.takeIf { it != position }
                ?.let { notifyItemChanged(it) }
        }
    }

    class BookmarkFolderViewHolder(
        val view: WebsiteListItemView,
    ) : RecyclerView.ViewHolder(view) {

        init {
            view.displayAs(WebsiteListItemView.ItemType.FOLDER)
            view.overflowView.visibility = View.GONE
        }

        fun bind(folder: BookmarkNodeWithDepth, selected: Boolean, onSelect: (BookmarkNode) -> Unit) {
            view.changeSelected(selected)
            view.iconView.setImageResource(R.drawable.ic_folder_icon)
            view.titleView.text = folder.node.title
            view.setOnClickListener {
                onSelect(folder.node)
            }
            val pxToIndent = view.resources.getDimensionPixelSize(R.dimen.bookmark_select_folder_indent)
            val padding = pxToIndent * minOf(MAX_DEPTH, folder.depth)
            view.updatePaddingRelative(start = padding)
        }

        companion object {
            const val viewType = 1
        }
    }

    private fun getSelectedItemIndex(): Int? {
        val selectedNode = sharedViewModel.selectedFolder
        val selectedNodeIndex = currentList.indexOfFirst { it.node == selectedNode }

        return selectedNodeIndex.takeIf { it != -1 }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun BookmarkNode.isSelected(): Boolean =
        this == sharedViewModel.selectedFolder

    companion object {
        private const val MAX_DEPTH = 10
    }
}

private object DiffCallback : DiffUtil.ItemCallback<BookmarkNodeWithDepth>() {

    override fun areItemsTheSame(
        oldItem: BookmarkNodeWithDepth,
        newItem: BookmarkNodeWithDepth,
    ) = oldItem.node.guid == newItem.node.guid

    override fun areContentsTheSame(
        oldItem: BookmarkNodeWithDepth,
        newItem: BookmarkNodeWithDepth,
    ) = oldItem == newItem
}

private fun BookmarksSharedViewModel.toggleSelection(node: BookmarkNode?) {
    selectedFolder = if (selectedFolder == node) null else node
}