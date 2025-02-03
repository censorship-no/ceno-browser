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
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.getPreference
import ie.equalit.ceno.ext.requireComponents

class WebsiteSourceSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        requireComponents.metrics.autoTracker.measureVisit(listOf(TAG))
        setPreferencesFromResource(R.xml.sources_preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()

        getActionBar().apply {
            show()
            setTitle(R.string.preferences_ceno_website_sources)
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.ceno_action_bar)))
        }

        setupSettings()
    }

    private fun setupSettings() {

        if (CenoSettings.isStatusUpdateRequired(requireContext())) {
            /* Ouinet status not yet updated */
            /* Grey out all Ceno related options */
            setPreference(getPreference(R.string.pref_key_ceno_sources_origin), false)
            setPreference(getPreference(R.string.pref_key_ceno_sources_private), false)
            setPreference(getPreference(R.string.pref_key_ceno_sources_public), false)
            setPreference(getPreference(R.string.pref_key_ceno_sources_shared), false)
            /* Fetch ouinet status */
            CenoSettings.ouinetClientRequest(requireContext(), OuinetKey.API_STATUS)
        } else {
            /* Enable Ceno related options */
            setPreference(
                getPreference(R.string.pref_key_ceno_sources_origin),
                true,
                changeListener = getChangeListenerForCenoSetting(OuinetKey.ORIGIN_ACCESS)
            )
            setPreference(
                getPreference(R.string.pref_key_ceno_sources_private),
                true,
                changeListener = getChangeListenerForCenoSetting(OuinetKey.PROXY_ACCESS)
            )
            setPreference(
                getPreference(R.string.pref_key_ceno_sources_public),
                true,
                changeListener = getChangeListenerForCenoSetting(OuinetKey.INJECTOR_ACCESS)
            )
            setPreference(
                getPreference(R.string.pref_key_ceno_sources_shared),
                true,
                changeListener = getChangeListenerForCenoSetting(OuinetKey.DISTRIBUTED_CACHE)
            )
        }
    }

    private fun getChangeListenerForCenoSetting(key: OuinetKey): Preference.OnPreferenceChangeListener {
        return Preference.OnPreferenceChangeListener { _, newValue ->
            val value = if (newValue == true) {
                OuinetValue.ENABLED
            } else {
                OuinetValue.DISABLED
            }
            CenoSettings.ouinetClientRequest(requireContext(), key, value)
            true
        }
    }

    private fun setPreference(
        pref: Preference?,
        enabled: Boolean,
        changeListener: Preference.OnPreferenceChangeListener? = null,
        clickListener: Preference.OnPreferenceClickListener? = null
    ) {
        pref?.let {
            it.isEnabled = enabled
            it.shouldDisableView = !enabled
            it.onPreferenceChangeListener = changeListener
            it.onPreferenceClickListener = clickListener
        }
    }

    private fun getActionBar() = (activity as AppCompatActivity).supportActionBar!!

    companion object {
        private const val TAG = "PrivacySettingsFragment"
    }
}