package ie.equalit.ceno.home

import android.view.View
import ie.equalit.ceno.databinding.HomeSectionHeaderLayoutBinding
import ie.equalit.ceno.home.sessioncontrol.HomePageInteractor
import ie.equalit.ceno.utils.view.CenoViewHolder

class SectionHeaderViewHolder(
    view: View
): CenoViewHolder(view) {
    private val binding = HomeSectionHeaderLayoutBinding.bind(itemView)

    companion object {
        val homepageCardType = HomepageCardType.SECTION_HEADER
    }
}