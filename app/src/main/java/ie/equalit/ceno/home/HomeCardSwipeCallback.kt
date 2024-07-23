package ie.equalit.ceno.home

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.home.sessioncontrol.SessionControlInteractor

class HomeCardSwipeCallback(
    dragDirs: Int,
    swipeDirs: Int,
    val interactor: SessionControlInteractor
) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (viewHolder.itemViewType == HomepageCardType.BASIC_MESSAGE_CARD.value
            || viewHolder.itemViewType == HomepageCardType.ANNOUNCEMENTS_CARD.value
        )
            return makeMovementFlags(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            )
        return 0
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        when (viewHolder.itemViewType) {
            HomepageCardType.BASIC_MESSAGE_CARD.value -> interactor.onCardSwipe(HomepageCardType.BASIC_MESSAGE_CARD)
            HomepageCardType.ANNOUNCEMENTS_CARD.value -> interactor.onAnnouncementCardSwiped(
                viewHolder.absoluteAdapterPosition
            )

            else -> interactor.onCardSwipe(HomepageCardType.MODE_MESSAGE_CARD)
        }
    }
}