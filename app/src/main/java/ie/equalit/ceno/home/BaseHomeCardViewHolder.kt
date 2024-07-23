package ie.equalit.ceno.home

import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.home.sessioncontrol.HomePageInteractor

open class BaseHomeCardViewHolder(
    itemView: View,
    val interactor: HomePageInteractor
) : RecyclerView.ViewHolder(itemView),
    View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener, View.OnClickListener {

    var cardType: HomepageCardType = HomepageCardType.BASIC_MESSAGE_CARD

    init {
        itemView.setOnClickListener(this)
    }

    fun enableContextMenu() {
        itemView.setOnCreateContextMenuListener(this)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        view: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        val learnMore = menu?.add(Menu.NONE, LEARN_MORE, 1, "Learn More")
        val close = menu?.add(Menu.NONE, CLOSE, 2, "Close")

        learnMore?.setOnMenuItemClickListener(this)
        close?.setOnMenuItemClickListener(this)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            LEARN_MORE -> {
                interactor.onMenuItemClicked(cardType)
            }

            CLOSE -> {
                interactor.onRemoveCard(cardType)
            }
        }
        return false
    }

    companion object {
        const val LEARN_MORE: Int = 0
        const val CLOSE: Int = 1
    }

    override fun onClick(p0: View?) {
        /*
        if (cardType == HomepageCardType.BASIC_MESSAGE_CARD) {
            interactor.onClicked(cardType)
        }
        */
    }
}