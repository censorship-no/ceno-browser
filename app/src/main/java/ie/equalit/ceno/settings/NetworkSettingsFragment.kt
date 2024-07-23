/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ie.equalit.ceno.BuildConfig
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.getPreferenceKey
import mozilla.components.support.ktx.kotlin.ifNullOrEmpty
import java.util.Locale

class NetworkSettingsFragment : PreferenceFragmentCompat() {

    // This variable stores a map of all the sources from local.properties
    private val btSourcesMap = mutableMapOf<String, String>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.network_detail_preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()

        getActionBar().apply {
            show()
            setTitle(R.string.preferences_ceno_network_config)
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.ceno_action_bar
                    )
                )
            )
        }

        for (entry in BuildConfig.BT_BOOTSTRAP_EXTRAS) btSourcesMap[Locale(
            "",
            entry[0]
        ).displayCountry] = entry[1]
        setupPreferences()
    }

    private fun setupPreferences() {

        val preferenceAboutOuinetProtocol = getPreference(R.string.pref_key_about_ouinet_protocol)
        val preferenceReachabilityStatus =
            getPreference(R.string.pref_key_ouinet_reachability_status)
        val preferenceLocalUdpEndpoint = getPreference(R.string.pref_key_ouinet_local_udp_endpoints)
        val preferenceExternalUdpEndpoint =
            getPreference(R.string.pref_key_ouinet_external_udp_endpoints)
        val preferencePublicUdpEndpoint =
            getPreference(R.string.pref_key_ouinet_public_udp_endpoints)
        val preferenceUpnpStatus = getPreference(R.string.pref_key_ouinet_upnp_status)
        val extraBootstrapBittorrentKey =
            requireContext().getPreferenceKey(R.string.pref_key_ouinet_extra_bittorrent_bootstraps)

        val preferenceExtraBitTorrentBootstrap =
            findPreference<Preference>(extraBootstrapBittorrentKey)
        preferenceExtraBitTorrentBootstrap?.onPreferenceClickListener =
            getClickListenerForExtraBitTorrentBootstraps()

        preferenceAboutOuinetProtocol?.summary =
            "${CenoSettings.getOuinetProtocol(requireContext())}"
        preferenceReachabilityStatus?.summary = CenoSettings.getReachabilityStatus(requireContext())
        preferenceLocalUdpEndpoint?.summary = CenoSettings.getLocalUdpEndpoint(requireContext())
            .ifNullOrEmpty { getString(R.string.not_applicable) }
        preferenceExternalUdpEndpoint?.summary =
            CenoSettings.getExternalUdpEndpoint(requireContext())
                .ifNullOrEmpty { getString(R.string.not_applicable) }
        preferencePublicUdpEndpoint?.summary = CenoSettings.getPublicUdpEndpoint(requireContext())
            .ifNullOrEmpty { getString(R.string.not_applicable) }
        preferenceUpnpStatus?.summary = CenoSettings.getUpnpStatus(requireContext())
        preferenceExtraBitTorrentBootstrap?.summary = getBTPreferenceSummary()

    }

    private fun getClickListenerForExtraBitTorrentBootstraps(): Preference.OnPreferenceClickListener {
        return Preference.OnPreferenceClickListener {
            val extraBTBootstrapsDialog = ExtraBTBootstrapsDialog(requireContext(), btSourcesMap) {
                getPreference(R.string.pref_key_ouinet_extra_bittorrent_bootstraps)?.summary =
                    getBTPreferenceSummary()
            }
            extraBTBootstrapsDialog.getDialog().show()

            true
        }
    }

    private fun getPreference(key: Int): Preference? {
        val prefKey = requireContext().getPreferenceKey(key)
        return findPreference(prefKey)
    }

    private fun getBTPreferenceSummary(): String {
        var summary = ""

        CenoSettings.getLocalBTSources(requireContext())?.forEach {
            summary = if (btSourcesMap.values.contains(it)) {
                "$summary ${
                    btSourcesMap.entries.find { e -> e.value.trim() == it }?.key?.replace(
                        " ",
                        ""
                    )
                }"
            } else {
                "$summary $it"
            }
        }

        return when {
            summary.trim().isEmpty() -> getString(R.string.bt_sources_none)
            else -> summary.trim().replace(" ", ", ")
        }
    }

    private fun getActionBar() = (activity as AppCompatActivity).supportActionBar!!

}
