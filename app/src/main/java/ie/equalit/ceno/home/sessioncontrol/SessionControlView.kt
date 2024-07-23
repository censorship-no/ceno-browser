package ie.equalit.ceno.home.sessioncontrol

import android.annotation.SuppressLint
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.components.ceno.appstate.AppState
import ie.equalit.ceno.ext.cenoPreferences
import ie.equalit.ceno.home.CenoMessageCard
import ie.equalit.ceno.home.HomeCardSwipeCallback
import ie.equalit.ceno.home.RssItem
import ie.equalit.ceno.settings.CenoSettings
import ie.equalit.ceno.utils.CenoPreferences
import mozilla.components.feature.top.sites.TopSite

// This method got a little complex with the addition of the tab tray feature flag
// When we remove the tabs from the home screen this will get much simpler again.
@Suppress("ComplexMethod", "LongParameterList")
@VisibleForTesting
internal fun normalModeAdapterItems(
    settings: CenoPreferences,
    topSites: List<TopSite>,
    messageCard: CenoMessageCard,
    mode: BrowsingMode,
    announcements: List<RssItem>?,
    isBridgeAnnouncementEnabled: Boolean
): List<AdapterItem> {
    val items = mutableListOf<AdapterItem>()

    // Add a synchronous, unconditional and invisible placeholder so home is anchored to the top when created.
    // IF THIS EVER CHANGES, REMEMBER TO UPDATE THE [_sessionControlInteractor : SessionControlInteractor] constructor in HomeFragment.kt.
    items.add(AdapterItem.TopPlaceholderItem)

    // Show announcements at the top
    announcements?.forEach { items.add(AdapterItem.CenoAnnouncementItem(it, BrowsingMode.Normal)) }

    items.add(AdapterItem.CenoModeItem(mode))

    if (!isBridgeAnnouncementEnabled && settings.showBridgeAnnouncementCard)
        items.add(AdapterItem.CenoMessageItem(messageCard))


    if (/*settings.showTopSitesFeature && */ topSites.isNotEmpty()) {
        items.add(AdapterItem.TopSitePager(topSites))
    }
    return items
}

internal fun personalModeAdapterItems(
    mode: BrowsingMode,
    announcements: List<RssItem>?
): List<AdapterItem> {
    val items = mutableListOf<AdapterItem>()
    // Add a synchronous, unconditional and invisible placeholder so home is anchored to the top when created.
    items.add(AdapterItem.TopPlaceholderItem)
    // Show announcements at the top
    announcements?.forEach {
        items.add(
            AdapterItem.CenoAnnouncementItem(
                it,
                BrowsingMode.Personal
            )
        )
    }

    items.add(AdapterItem.CenoModeItem(mode))
    items.add(AdapterItem.PersonalModeDescriptionItem)

    return items
}

private fun AppState.toAdapterList(
    prefs: CenoPreferences,
    messageCard: CenoMessageCard,
    announcement: List<RssItem>?,
    isBridgeAnnouncementEnabled: Boolean
): List<AdapterItem> = when (mode) {
    BrowsingMode.Normal ->
        normalModeAdapterItems(
            prefs,
            topSites,
            messageCard,
            mode,
            announcement,
            isBridgeAnnouncementEnabled
        )

    BrowsingMode.Personal -> personalModeAdapterItems(mode, announcement)
}


class SessionControlView(
    val containerView: View,
    viewLifecycleOwner: LifecycleOwner,
    internal val interactor: SessionControlInteractor
) {
    val view: RecyclerView = containerView as RecyclerView

    private val sessionControlAdapter = SessionControlAdapter(
        interactor,
        viewLifecycleOwner
    )

    init {
        view.apply {
            adapter = sessionControlAdapter
            layoutManager = object : LinearLayoutManager(containerView.context) {
                override fun onLayoutCompleted(state: RecyclerView.State?) {
                    super.onLayoutCompleted(state)
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(
            HomeCardSwipeCallback(
                swipeDirs = ItemTouchHelper.LEFT,
                dragDirs = 0,
                interactor = interactor
            )
        )
        itemTouchHelper.attachToRecyclerView(view)

    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(state: AppState, announcements: List<RssItem>?) {
        /* TODO: add onboarding pages
        if (state.shouldShowHomeOnboardingDialog(view.context.settings())) {
            interactor.showOnboardingDialog()
        }
         */

        val messageCard = CenoMessageCard(
            text = ContextCompat.getString(view.context, R.string.enable_bridge_card_text) + " " +
                ContextCompat.getString(view.context, R.string.bridge_mode_ip_warning_text),
            title = ContextCompat.getString(view.context, R.string.enable_bridge_card_title)
        )
        sessionControlAdapter.submitList(
            state.toAdapterList(
                view.context.cenoPreferences(),
                messageCard,
                announcements,
                CenoSettings.isBridgeAnnouncementEnabled(view.context)
            )
        )
        sessionControlAdapter.notifyDataSetChanged()

    }
}