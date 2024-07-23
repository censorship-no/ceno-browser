/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.home

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.databinding.CenoModeItemBinding
import ie.equalit.ceno.ext.cenoPreferences
import ie.equalit.ceno.home.sessioncontrol.HomePageInteractor


/**
 * View holder for the CENO normal/private mode description.
 */
class CenoModeViewHolder(
    view: View,
    interactor: HomePageInteractor
) : BaseHomeCardViewHolder(view, interactor) {

    private val binding = CenoModeItemBinding.bind(view)
    private var mode: BrowsingMode = BrowsingMode.Normal
        get() {
            return field
        }
        set(value) {
            field = value
            updateUI()
        }

    private fun updateUI() {
        when (mode) {
            BrowsingMode.Normal -> {
                binding.personalModeCard.setOnClickListener {
                    interactor.onClicked(homepageCardType, mode)
                }
                binding.publicModeCard.setOnClickListener(null)
                binding.cenoModeTitle.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_home_card_text_color
                    )
                )
                binding.cenoModeItem.setCardBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_home_card_background_tint
                    )
                )

                binding.publicModeCardCheckmark.visibility = View.VISIBLE
                binding.publicModeCard.background.setTint(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_mode_selected_green
                    )
                )
                binding.tvHomeCardPublicText.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_home_card_text_color
                    )
                )
                binding.tvHomeCardPublicTitle.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_home_card_public_text
                    )
                )
                binding.ivPublicModeCardIcon.drawable.setTint(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_home_card_public_text
                    )
                )

                binding.personalModeCardCheckmark.visibility = View.INVISIBLE
                binding.personalModeCard.background.setTint(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_browsing_mode_card_border
                    )
                )
                binding.tvHomeCardPersonalText.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_home_card_text_color
                    )
                )
                binding.tvHomeCardPersonalTitle.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_home_card_personal_text
                    )
                )
                binding.ivPersonalModeCardIcon.drawable.setTint(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_home_card_personal_text
                    )
                )

            }

            BrowsingMode.Personal -> {
                binding.publicModeCard.setOnClickListener {
                    interactor.onClicked(homepageCardType, mode)
                }
                binding.personalModeCard.setOnClickListener(null)
                binding.cenoModeTitle.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_grey_300
                    )
                )
                binding.cenoModeItem.setCardBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_purple_800
                    )
                )

                binding.publicModeCardCheckmark.visibility = View.INVISIBLE
                binding.publicModeCard.background.setTint(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_grey_500
                    )
                )
                binding.tvHomeCardPublicText.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_grey_300
                    )
                )
                binding.tvHomeCardPublicTitle.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_blue_300
                    )
                )
                binding.ivPublicModeCardIcon.drawable.setTint(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_blue_300
                    )
                )

                binding.personalModeCardCheckmark.visibility = View.VISIBLE
                binding.personalModeCard.background.setTint(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_mode_selected_green
                    )
                )
                binding.tvHomeCardPersonalText.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_grey_300
                    )
                )
                binding.tvHomeCardPersonalTitle.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_purple_300
                    )
                )
                binding.ivPersonalModeCardIcon.drawable.setTint(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.ceno_purple_300
                    )
                )
            }
        }
    }

    init {
        cardType = homepageCardType
        binding.cenoModeTitle.setOnClickListener {
            //collapse
            if (binding.tvHomeCardPublicText.isVisible) {
                binding.tvHomeCardPublicText.visibility = View.GONE
                binding.tvHomeCardPersonalText.visibility = View.GONE
                binding.cenoModeTitle.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_collapsed),
                    null
                )
                itemView.context.cenoPreferences().isModeCardExpanded = false
            } else {
                //expand
                binding.tvHomeCardPublicText.visibility = View.VISIBLE
                binding.tvHomeCardPersonalText.visibility = View.VISIBLE
                binding.cenoModeTitle.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_expanded),
                    null
                )
                itemView.context.cenoPreferences().isModeCardExpanded = true
            }
        }

        if (itemView.context.cenoPreferences().isModeCardExpanded) {
            binding.tvHomeCardPublicText.visibility = View.VISIBLE
            binding.tvHomeCardPersonalText.visibility = View.VISIBLE
            binding.cenoModeTitle.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_expanded),
                null
            )
        } else {
            binding.tvHomeCardPublicText.visibility = View.GONE
            binding.tvHomeCardPersonalText.visibility = View.GONE
            binding.cenoModeTitle.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_collapsed),
                null
            )
        }
    }

    fun bind(mode: BrowsingMode) {
        this@CenoModeViewHolder.mode = mode
        //modify based on mode
    }

    companion object {
        val homepageCardType = HomepageCardType.MODE_MESSAGE_CARD
    }

    private fun Drawable.colorTint(@ColorRes color: Int) = apply {
        mutate()
        @Suppress("DEPRECATION") // Deprecated warning appeared when switching to Java 11.
        setColorFilter(ContextCompat.getColor(itemView.context, color), PorterDuff.Mode.SRC_IN)
    }
}
