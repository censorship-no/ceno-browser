/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.getPreferenceKey
import ie.equalit.ceno.ext.requireComponents
import mozilla.components.browser.state.selector.selectedTab

class CustomizationSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.customization_preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        setupPreferences()
        getActionBar().apply {
            show()
            setTitle(R.string.customization_settings)
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
    }

    private fun setupPreferences() {
        val changeAppIconKey = requireContext().getPreferenceKey(R.string.pref_key_change_app_icon)
        val themeKey = requireContext().getPreferenceKey(R.string.pref_key_theme)

        val preferenceChangeAppIcon = findPreference<Preference>(changeAppIconKey)
        val preferenceTheme = findPreference<Preference>(themeKey)

        preferenceChangeAppIcon?.onPreferenceClickListener = getClickListenerForChangeAppIcon()
        preferenceTheme?.onPreferenceChangeListener = getChangeListenerForTheme()
    }

    private fun getClickListenerForChangeAppIcon(): Preference.OnPreferenceClickListener {
        return Preference.OnPreferenceClickListener {
            findNavController().navigate(
                R.id.action_customizationSettingsFragment_to_changeIconFragment
            )
            getActionBar().setTitle(R.string.preferences_change_app_icon)
            true
        }
    }

    private fun getChangeListenerForTheme(): Preference.OnPreferenceChangeListener {
        return Preference.OnPreferenceChangeListener { _, newValue ->
            val modeString = newValue as String
            val mode = modeString.toInt()
            if (AppCompatDelegate.getDefaultNightMode() != mode) {
                AppCompatDelegate.setDefaultNightMode(mode)
                activity?.recreate()
                requireComponents.core.store.state.selectedTab?.let {
                    requireComponents.useCases.tabsUseCases.selectTab(requireComponents.core.store.state.selectedTab!!.id)
                    requireComponents.useCases.sessionUseCases.reload.invoke()
                }
                /* TODO: send colorScheme to gecko engine
                with(requireComponents.core) {
                    engine.settings.preferredColorScheme = getPreferredColorScheme()
                }
                 */
            }
            true
        }
    }

    private fun getActionBar() = (activity as AppCompatActivity).supportActionBar!!
}
