/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings.metrics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentMetricsCampaignBinding
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.settings.dialogs.WebViewPopupPanel

class MetricsCampaignFragment : Fragment(R.layout.fragment_metrics_campaign) {

    private lateinit var controller: MetricsCampaignController
    private var scope: CoroutineScope? = null

    private var _binding: FragmentMetricsCampaignBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMetricsCampaignBinding.bind(view)
        controller = DefaultMetricsCampaignController(requireContext(), requireComponents)

        binding.campaignCrashReporting.onCheckListener = { newValue ->
            controller.crashReporting(newValue)
        }
        binding.campaignOne.onCheckListener = { newValue ->
            controller.campaignOne(newValue) { granted ->
                binding.campaignOne.isChecked = granted
            }
        }

        val privacyPolicyUrl = requireContext().getString(R.string.privacy_policy_url)
        binding.deleteData.setOnClickListener {
            val dialog = WebViewPopupPanel(requireContext(), context as LifecycleOwner, privacyPolicyUrl)
            dialog.show()
        }
    }

    override fun onResume() {
        super.onResume()

        getCheckboxes().iterator().forEach {
            it.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        binding.progressBar.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        scope?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCheckboxes(): List<MetricsCampaignItem> {
        return listOf(
            binding.campaignCrashReporting,
            binding.campaignAutoTracker,
            binding.campaignOne,
        )
    }

    companion object {
        private const val ENABLED_ALPHA = 1f
        private const val DISABLED_ALPHA = 0.6f
    }
}
