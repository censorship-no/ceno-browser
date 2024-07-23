package ie.equalit.ceno.home.announcements

import android.text.method.LinkMovementMethod
import android.view.ContextThemeWrapper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.buildSpannedString
import androidx.core.view.isGone
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.databinding.RssAnnouncementItemBinding
import ie.equalit.ceno.ext.click
import ie.equalit.ceno.ext.extractATags
import ie.equalit.ceno.ext.getContentFromATag
import ie.equalit.ceno.home.BaseHomeCardViewHolder
import ie.equalit.ceno.home.HomepageCardType
import ie.equalit.ceno.home.RssItem
import ie.equalit.ceno.home.sessioncontrol.HomePageInteractor
import ie.equalit.ceno.utils.XMLParser

class RSSAnnouncementViewHolder(
    itemView: View,
    interactor: HomePageInteractor
) : BaseHomeCardViewHolder(itemView, interactor) {

    private val binding = RssAnnouncementItemBinding.bind(itemView)
    private var mode: BrowsingMode = BrowsingMode.Normal
        get() {
            return field
        }
        set(value) {
            field = value
            update()
        }

    fun update() {
        val listIsHidden = binding.rssAnnouncementsContent.visibility == View.GONE
        val personalContext = ContextThemeWrapper(itemView.context, R.style.PersonalTheme)
        if (mode.isPersonal) {
            binding.rssTitle.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.ceno_orange_200
                )
            )
            DrawableCompat.setTint(
                binding.rssTitle.background,
                ContextCompat.getColor(itemView.context, R.color.ceno_orange_800)
            )
            binding.rssAnnouncementsCard.background.setTint(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.ceno_orange_900
                )
            )
            binding.rssTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                if (listIsHidden) ContextCompat.getDrawable(
                    personalContext,
                    R.drawable.ic_announcement_collapsed
                ) else ContextCompat.getDrawable(
                    personalContext,
                    R.drawable.ic_announcement_expanded
                ),
                null,
                if (listIsHidden) ContextCompat.getDrawable(
                    personalContext,
                    R.drawable.ic_arrow_collapsed
                ) else ContextCompat.getDrawable(personalContext, R.drawable.ic_arrow_expanded),
                null
            )

        } else {
            binding.rssTitle.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.ceno_home_card_announcement_title_color
                )
            )
            DrawableCompat.setTint(
                binding.rssTitle.background,
                ContextCompat.getColor(
                    itemView.context,
                    R.color.ceno_home_card_announcement_title_background
                )
            )
            binding.rssAnnouncementsCard.background.setTint(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.home_card_announcements_background
                )
            )
            binding.rssTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                if (listIsHidden) R.drawable.ic_announcement_collapsed else R.drawable.ic_announcement_expanded,
                0,
                if (listIsHidden) R.drawable.ic_arrow_collapsed else R.drawable.ic_arrow_expanded,
                0
            )
        }
    }

    fun bind(response: RssItem, mode: BrowsingMode) {
        this@RSSAnnouncementViewHolder.mode = mode

        binding.rssTitle.text = response.title

        val personalContext = ContextThemeWrapper(itemView.context, R.style.PersonalTheme)

        binding.rssTitle.setOnClickListener {

            val listIsHidden = binding.rssAnnouncementsContent.visibility == View.GONE

            binding.rssAnnouncementsContent.isGone = !listIsHidden
            if (mode.isPersonal) {
                binding.rssTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    if (listIsHidden) ContextCompat.getDrawable(
                        personalContext,
                        R.drawable.ic_announcement_expanded
                    ) else ContextCompat.getDrawable(
                        personalContext,
                        R.drawable.ic_announcement_collapsed
                    ),
                    null,
                    if (listIsHidden) ContextCompat.getDrawable(
                        personalContext,
                        R.drawable.ic_arrow_expanded
                    ) else ContextCompat.getDrawable(
                        personalContext,
                        R.drawable.ic_arrow_collapsed
                    ),
                    null
                )
                binding.itemDate.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_orange_300
                    )
                )
                binding.tvMessage.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_orange_200
                    )
                )
            } else {
                binding.rssTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    if (listIsHidden) R.drawable.ic_announcement_expanded else R.drawable.ic_announcement_collapsed,
                    0,
                    if (listIsHidden) R.drawable.ic_arrow_expanded else R.drawable.ic_arrow_collapsed,
                    0
                )
                binding.itemDate.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_home_card_announcement_timestamp_color
                    )
                )
                binding.tvMessage.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_home_card_announcement_message
                    )
                )
            }
        }

        binding.itemDate.text = response.pubDate

        var descriptionText = response.description
        // Replace all a-tags in the description string with a placeholder string
        val allATags = descriptionText.extractATags()
        allATags.forEach {
            descriptionText = descriptionText.replaceFirst(it, XMLParser.CENO_CUSTOM_PLACEHOLDER)
        }

        // split the new description string by the placeholder string to generate an array
        val descriptionSubStringArray = descriptionText.split(XMLParser.CENO_CUSTOM_PLACEHOLDER)

        // iteration variable for the list of a-tags
        var index = 0

        // Construct new HTML string to be displayed
        val spannedString = buildSpannedString {
            descriptionSubStringArray.forEach {
                append(it)
                if (index < allATags.size) {
                    val pair = allATags[index].getContentFromATag()
                    click(true, onClick = {
                        interactor.onUrlClicked(homepageCardType, pair.first.toString())
                    }) {
                        append(pair.second)
                    }
                    index++
                }
            }
        }

        binding.tvMessage.movementMethod = LinkMovementMethod.getInstance()
        binding.tvMessage.text = spannedString

    }

    interface RssAnnouncementSwipeListener {
        fun onSwipeCard(index: Int)
    }

    companion object {
        val homepageCardType = HomepageCardType.ANNOUNCEMENTS_CARD
    }
}