package ie.equalit.ceno.settings

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.R.string.download_logs
import ie.equalit.ceno.R.string.ouinet_log_file_prompt_desc
import ie.equalit.ceno.R.string.preferences_ceno_download_log
import ie.equalit.ceno.R.string.view_logs
import ie.equalit.ceno.ext.getPreference
import ie.equalit.ceno.ext.getPreferenceKey
import ie.equalit.ceno.ext.getSwitchPreferenceCompat
import ie.equalit.ceno.ext.requireComponents
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.action.TabListAction
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.content.DownloadState
import mozilla.components.browser.state.state.createTab

class DeveloperToolsSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        requireComponents.metrics.autoTracker.measureVisit(listOf(TAG))
        setPreferencesFromResource(R.xml.developer_tools_preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        setupPreferences()
        getActionBar().apply{
            show()
            setTitle(R.string.developer_tools_category)
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.ceno_action_bar)))
        }
    }

    private fun setupPreferences() {
        getSwitchPreferenceCompat(R.string.pref_key_remote_debugging)?.
            onPreferenceChangeListener = getChangeListenerForRemoteDebugging()
        getPreference(R.string.pref_key_ceno_download_log)?.
            onPreferenceClickListener = getClickListenerForOuinetLogExport()
    }

    private fun getChangeListenerForRemoteDebugging(): OnPreferenceChangeListener {
        return OnPreferenceChangeListener { _, newValue ->
            requireComponents.core.engine.settings.remoteDebuggingEnabled = newValue as Boolean
            true
        }
    }

    private fun getClickListenerForOuinetLogExport(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            val store = requireComponents.core.store
            val logUrl = "${CenoSettings.SET_VALUE_ENDPOINT}/${CenoSettings.LOGFILE_TXT}"
            val download = DownloadState(logUrl)

            // prompt the user to view or download
            AlertDialog.Builder(requireContext()).apply {
                setTitle(context.getString(preferences_ceno_download_log))
                setMessage(context.getString(ouinet_log_file_prompt_desc))
                setNegativeButton(getString(download_logs)) { _, _ ->
                    createTab(logUrl).apply {
                        store.dispatch(TabListAction.AddTabAction(this, select = true))
                        store.dispatch(ContentAction.UpdateDownloadAction(this.id, download))
                    }
                    (activity as BrowserActivity).openToBrowser()
                }
                setPositiveButton(getString(view_logs)) { _, _ ->
                    createTab(logUrl).apply {
                        store.dispatch(TabListAction.AddTabAction(this, select = true))
                    }
                    (activity as BrowserActivity).openToBrowser()
                }
                create()
            }.show()
            true
        }
    }

    private fun getActionBar() = (activity as AppCompatActivity).supportActionBar!!

    companion object {
        private const val TAG = "DeveloperToolsSettingsFragment"
    }
}