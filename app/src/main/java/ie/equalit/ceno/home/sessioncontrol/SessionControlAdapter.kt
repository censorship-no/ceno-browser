package ie.equalit.ceno.home.sessioncontrol

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.R
import ie.equalit.ceno.home.CenoMessageViewHolder
import ie.equalit.ceno.home.CenoModeViewHolder
import ie.equalit.ceno.home.HomepageCardType
import ie.equalit.ceno.home.SectionHeaderViewHolder
import ie.equalit.ceno.home.TopPlaceholderViewHolder
import ie.equalit.ceno.home.announcements.RSSAnnouncementViewHolder
import ie.equalit.ceno.home.ouicrawl.OuicrawledSiteViewHolder
import ie.equalit.ceno.home.personal.PersonalModeDescriptionViewHolder
import ie.equalit.ceno.home.topsites.TopSitePagerViewHolder


class SessionControlAdapter internal constructor(
    private val interactor: SessionControlInteractor,
    private val viewLifecycleOwner: LifecycleOwner
) :
    ListAdapter<AdapterItem, RecyclerView.ViewHolder>(AdapterItemDiffCallback()) {
    // inflates the row layout from xml when needed
    // This method triggers the ComplexMethod lint error when in fact it's quite simple.
    @SuppressWarnings("ComplexMethod", "LongMethod", "ReturnCount")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val layout = when (viewType) {
            HomepageCardType.TOP_PLACE_CARD.value -> R.layout.top_placeholder_item
            HomepageCardType.MODE_MESSAGE_CARD.value -> R.layout.ceno_mode_item
            HomepageCardType.BASIC_MESSAGE_CARD.value -> R.layout.home_message_card_item
            HomepageCardType.TOPSITES_CARD.value -> R.layout.component_top_sites_pager
            HomepageCardType.PERSONAL_MODE_CARD.value -> R.layout.personal_mode_description
            HomepageCardType.ANNOUNCEMENTS_CARD.value -> R.layout.rss_announcement_item
            HomepageCardType.OUICRAWLED_SITE_CARD.value -> R.layout.home_ouicraw_site_item
            HomepageCardType.SECTION_HEADER.value -> R.layout.home_section_header_layout
            else -> throw IllegalArgumentException("Invalid view type")
        }

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return when (viewType) {
            TopPlaceholderViewHolder.homepageCardType.value -> TopPlaceholderViewHolder(view)
            CenoModeViewHolder.homepageCardType.value -> CenoModeViewHolder(view, interactor)
            CenoMessageViewHolder.homepageCardType.value -> CenoMessageViewHolder(view, interactor)
            TopSitePagerViewHolder.homepageCardType.value -> TopSitePagerViewHolder(
                view = view,
                viewLifecycleOwner = viewLifecycleOwner,
                interactor = interactor
            )

            RSSAnnouncementViewHolder.homepageCardType.value -> RSSAnnouncementViewHolder(
                view,
                interactor
            )

            PersonalModeDescriptionViewHolder.homepageCardType.value -> PersonalModeDescriptionViewHolder(
                view,
                interactor
            )
            SectionHeaderViewHolder.homepageCardType.value -> SectionHeaderViewHolder(
                view
            )
            OuicrawledSiteViewHolder.homepageCardType.value -> OuicrawledSiteViewHolder(view, interactor, viewLifecycleOwner)
            else -> throw IllegalStateException()
        }
    }

    override fun getItemViewType(position: Int) = getItem(position).type.value

    @SuppressWarnings("ComplexMethod")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is TopPlaceholderViewHolder -> {
                holder.bind()
            }

            is CenoModeViewHolder -> {
                holder.bind((item as AdapterItem.CenoModeItem).mode)
            }

            is TopSitePagerViewHolder -> {
                holder.bind((item as AdapterItem.TopSitePager).topSites)
            }

            is CenoMessageViewHolder -> {
                holder.bind((item as AdapterItem.CenoMessageItem).message)
            }

            is RSSAnnouncementViewHolder -> {
                (item as AdapterItem.CenoAnnouncementItem).apply {
                    holder.bind(this.response, this.mode)
                }
            }

            is OuicrawledSiteViewHolder -> {
                (item as AdapterItem.OuicrawledSiteItem).apply {
                    holder.bind(this.site)
                }
            }
        }
    }
}