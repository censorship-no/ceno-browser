package ie.equalit.ceno.home.ouicrawl

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import ie.equalit.ceno.databinding.HomeOuicrawSiteItemBinding
import ie.equalit.ceno.ext.ceno.bitmapForUrl
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.home.BaseHomeCardViewHolder
import ie.equalit.ceno.home.HomepageCardType
import ie.equalit.ceno.home.sessioncontrol.HomePageInteractor
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OuicrawledSiteViewHolder(
    itemView: View,
    interactor: HomePageInteractor,
    private val viewLifecycleOwner: LifecycleOwner,
):BaseHomeCardViewHolder(itemView, interactor) {
    private val binding = HomeOuicrawSiteItemBinding.bind(itemView)

    init {
        cardType = HomepageCardType.OUICRAWLED_SITE_CARD
    }

    companion object {
        val homepageCardType = HomepageCardType.OUICRAWLED_SITE_CARD
    }

    fun bind(site: OuicrawledSite) {
        binding.tvWebsiteName.text = site.SiteName
        binding.tvWebsiteUrl.text = site.GatewayURL
        binding.tvLastUpdatedStatus.text = "last mirror - ${site.LastCrawlUpdated}"
        viewLifecycleOwner.lifecycleScope.launch(IO) {
            itemView.context.components.core.client.bitmapForUrl(site.FaviconURL)?.let { bitmap ->
                withContext(Main) {
                    binding.ivFavicon.setImageBitmap(bitmap)
                }
            }
        }
    }
}