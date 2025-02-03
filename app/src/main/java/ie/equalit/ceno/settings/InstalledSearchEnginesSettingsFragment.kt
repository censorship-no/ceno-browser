/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import mozilla.components.browser.state.state.SearchState
import mozilla.components.browser.state.state.availableSearchEngines
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.search.SearchUseCases
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.getPreferenceKey
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.search.RadioSearchEngineListPreference
import kotlin.collections.forEach as withEach

class InstalledSearchEnginesSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        requireComponents.metrics.autoTracker.measureVisit(listOf(TAG))
    }

    companion object {
        var languageChanged: Boolean = false
        private const val TAG = "InstalledSearchEnginesSettingsFragment"
    }

    override fun onResume() {
        super.onResume()
        setupPreferences()

        if (languageChanged) {
            restoreSearchEngines()
        } else {
            refetchSearchEngines()
        }
    }

    private fun setupPreferences() {
        val searchEngineListKey = requireContext().getPreferenceKey(R.string.pref_key_radio_search_engine_list)
        val preferenceSearchEngineList = findPreference<Preference>(searchEngineListKey)
        preferenceSearchEngineList?.onPreferenceClickListener = getClickListenerForSearchEngineList()
    }

    private fun getClickListenerForSearchEngineList(): Preference.OnPreferenceClickListener {
        return Preference.OnPreferenceClickListener {
            refetchSearchEngines()
            true
        }
    }

    /*
    // TODO: allow removing search engines via menu bar?
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.menu_search_engines, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        menu.findItem(R.id.menu_restore_default_engines)?.let {
            it.isEnabled = !requireComponents.store.state.search.hasDefaultSearchEnginesOnly()
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        val currentEnginesCount = requireComponents.store.state.search.searchEngines.size

        return when (menuItem.itemId) {
            R.id.menu_remove_search_engines -> {
                requireComponents.appStore.dispatch(
                    AppAction.OpenSettings(Screen.Settings.Page.SearchRemove),
                )
                true
            }
            R.id.menu_restore_default_engines -> {
                restoreSearchEngines()
                true
            }
            else -> false
        }
    }
     */

    private fun restoreSearchEngines() {

        restoreSearchDefaults(requireComponents.core.store, requireComponents.useCases.searchUseCases)
        refetchSearchEngines()
        languageChanged = false
    }

    /*
    // TODO: allow adding custom search engines
    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return when (preference.key) {
            resources.getString(R.string.pref_key_radio_search_engine_list) -> {
                requireComponents.appStore.dispatch(
                    AppAction.OpenSettings(page = Screen.Settings.Page.SearchAdd),
                )
                return true
            }
            else -> {
                super.onPreferenceTreeClick(preference)
            }
        }
    }
    */

    /**
     * Refresh search engines list.
     */
    private fun refetchSearchEngines() {
        // Refresh this preference screen to display changes.
        preferenceScreen?.removeAll()
        addPreferencesFromResource(R.xml.search_engine_settings)

        val pref: RadioSearchEngineListPreference? = preferenceScreen.findPreference(
            resources.getString(R.string.pref_key_radio_search_engine_list),
        )
        pref?.refetchSearchEngines()
    }
}

private fun SearchState.hasDefaultSearchEnginesOnly(): Boolean {
    return availableSearchEngines.isEmpty() && additionalSearchEngines.isEmpty() && customSearchEngines.isEmpty()
}

private fun restoreSearchDefaults(store: BrowserStore, useCases: SearchUseCases) {
    store.state.search.customSearchEngines.withEach { searchEngine ->
        useCases.removeSearchEngine(
            searchEngine,
        )
    }
    store.state.search.hiddenSearchEngines.withEach { searchEngine ->
        useCases.addSearchEngine(
            searchEngine,
        )
    }
}
