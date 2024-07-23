package ie.equalit.ceno.home.personal

import android.annotation.SuppressLint
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.URLSpan
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.databinding.PersonalModeDescriptionBinding
import ie.equalit.ceno.home.HomepageCardType
import ie.equalit.ceno.home.sessioncontrol.HomePageInteractor
import ie.equalit.ceno.utils.view.CenoViewHolder

class PersonalModeDescriptionViewHolder(
    view: View,
    interactor: HomePageInteractor
) : CenoViewHolder(view) {

    private val binding = PersonalModeDescriptionBinding.bind(view)

    init {
        binding.root.setBackgroundColor(
            ContextCompat.getColor(
                view.context,
                R.color.fx_mobile_private_layer_color_3
            )
        )
        setLinkTextView(
            binding.tvPersonalBrowsingLearnMore,
            view.context.getString(R.string.personal_home_learn_more)
        )
        binding.tvPersonalBrowsingLearnMore.setOnClickListener {
            interactor.onClicked(homepageCardType, BrowsingMode.Personal)

        }
    }

    fun bind() = Unit

    companion object {
        val homepageCardType = HomepageCardType.PERSONAL_MODE_CARD
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setLinkTextView(textView: TextView, text: String) {
        val notClickedString = SpannableString(text)
        notClickedString.setSpan(
            URLSpan(""),
            0,
            notClickedString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.setText(notClickedString, TextView.BufferType.SPANNABLE)
        val clickedString = SpannableString(notClickedString)
        clickedString.setSpan(
            BackgroundColorSpan(
                ContextCompat.getColor(
                    textView.context,
                    R.color.fx_mobile_text_color_secondary
                )
            ), 0, notClickedString.length,
            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
        )
        textView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> textView.text = clickedString
                MotionEvent.ACTION_UP -> {
                    textView.setText(notClickedString, TextView.BufferType.SPANNABLE)
                    v.performClick()
                }

                MotionEvent.ACTION_CANCEL -> textView.setText(
                    notClickedString,
                    TextView.BufferType.SPANNABLE
                )
            }
            true
        }
    }

}