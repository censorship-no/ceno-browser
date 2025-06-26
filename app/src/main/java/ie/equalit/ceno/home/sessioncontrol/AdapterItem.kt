package ie.equalit.ceno.home.sessioncontrol

import androidx.recyclerview.widget.DiffUtil
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.home.CenoMessageCard
import ie.equalit.ceno.home.CenoMessageViewHolder
import ie.equalit.ceno.home.CenoModeViewHolder
import ie.equalit.ceno.home.HomepageCardType
import ie.equalit.ceno.home.RssItem
import ie.equalit.ceno.home.SectionHeaderViewHolder
import ie.equalit.ceno.home.TopPlaceholderViewHolder
import ie.equalit.ceno.home.announcements.RSSAnnouncementViewHolder
import ie.equalit.ceno.home.ouicrawl.OuicrawlSite
import ie.equalit.ceno.home.ouicrawl.OuicrawledSiteViewHolder
import ie.equalit.ceno.home.personal.PersonalModeDescriptionViewHolder
import ie.equalit.ceno.home.topsites.TopSitePagerViewHolder
import mozilla.components.feature.top.sites.TopSite

sealed class AdapterItem(val type: HomepageCardType) {

    object TopPlaceholderItem : AdapterItem(TopPlaceholderViewHolder.homepageCardType)

    data class CenoModeItem(val mode: BrowsingMode) :
        AdapterItem(CenoModeViewHolder.homepageCardType) {
        override fun contentsSameAs(other: AdapterItem): Boolean {
            val newCenoMode = (other as? CenoModeItem) ?: return false
            if (newCenoMode.mode != this.mode) return false
            return super.contentsSameAs(other)
        }
    }

    data class CenoMessageItem(val message: CenoMessageCard) :
        AdapterItem(CenoMessageViewHolder.homepageCardType)

    data class CenoAnnouncementItem(val response: RssItem, var mode: BrowsingMode) :
        AdapterItem(RSSAnnouncementViewHolder.homepageCardType) {
        override fun contentsSameAs(other: AdapterItem): Boolean {
            val newItem = (other as? CenoAnnouncementItem) ?: return false
            if (newItem.mode != this.mode) return false
            if (newItem.response.title != this.response.title) return false
            return super.contentsSameAs(other)
        }
    }

    data class OuicrawledSiteItem(val site:OuicrawlSite) :
            AdapterItem(OuicrawledSiteViewHolder.homepageCardType)

    object SectionHeaderItem:
        AdapterItem(SectionHeaderViewHolder.homepageCardType)

    object PersonalModeDescriptionItem :
        AdapterItem(PersonalModeDescriptionViewHolder.homepageCardType)

    /**
     * Contains a set of [Pair]s where [Pair.first] is the index of the changed [TopSite] and
     * [Pair.second] is the new [TopSite].
     */
    data class TopSitePagerPayload(
        val changed: Set<Pair<Int, TopSite>>
    )

    data class TopSitePager(val topSites: List<TopSite>) :
        AdapterItem(TopSitePagerViewHolder.homepageCardType) {
        override fun sameAs(other: AdapterItem): Boolean {
            return other is TopSitePager
        }

        override fun contentsSameAs(other: AdapterItem): Boolean {
            val newTopSites = (other as? TopSitePager) ?: return false
            if (newTopSites.topSites.size != this.topSites.size) return false
            val newSitesSequence = newTopSites.topSites.asSequence()
            val oldTopSites = this.topSites.asSequence()
            return newSitesSequence.zip(oldTopSites).all { (new, old) -> new == old }
        }

        /**
         * Returns a payload if there's been a change, or null if not, but adds a "dummy" item for
         * each deleted [TopSite]. This is done in order to more easily identify the actual views
         * that need to be removed in [TopSitesPagerAdapter.update].
         *
         * See https://github.com/mozilla-mobile/fenix/pull/20189#issuecomment-877124730
         */
        @Suppress("ComplexCondition")
        override fun getChangePayload(newItem: AdapterItem): Any? {
            val newTopSites = (newItem as? TopSitePager)
            val oldTopSites = (this as? TopSitePager)

            if (newTopSites == null || oldTopSites == null ||
                newTopSites.topSites.size > oldTopSites.topSites.size ||
                (newTopSites.topSites.size > TopSitePagerViewHolder.TOP_SITES_PER_PAGE)
                != (oldTopSites.topSites.size > TopSitePagerViewHolder.TOP_SITES_PER_PAGE)
            ) {
                return null
            }

            val changed = mutableSetOf<Pair<Int, TopSite>>()

            for ((index, item) in oldTopSites.topSites.withIndex()) {
                val changedItem =
                    newTopSites.topSites.getOrNull(index) ?: TopSite.Frecent(-1, "REMOVED", "", 0)
                if (changedItem != item) {
                    changed.add((Pair(index, changedItem)))
                }
            }
            return if (changed.isNotEmpty()) TopSitePagerPayload(changed) else null
        }
    }

    /**
     * True if this item represents the same value as other. Used by [AdapterItemDiffCallback].
     */
    open fun sameAs(other: AdapterItem) = this::class == other::class

    /**
     * Returns a payload if there's been a change, or null if not
     */
    open fun getChangePayload(newItem: AdapterItem): Any? = null

    open fun contentsSameAs(other: AdapterItem) = this::class == other::class
}

class AdapterItemDiffCallback : DiffUtil.ItemCallback<AdapterItem>() {
    override fun areItemsTheSame(oldItem: AdapterItem, newItem: AdapterItem) =
        oldItem.sameAs(newItem)

    @Suppress("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: AdapterItem, newItem: AdapterItem) =
        oldItem.contentsSameAs(newItem)

    override fun getChangePayload(oldItem: AdapterItem, newItem: AdapterItem): Any? {
        return oldItem.getChangePayload(newItem) ?: return super.getChangePayload(oldItem, newItem)
    }
}
