/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.home.topsites

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import ie.equalit.ceno.databinding.ComponentTopSitesPagerBinding
import ie.equalit.ceno.home.HomepageCardType
import ie.equalit.ceno.home.sessioncontrol.AdapterItem
import ie.equalit.ceno.home.sessioncontrol.TopSiteInteractor
import mozilla.components.feature.top.sites.TopSite

class TopSitePagerViewHolder(
    view: View,
    viewLifecycleOwner: LifecycleOwner,
    interactor: TopSiteInteractor
) : RecyclerView.ViewHolder(view) {

    private val binding = ComponentTopSitesPagerBinding.bind(view)
    private val topSitesPagerAdapter = TopSitesPagerAdapter(viewLifecycleOwner, interactor)
    private val pageIndicator = binding.pageIndicator
    private var currentPage = 0

    private val topSitesPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            pageIndicator.setSelection(position)
            currentPage = position
        }
    }

    init {
        binding.topSitesPager.apply {
            adapter = topSitesPagerAdapter
            registerOnPageChangeCallback(topSitesPageChangeCallback)
            // Retain one more TopSites pages to ensure a new layout request will measure the first page also.
            // Otherwise the second page with 3 TopSites will have the entire ViewPager only show
            // the first row of TopSites, hiding half of those shown on the first page.
            offscreenPageLimit = 1
        }
    }

    fun update(payload: AdapterItem.TopSitePagerPayload) {
        // Due to offscreenPageLimit = 1 we need to update both pages manually here
        topSitesPagerAdapter.notifyItemChanged(0, payload)
        topSitesPagerAdapter.notifyItemChanged(1, payload)
    }

    fun bind(topSites: List<TopSite>) {
        val chunkedTopSites = topSites.chunked(TOP_SITES_PER_PAGE)
        topSitesPagerAdapter.submitList(chunkedTopSites)

        // Don't show any page indicator if there is only 1 page.
        val numPages = if (topSites.size > TOP_SITES_PER_PAGE) {
            TOP_SITES_MAX_PAGE_SIZE
        } else {
            0
        }

        pageIndicator.isVisible = numPages > 1
        pageIndicator.setSize(numPages)
    }

    companion object {
        val homepageCardType = HomepageCardType.TOPSITES_CARD
        const val TOP_SITES_MAX_PAGE_SIZE = 2
        const val TOP_SITES_PER_PAGE = 8
    }
}
