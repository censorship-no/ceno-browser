package ie.equalit.ceno.bookmarks

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import ie.equalit.ceno.databinding.WebsiteListItemViewBinding
import ie.equalit.ceno.ext.components
import mozilla.components.browser.icons.IconRequest
import mozilla.components.concept.menu.MenuController
import mozilla.components.concept.menu.Orientation

class WebsiteListItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding = WebsiteListItemViewBinding.inflate(
        LayoutInflater.from(context),
        this,
        true,
    )

    val titleView: TextView get() = binding.title

    val urlView: TextView get() = binding.url

    val iconView: ImageView get() = binding.favicon

    val overflowView: ImageButton get() = binding.overflowMenu

    /**
     * Change visibility of parts of this view based on what type of item is being represented.
     */
    fun displayAs(mode: ItemType) {
        urlView.isVisible = mode == ItemType.SITE
    }

    /**
     * Changes the icon to show a check mark if [isSelected]
     */
    fun changeSelected(isSelected: Boolean) {
        binding.icon.displayedChild = if (isSelected) 1 else 0
    }

    fun loadFavicon(url: String) {
        context.components.core.icons.loadIntoView(iconView, IconRequest(url))
    }

    fun attachMenu(menuController: MenuController) {
        overflowView.setOnClickListener {
            menuController.show(
                anchor = it,
                orientation = Orientation.DOWN,
            )
        }
    }

//    fun <T> setSelectionInteractor(item: T, holder: SelectionHolder<T>, interactor: SelectionInteractor<T>) {
//        setOnClickListener {
//            val selected = holder.selectedItems
//            when {
//                selected.isEmpty() -> interactor.open(item)
//                item in selected -> interactor.deselect(item)
//                else -> interactor.select(item)
//            }
//        }
//
//        setOnLongClickListener {
//            if (holder.selectedItems.isEmpty()) {
//                interactor.select(item)
//                true
//            } else {
//                false
//            }
//        }
//
//        iconView.setOnClickListener {
//            if (item in holder.selectedItems) {
//                interactor.deselect(item)
//            } else {
//                interactor.select(item)
//            }
//        }
//    }

    enum class ItemType {
        SITE, FOLDER
    }
}
