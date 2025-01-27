package ie.equalit.ceno.bookmarks

import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.BookmarkListItemBinding
import ie.equalit.ceno.ext.ceno.loadIntoView
import ie.equalit.ceno.ext.components
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.concept.menu.Orientation
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType

class BookmarkNodeViewHolder(
    private val  itemView: View,
    private val interactor: BookmarkViewInteractor
): RecyclerView.ViewHolder(itemView) {
    var item: BookmarkNode? = null

    private val binding = BookmarkListItemBinding.bind(itemView)

    private val menu: BookmarkItemMenu

    init {
        menu = BookmarkItemMenu(itemView.context) { menuItem ->
            val item = this.item ?: return@BookmarkItemMenu
            when (menuItem) {
                BookmarkItemMenu.Item.Edit -> interactor.onEditPressed(item)
                BookmarkItemMenu.Item.Copy -> interactor.onCopyPressed(item)
                BookmarkItemMenu.Item.Share -> interactor.onSharePressed(item)
                BookmarkItemMenu.Item.OpenInNewTab -> interactor.onOpenInNormalTab(item)
                BookmarkItemMenu.Item.OpenInPersonalTab -> interactor.onOpenInPersonalTab(item)
                BookmarkItemMenu.Item.Delete -> interactor.onDelete(setOf(item))
            }
        }

//        itemView.attachMenu(menu.menuController)
    }

    fun bind(
        item: BookmarkNode,
        mode: BookmarkFragmentState.Mode,
        payload: BookmarkPayload,
    ) {
        this.item = item

        binding.title.text = item.title
        binding.url.text = item.url
        binding.url.isVisible = item.type == BookmarkNodeType.ITEM
        binding.overflowMenu.setOnClickListener {
            Log.d("BOOKMARK", "Bookmark Menu")
            menu.menuController.show(
                anchor = it,
                orientation = Orientation.DOWN
            )
        }

        CoroutineScope(Dispatchers.Default).launch {
            menu.updateMenu(item.type, item.guid)
        }
//        containerView.setSelectionInteractor(item, mode, interactor)

//        CoroutineScope(Dispatchers.Default).launch {
//            menu.updateMenu(item.type, item.guid)
//        }

        // Hide menu button if this item is a root folder or is selected
//        if (item.type == BookmarkNodeType.FOLDER && item.inRoots()) {
//            containerView.overflowView.removeAndDisable()
//        } else if (payload.modeChanged) {
//            if (mode is BookmarkFragmentState.Mode.Selecting) {
//                containerView.overflowView.hideAndDisable()
//            } else {
//                containerView.overflowView.showAndEnable()
//            }
//        }

//        if (payload.selectedChanged) {
//            containerView.changeSelected(item in mode.selectedItems)
//        }
//        val useTitleFallback = item.type == BookmarkNodeType.ITEM && item.title.isNullOrBlank()
//        if (payload.titleChanged) {
//            containerView.titleView.text = if (useTitleFallback) item.url else item.title
//        } else if (payload.urlChanged && useTitleFallback) {
//            containerView.titleView.text = item.url
//        }
//
//        if (payload.urlChanged) {
//            containerView.urlView.text = item.url
//        }

        if (payload.iconChanged) {
            updateIcon(item)
        }
    }

    private fun updateIcon(item: BookmarkNode) {
        val context = itemView.context
        val iconView = binding.favicon
        val url = item.url

        when {
            // Item is a folder
            item.type == BookmarkNodeType.FOLDER ->
                iconView.setImageResource(R.drawable.ic_folder_icon)
            // Item has a http/https URL
            url != null && url.startsWith("http") ->
                context.components.core.icons.loadIntoView(iconView, url)
            else ->
                iconView.setImageDrawable(null)
        }
    }

    companion object {
        const val LAYOUT_ID = R.layout.bookmark_list_item
    }
}