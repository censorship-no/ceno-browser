package ie.equalit.ceno.home.ouicrawl

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.HomeOuicrawSiteItemBinding
import ie.equalit.ceno.ext.ceno.bitmapForUrl
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.home.BaseHomeCardViewHolder
import ie.equalit.ceno.home.HomepageCardType
import ie.equalit.ceno.home.sessioncontrol.HomePageInteractor
import ie.equalit.ceno.home.sessioncontrol.OuicrawlSiteInteractor
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.feature.top.sites.TopSite

class OuicrawledSiteViewHolder(
    itemView: View,
    val interactor: OuicrawlSiteInteractor,
    private val viewLifecycleOwner: LifecycleOwner,
): RecyclerView.ViewHolder(itemView) {
    private val binding = HomeOuicrawSiteItemBinding.bind(itemView)
    private lateinit var ouicrawlSite: OuicrawlSite

    var cardType: HomepageCardType = HomepageCardType.OUICRAWLED_SITE_CARD

    init {
        binding.ouicrawledSite.setOnLongClickListener {
            val contextMenu = OuicrawledSiteMenu (
                context = itemView.context,
                ouicrawlSite
            ) { item ->
                when (item) {
                    is OuicrawledSiteMenu.Item.OpenInPersonalTab -> {
                        interactor.onOpenInPersonalTabClicked(ouicrawlSite)
                    }
                    is OuicrawledSiteMenu.Item.AddToShortcut -> {
                        interactor.onShortcuts(ouicrawlSite,
                            itemView.context.components.appStore.state.topSites.any { topSite: TopSite ->
                                topSite.url == ("https://${ouicrawlSite.SiteURL}/")
                            }
                        )
                    }
                }
            }
            contextMenu.menuBuilder.build(itemView.context).show(it)
            true
        }
    }

    companion object {
        val homepageCardType = HomepageCardType.OUICRAWLED_SITE_CARD
    }

    fun bind(ouicrawlSite: OuicrawlSite) {
        binding.tvWebsiteName.text = ouicrawlSite.SiteName
        binding.tvWebsiteUrl.text = ouicrawlSite.GatewayURL
        binding.tvLastUpdatedStatus.text = "last mirror - " + ouicrawlSite.LastCrawlUpdated
        viewLifecycleOwner.lifecycleScope.launch(IO) {
            itemView.context.components.core.client.bitmapForUrl(ouicrawlSite.FaviconURL)?.let { bitmap ->
                withContext(Main) {
                    binding.ivFavicon.setImageBitmap(bitmap)
                }
            }
        }
        binding.ouicrawledSite.setOnClickListener {
            interactor.onOuicrawlSiteClicked(ouicrawlSite.SiteURL)
        }

        this.ouicrawlSite = ouicrawlSite
    }
}