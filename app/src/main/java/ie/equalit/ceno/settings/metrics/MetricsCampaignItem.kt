/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings.metrics

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.MetricsCampaignItemBinding

class MetricsCampaignItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private companion object {
        private const val ENABLED_ALPHA = 1f
        private const val DISABLED_ALPHA = 0.6f
    }

    private var binding: MetricsCampaignItemBinding

    val titleView: TextView
        get() = binding.title

    val subtitleView: TextView
        get() = binding.subtitle

    var isChecked: Boolean
        get() = binding.checkbox.isChecked
        set(value) {
            binding.checkbox.isChecked = value
        }

    var onCheckListener: ((Boolean) -> Unit)? = null

    init {
        val view =
            LayoutInflater.from(context).inflate(R.layout.metrics_campaign_item, this, true)

        binding = MetricsCampaignItemBinding.bind(view)

        setOnClickListener {
            binding.checkbox.isChecked = !binding.checkbox.isChecked
        }

        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            onCheckListener?.invoke(isChecked)
        }

        context.withStyledAttributes(attrs, R.styleable.MetricsCampaignItem, defStyleAttr, 0) {
            val titleId = getResourceId(
                R.styleable.MetricsCampaignItem_metricsCampaignItemTitle,
                R.string.browser_menu_library,
            )
            val subtitleId = getResourceId(
                R.styleable.MetricsCampaignItem_metricsCampaignItemSubtitle,
                R.string.empty_string,
            )

            val moreInfoId = getResourceId(
                R.styleable.MetricsCampaignItem_metricsCampaignItemMoreInfo,
                R.string.metrics_campaign_more_info,
            )

            binding.title.text = resources.getString(titleId)
            val subtitleText = try {
                resources.getString(subtitleId)
            } catch ( _ : Exception) {
                /* Subtitle might be a plural instead of string
                 * catch this exception and set subtitle empty
                 */
                resources.getString(R.string.empty_string)
            }
            binding.subtitle.text = subtitleText
            if (subtitleText.isBlank()) binding.subtitle.visibility = View.GONE
            binding.moreInfo.text = resources.getString(moreInfoId)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        alpha = if (enabled) ENABLED_ALPHA else DISABLED_ALPHA
    }
}
