/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import ie.equalit.ceno.AppPermissionCodes
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.R.string.bridge_mode_ip_warning_text
import ie.equalit.ceno.R.string.ceno_clear_dialog_cancel
import ie.equalit.ceno.R.string.confirm_clear_cached_content
import ie.equalit.ceno.R.string.confirm_clear_cached_content_desc
import ie.equalit.ceno.R.string.customize_addon_collection_cancel
import ie.equalit.ceno.R.string.customize_addon_collection_ok
import ie.equalit.ceno.R.string.dialog_btn_positive_ok
import ie.equalit.ceno.R.string.download_logs
import ie.equalit.ceno.R.string.no_content_shared
import ie.equalit.ceno.R.string.onboarding_battery_button
import ie.equalit.ceno.R.string.ouinet_client_fetch_fail
import ie.equalit.ceno.R.string.ouinet_log_file_prompt_desc
import ie.equalit.ceno.R.string.pref_key_about_ceno
import ie.equalit.ceno.R.string.pref_key_about_geckoview
import ie.equalit.ceno.R.string.pref_key_about_ouinet
import ie.equalit.ceno.R.string.pref_key_about_page
import ie.equalit.ceno.R.string.pref_key_add_ons
import ie.equalit.ceno.R.string.pref_key_allow_crash_reporting
import ie.equalit.ceno.R.string.pref_key_allow_notifications
import ie.equalit.ceno.R.string.pref_key_bridge_announcement
import ie.equalit.ceno.R.string.pref_key_ceno_cache_size
import ie.equalit.ceno.R.string.pref_key_ceno_download_android_log
import ie.equalit.ceno.R.string.pref_key_ceno_download_log
import ie.equalit.ceno.R.string.pref_key_ceno_enable_log
import ie.equalit.ceno.R.string.pref_key_ceno_groups_count
import ie.equalit.ceno.R.string.pref_key_ceno_network_config
import ie.equalit.ceno.R.string.pref_key_ceno_website_sources
import ie.equalit.ceno.R.string.pref_key_change_language
import ie.equalit.ceno.R.string.pref_key_clear_ceno_cache
import ie.equalit.ceno.R.string.pref_key_customization
import ie.equalit.ceno.R.string.pref_key_delete_browsing_data
import ie.equalit.ceno.R.string.pref_key_disable_battery_opt
import ie.equalit.ceno.R.string.pref_key_make_default_browser
import ie.equalit.ceno.R.string.pref_key_ouinet_state
import ie.equalit.ceno.R.string.pref_key_override_amo_collection
import ie.equalit.ceno.R.string.pref_key_privacy
import ie.equalit.ceno.R.string.pref_key_remote_debugging
import ie.equalit.ceno.R.string.pref_key_search_engine
import ie.equalit.ceno.R.string.pref_key_shared_prefs_reload
import ie.equalit.ceno.R.string.pref_key_shared_prefs_update
import ie.equalit.ceno.R.string.preference_choose_search_engine
import ie.equalit.ceno.R.string.preferences_about_page
import ie.equalit.ceno.R.string.preferences_ceno_download_log
import ie.equalit.ceno.R.string.preferences_customize_amo_collection
import ie.equalit.ceno.R.string.preferences_delete_browsing_data
import ie.equalit.ceno.R.string.setting_item_selected
import ie.equalit.ceno.R.string.settings
import ie.equalit.ceno.R.string.thank_you_bridge_mode_enabled
import ie.equalit.ceno.R.string.title_success
import ie.equalit.ceno.R.string.toast_copied
import ie.equalit.ceno.R.string.toast_customize_addon_collection_done
import ie.equalit.ceno.R.string.tracker_category
import ie.equalit.ceno.R.string.view_logs
import ie.equalit.ceno.downloads.DownloadService
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.ext.getPreference
import ie.equalit.ceno.ext.getPreferenceKey
import ie.equalit.ceno.ext.getSwitchPreferenceCompat
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.settings.dialogs.LanguageChangeDialog
import ie.equalit.ceno.settings.dialogs.UpdateBridgeAnnouncementDialog
import ie.equalit.ceno.utils.CenoPreferences
import ie.equalit.ceno.utils.sentry.SentryOptionsConfiguration
import ie.equalit.ouinet.Config
import ie.equalit.ouinet.Ouinet
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mozilla.components.browser.state.action.ContentAction
import mozilla.components.browser.state.action.TabListAction
import mozilla.components.browser.state.state.content.DownloadState
import mozilla.components.browser.state.state.createTab
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.downloads.manager.FetchDownloadManager
import mozilla.components.lib.state.ext.consumeFrom
import mozilla.components.support.base.feature.PermissionsFeature
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.android.view.showKeyboard
import org.mozilla.geckoview.BuildConfig
import java.util.Locale
import kotlin.system.exitProcess


class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var cenoPrefs: CenoPreferences
    private val downloadsFeature = ViewBoundFeatureWrapper<DownloadsFeature>()

    private var job: Job? = null

    private var hasOuinetStopped: Boolean = false
    private var wasLogEnabled: Boolean = false
    private var bridgeModeChanged: Boolean = false
    private lateinit var bridgeAnnouncementDialog: AlertDialog
    private var logFileReset:Boolean = false
    private var logLevelReset:Boolean = false

    val getFileResult = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
        Log.d("DOC", uri.toString())
    }

    private val defaultClickListener = OnPreferenceClickListener { preference ->
        Toast.makeText(context, "${preference.title} Clicked", LENGTH_SHORT).show()
        true
    }

    private val sharedPreferencesChangeListener = OnSharedPreferenceChangeListener { sharedPrefs, key ->
        val newValue = sharedPrefs.getBoolean(key, false)
        if (key == getString(pref_key_shared_prefs_reload)) {
            Logger.debug("Got change listener for $key = $newValue")
            if (newValue) {
                Logger.debug("Reloading Settings fragment")
                CenoSettings.setStatusUpdateRequired(requireContext(), false)
                findNavController().popBackStack() // Pop before relaunching the fragment to preserve backstack
                findNavController().navigate(R.id.action_global_settings)
            }
        } else if (key == getString(pref_key_shared_prefs_update)) {
            if (newValue) {
                /* toggle preferences to refresh value */
                getPreference(pref_key_ouinet_state)?.let {
                    it.isEnabled = !(it.isEnabled)
                    it.isEnabled = !(it.isEnabled)
                }
                getPreference(pref_key_ceno_cache_size)?.let {
                    it.isEnabled = !(it.isEnabled)
                    it.isEnabled = !(it.isEnabled)
                }
                getPreference(pref_key_ceno_groups_count)?.let {
                    it.isEnabled = !(it.isEnabled)
                    it.isEnabled = !(it.isEnabled)
                }
                getPreference(pref_key_ceno_download_log)?.let {
                    it.isVisible = CenoSettings.isCenoLogEnabled(requireContext())
                    it.isEnabled = !(it.isEnabled)
                    it.isEnabled = !(it.isEnabled)
                }
                getPreference(pref_key_ceno_download_android_log)?.let {
                    it.isVisible = CenoSettings.isCenoLogEnabled(requireContext())
                    it.isEnabled = !(it.isEnabled)
                    it.isEnabled = !(it.isEnabled)
                }
                cenoPrefs.sharedPrefsUpdate = false
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        cenoPrefs = requireComponents.cenoPreferences
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /* TODO: The downloads feature is also used by the BaseBrowserFragment,
            should move this to a BaseFragment that both Settings and BaseBrowserFragment can inherit */
        downloadsFeature.set(
            feature = DownloadsFeature(
                requireContext(),
                store = requireComponents.core.store,
                useCases = requireComponents.useCases.downloadsUseCases,
                fragmentManager = childFragmentManager,
                downloadManager = FetchDownloadManager(
                    requireContext().applicationContext,
                    requireComponents.core.store,
                    DownloadService::class,
                    notificationsDelegate = requireComponents.notificationsDelegate
                ),
                onNeedToRequestPermissions = { permissions ->
                    // The Fragment class wants us to use registerForActivityResult
                    @Suppress("DEPRECATION")
                    requestPermissions(permissions, AppPermissionCodes.REQUEST_CODE_DOWNLOAD_PERMISSIONS)
                },
            ),
            owner = this,
            view = view,
        )

        (activity as BrowserActivity).themeManager.applyStatusBarThemeTabsTray()
        bridgeAnnouncementDialog = UpdateBridgeAnnouncementDialog(requireContext()).getDialog()
        bridgeAnnouncementDialog.setOnDismissListener {
            showThankyouDialog()
        }


        view.consumeFrom(requireComponents.appStore, viewLifecycleOwner) {
            CenoSettings.setOuinetState(requireContext(), it.ouinetStatus.name)
            getPreference(pref_key_ouinet_state)?.summaryProvider = Preference.SummaryProvider<Preference> {
                CenoSettings.getOuinetState(requireContext())
            }
            if (it.ouinetStatus == Ouinet.RunningState.Started) {
                bridgeAnnouncementDialog.dismiss()
                bridgeModeChanged = false
                getPreference(pref_key_bridge_announcement)?.onPreferenceChangeListener = getChangeListenerForBridgeAnnouncement()
            }
        }
        if (arguments?.getBoolean(SCROLL_TO_BRIDGE) == true) {
            getPreference(pref_key_bridge_announcement)?.let {
                scrollToPreference(it)
            }
        }


    }

    private fun showThankyouDialog() {
        if (CenoSettings.isBridgeAnnouncementEnabled(requireContext())) {
            AlertDialog.Builder(requireContext()).apply {
                setTitle(getString(title_success))
                setMessage(getString(thank_you_bridge_mode_enabled))
                setPositiveButton(getString(dialog_btn_positive_ok)) { _, _ ->

                }
                show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val feature: PermissionsFeature? = when (requestCode) {
            AppPermissionCodes.REQUEST_CODE_DOWNLOAD_PERMISSIONS -> downloadsFeature.get()
            else -> null
        }
        feature?.onPermissionsResult(permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        cenoPrefs.preferences.registerOnSharedPreferenceChangeListener(
            sharedPreferencesChangeListener
        )
        setupPreferences()
        setupCenoSettings()
        getActionBar().apply {
            show()
            setTitle(settings)
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

    override fun onPause() {
        super.onPause()
        cenoPrefs.preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesChangeListener)
        cenoPrefs.sharedPrefsReload = false
    }

    private fun setupPreferences() {

        getPreference(pref_key_make_default_browser)?.onPreferenceClickListener = getClickListenerForMakeDefaultBrowser()
        getSwitchPreferenceCompat(pref_key_remote_debugging)?.onPreferenceChangeListener = getChangeListenerForRemoteDebugging()
        getPreference(pref_key_about_page)?.onPreferenceClickListener = getAboutPageListener()
        getPreference(pref_key_privacy)?.onPreferenceClickListener = getClickListenerForPrivacy()
        getPreference(pref_key_override_amo_collection)?.onPreferenceClickListener = getClickListenerForCustomAddons()
        getPreference(pref_key_customization)?.onPreferenceClickListener = getClickListenerForCustomization()
        getPreference(pref_key_delete_browsing_data)?.onPreferenceClickListener = getClickListenerForDeleteBrowsingData()
        getSwitchPreferenceCompat(pref_key_allow_crash_reporting)?.onPreferenceChangeListener = getClickListenerForCrashReporting()
        getPreference(pref_key_search_engine)?.onPreferenceClickListener = getClickListenerForSearch()
        getPreference(pref_key_add_ons)?.onPreferenceClickListener = getClickListenerForAddOns()
        getPreference(pref_key_ceno_website_sources)?.onPreferenceClickListener = getClickListenerForWebsiteSources()
        getPreference(pref_key_bridge_announcement)?.onPreferenceChangeListener = getChangeListenerForBridgeAnnouncement()
        findPreference<Preference>(requireContext().getPreferenceKey(pref_key_change_language))?.onPreferenceClickListener = getClickListenerForLanguageChange()
        findPreference<Preference>(requireContext().getPreferenceKey(pref_key_change_language))?.summary = getCurrentLocale().displayLanguage
        getPreference(pref_key_search_engine)?.summary = getString(setting_item_selected, requireContext().components.core.store.state.search.selectedOrDefaultSearchEngine?.name)

        getPreference(pref_key_bridge_announcement)?.summary = getString(bridge_mode_ip_warning_text)


        // Update notifications
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !requireComponents.permissionHandler.isAllowingPostNotifications() -> {
                getPreference(pref_key_allow_notifications)?.isVisible = true
                getPreference(pref_key_allow_notifications)?.onPreferenceClickListener = getClickListenerForAllowNotifications()
            }

            else -> {
                getPreference(pref_key_allow_notifications)?.isVisible = false
            }
        }

        // Update battery optimization
        when {
            requireComponents.permissionHandler.isIgnoringBatteryOptimizations() -> {
                getPreference(pref_key_disable_battery_opt)?.isVisible = false
            }

            else -> {
                getPreference(pref_key_disable_battery_opt)?.isVisible = true
                getPreference(pref_key_disable_battery_opt)?.onPreferenceClickListener = getClickListenerForDisableBatteryOpt()
            }
        }

    }

    private fun setPreference(
        pref: Preference?,
        enabled: Boolean,
        changeListener: OnPreferenceChangeListener? = null,
        clickListener: OnPreferenceClickListener? = null
    ) {
        pref?.let {
            it.isEnabled = enabled
            it.shouldDisableView = !enabled
            it.onPreferenceChangeListener = changeListener
            it.onPreferenceClickListener = clickListener
        }
    }

    private fun setupCenoSettings() {
        getPreference(pref_key_ceno_download_log)?.isVisible = CenoSettings.isCenoLogEnabled(requireContext())
        getPreference(pref_key_ceno_download_android_log)?.isVisible = CenoSettings.isCenoLogEnabled(requireContext())
        (getPreference(pref_key_about_ceno) as LongClickPreference).let { preference ->
            preference.summary = CenoSettings.getCenoVersionString(requireContext())
            preference.onLongClick {
                //copy to clipboard
                copyToClipboard(preference.title, preference.summary)
                true
            }
        }
        (getPreference(pref_key_about_geckoview) as LongClickPreference).let { preference ->
            preference.summary = BuildConfig.MOZ_APP_VERSION + "-" + BuildConfig.MOZ_APP_BUILDID
            preference.onLongClick {
                copyToClipboard(preference.title, preference.summary)
                true
            }
        }

        if (CenoSettings.isStatusUpdateRequired(requireContext())) {
            /* Ouinet status not yet updated */
            /* Grey out all Ceno related options */
            setPreference(getPreference(pref_key_ceno_groups_count), false)
            setPreference(getPreference(pref_key_clear_ceno_cache), false)
            setPreference(getPreference(pref_key_ceno_network_config), false)
            setPreference(getPreference(pref_key_ceno_enable_log), false)
            setPreference(getPreference(pref_key_ceno_download_log), false)
            setPreference(getPreference(pref_key_ceno_download_android_log), false)
            /* Fetch ouinet status */
            CenoSettings.ouinetClientRequest(requireContext(), OuinetKey.API_STATUS)
        } else {
            /* Enable Ceno related options */
            getPreference(pref_key_ouinet_state)?.summaryProvider = Preference.SummaryProvider<Preference> {
                CenoSettings.getOuinetState(requireContext())
            }
            getPreference(pref_key_ceno_cache_size)?.summaryProvider = Preference.SummaryProvider<Preference> {
                CenoSettings.getCenoCacheSize(requireContext())
            }
            CenoSettings.ouinetClientRequest(requireContext(), OuinetKey.GROUPS_TXT)
            getPreference(pref_key_ceno_groups_count)?.summaryProvider = Preference.SummaryProvider<Preference> {
                resources.getQuantityString(
                    R.plurals.preferences_ceno_groups_count_subtitle,
                    CenoSettings.getCenoGroupsCount(requireContext()),
                    CenoSettings.getCenoGroupsCount(requireContext())
                )
            }
            setPreference(
                getPreference(pref_key_ceno_groups_count),
                true,
                clickListener = getClickListenerForCenoGroupsCounts()
            )
            setPreference(
                getPreference(pref_key_clear_ceno_cache),
                true,
                clickListener = getClickListenerForClearCenoCache()
            )
            setPreference(
                getPreference(pref_key_ceno_network_config),
                true,
                clickListener = getClickListenerForCenoNetworkDetails()
            )
            setPreference(
                getPreference(pref_key_ceno_enable_log),
                true,
                changeListener = getChangeListenerForLogFileToggle()
            )
            setPreference(
                getPreference(pref_key_ceno_download_log),
                true,
                clickListener = getClickListenerForOuinetLogExport()
            )
            setPreference(
                getPreference(pref_key_ceno_download_android_log),
                true,
                clickListener = getClickListenerForAndroidLogExport()
            )
            (getPreference(pref_key_about_ouinet) as LongClickPreference).let { preference ->
                preference.summary = CenoSettings.getOuinetVersion(requireContext()) + " " +
                        CenoSettings.getOuinetBuildId(requireContext())
                preference.onLongClick {
                    copyToClipboard(preference.title, preference.summary)
                    true
                }

            }
        }
    }

    private fun copyToClipboard(label: CharSequence?, text: CharSequence?) {
        //copy to clipboard
        val clipBoard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipBoard.setPrimaryClip(
            ClipData.newPlainText(
                label,
                text
            )
        )

        Toast.makeText(requireContext(), getString(toast_copied), LENGTH_SHORT).show()
    }


    private fun getClickListenerForMakeDefaultBrowser(): OnPreferenceClickListener {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            OnPreferenceClickListener {
                val intent = Intent(
                    Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS,
                )
                startActivity(intent)
                true
            }
        } else {
            defaultClickListener
        }
    }

    private fun getClickListenerForPrivacy(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            findNavController().navigate(
                R.id.action_settingsFragment_to_privacySettingsFragment
            )
            getActionBar().setTitle(tracker_category)
            true
        }
    }

    private fun getClickListenerForCustomization(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            findNavController().navigate(
                R.id.action_settingsFragment_to_customizationSettingsFragment
            )
            true
        }
    }

    private fun getClickListenerForSearch(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            findNavController().navigate(
                R.id.action_settingsFragment_to_installedSearchEnginesSettingsFragment
            )
            getActionBar().setTitle(preference_choose_search_engine)
            true
        }
    }

    private fun getClickListenerForDeleteBrowsingData(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            findNavController().navigate(
                R.id.action_settingsFragment_to_deleteBrowsingDataFragment
            )
            getActionBar().setTitle(preferences_delete_browsing_data)
            true
        }
    }

    private fun getClickListenerForAddOns(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            findNavController().navigate(R.id.action_global_addons)
            true
        }
    }

    private fun getClickListenerForWebsiteSources(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_websiteSourceSettingsFragment)
            true
        }
    }

    private fun getClickListenerForCrashReporting(): OnPreferenceChangeListener {
        return OnPreferenceChangeListener { _, _ ->
            // Re-initialize Sentry-Android
            SentryAndroid.init(
                requireContext(),
                SentryOptionsConfiguration.getConfig(requireContext())
            )

//            Re-allow post-crash permissions nudge
//            This should ALWAYS be turned on when this permission state is toggled
            ie.equalit.ceno.settings.Settings.toggleCrashReportingPermissionNudge(requireContext(), true)
            true
        }
    }

    private fun getChangeListenerForRemoteDebugging(): OnPreferenceChangeListener {
        return OnPreferenceChangeListener { _, newValue ->
            requireComponents.core.engine.settings.remoteDebuggingEnabled = newValue as Boolean
            true
        }
    }

    private fun getAboutPageListener(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            findNavController().navigate(
                R.id.action_settingsFragment_to_aboutFragment
            )
            getActionBar().setTitle(preferences_about_page)
            true
        }
    }

    private fun getActionBar() = (activity as AppCompatActivity).supportActionBar!!

    private fun getClickListenerForAndroidLogExport(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            exportAndroidLogs()
            true
        }
    }

    private fun getClickListenerForCustomAddons(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            val context = requireContext()
            val dialogView = View.inflate(context, R.layout.amo_collection_override_dialog, null)
            val userView = dialogView.findViewById<EditText>(R.id.custom_amo_user)
            val collectionView = dialogView.findViewById<EditText>(R.id.custom_amo_collection)

            AlertDialog.Builder(context).apply {
                setTitle(context.getString(preferences_customize_amo_collection))
                setView(dialogView)
                setNegativeButton(customize_addon_collection_cancel) { dialog: DialogInterface, _ ->
                    dialog.cancel()
                }

                setPositiveButton(customize_addon_collection_ok) { _, _ ->
                    ie.equalit.ceno.settings.Settings.setOverrideAmoUser(
                        context,
                        userView.text.toString()
                    )
                    ie.equalit.ceno.settings.Settings.setOverrideAmoCollection(
                        context,
                        collectionView.text.toString()
                    )

                    Toast.makeText(
                        context,
                        getString(toast_customize_addon_collection_done),
                        Toast.LENGTH_LONG,
                    ).show()

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            exitProcess(0)
                        },
                        AMO_COLLECTION_OVERRIDE_EXIT_DELAY,
                    )
                }

                collectionView.setText(
                    ie.equalit.ceno.settings.Settings.getOverrideAmoCollection(
                        context
                    )
                )
                userView.setText(ie.equalit.ceno.settings.Settings.getOverrideAmoUser(context))
                userView.requestFocus()
                userView.showKeyboard()
                create()
            }.show()
            true
        }
    }

    /*
    private fun getChangeListenerForMobileData(): OnPreferenceChangeListener {
        return OnPreferenceChangeListener { _, newValue ->
            if (newValue == true) {
                /* this should pop-up the mobile data dialog to provide warning and allow user to select option*/
                Toast.makeText(context, preferences_mobile_data_warning_disabled, LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(context, preferences_mobile_data_warning_enabled, LENGTH_SHORT).show()
            }
            true
        }
    }
     */

    private fun getClickListenerForAllowNotifications(): OnPreferenceClickListener {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            OnPreferenceClickListener {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireActivity().packageName)
                    requireActivity().startActivity(this)
                }
                true
            }
        } else {
            defaultClickListener
        }
    }

    private fun getClickListenerForDisableBatteryOpt(): OnPreferenceClickListener {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            OnPreferenceClickListener {
                requireComponents.permissionHandler.requestBatteryOptimizationsOff(requireActivity())
                true
            }
        } else {
            defaultClickListener
        }
    }

    private fun getChangeListenerForLogFileToggle(): OnPreferenceChangeListener {
        return OnPreferenceChangeListener { _, newValue ->

            // network request to update preference value
            CenoSettings.ouinetClientRequest(
                context = requireContext(),
                key = OuinetKey.LOGFILE,
                newValue = if(newValue == true) OuinetValue.ENABLED else OuinetValue.DISABLED,
                stringValue = null,
                object : OuinetResponseListener {
                    override fun onSuccess(message: String, data: Any?) {
                        requireComponents.cenoPreferences.sharedPrefsUpdate = true
                    }
                    override fun onError() {
                        Log.e(TAG, "Failed to set log file to newValue: $newValue")
                    }
                }
            )

            // network request to update log level based on preference value
            CenoSettings.ouinetClientRequest(
                context = requireContext(),
                key = OuinetKey.LOG_LEVEL,
                stringValue = if(newValue == true) Config.LogLevel.DEBUG.toString() else Config.LogLevel.INFO.toString()
            )

            true
        }
    }

    private fun getClickListenerForClearCenoCache(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            AlertDialog.Builder(requireContext()).apply {
                setTitle(getString(confirm_clear_cached_content))
                setMessage(getString(confirm_clear_cached_content_desc))
                setNegativeButton(getString(ceno_clear_dialog_cancel)) { _, _ -> }
                setPositiveButton(getString(onboarding_battery_button)) { _, _ ->
                    CenoSettings.ouinetClientRequest(requireContext(), OuinetKey.PURGE_CACHE)
                    //ClearButtonFeature.createClearDialog(requireContext()).show()
                }
                create()
            }.show()
            true
        }
    }

    private fun getClickListenerForCenoGroupsCounts(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            CenoSettings.ouinetClientRequest(
                context = requireContext(),
                key = OuinetKey.GROUPS_TXT,
                ouinetResponseListener = object : OuinetResponseListener {
                    override fun onSuccess(message: String, data: Any?) {
                        if (message.trim().isEmpty()) {
                            Toast.makeText(requireContext(), getString(no_content_shared), Toast.LENGTH_LONG).show()
                        } else {
                            findNavController().navigate(
                                R.id.action_settingsFragment_to_siteContentGroupFragment,
                                bundleOf("groups" to message)
                            )
                        }
                    }

                    override fun onError() {
                        Toast.makeText(
                            requireContext(),
                            ContextCompat.getString(requireContext(), ouinet_client_fetch_fail),
                            LENGTH_SHORT
                        ).show()
                    }
                },
                shouldRefresh = false
            )
            true
        }
    }

    private fun getClickListenerForCenoNetworkDetails(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            findNavController().navigate(
                R.id.action_settingsFragment_to_networkSettingsFragment
            )
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

    private fun getChangeListenerForBridgeAnnouncement(): OnPreferenceChangeListener {
        return OnPreferenceChangeListener { _, _ ->
            /* Resetting the log settings is a workaround for ouinet logs disappearing after toggling bridge mode,
            * https://gitlab.com/censorship-no/ceno-browser/-/merge_requests/127#note_1795759444
            * TODO: identify root cause of this behavior and remove workaround
            * */
            if (!bridgeModeChanged) {
                bridgeModeChanged = true
                getPreference(pref_key_bridge_announcement)?.onPreferenceChangeListener = null
                wasLogEnabled = CenoSettings.isCenoLogEnabled(requireContext())
                if (wasLogEnabled) {
                    CenoSettings.setCenoEnableLog(requireContext(), false)
                    setLogFileAndLevel(false)
                }
                monitorOuinet()
                lifecycleScope.launch {
                    if (wasLogEnabled) {
                        while (!(logFileReset && logLevelReset)) {
                            delay(DELAY_ONE_SECOND)
                            println("logFileReset && logLevelReset = $logFileReset && $logLevelReset")
                        }
                    }
                    requireComponents.ouinet.background.shutdown(false) {
                        hasOuinetStopped = true
                    }
                }
                bridgeAnnouncementDialog.show()
            }
            true
        }
    }

    private fun getClickListenerForLanguageChange(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            val languageChangeDialog = LanguageChangeDialog(
                requireContext(),
                object : LanguageChangeDialog.SetLanguageListener {
                    override fun onLanguageSelected(locale: Locale) {

                        // update language
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.create(Locale.forLanguageTag(locale.toLanguageTag()))
                        )
                        ie.equalit.ceno.settings.Settings.clearAnnouncementData(requireContext())
                    }
                }
            )

            languageChangeDialog.getDialog().show()
            true
        }
    }

    private fun setLogFileAndLevel (newValue : Boolean) {
        // network request to update preference value
        CenoSettings.ouinetClientRequest(
            context = requireContext(),
            key = OuinetKey.LOGFILE,
            newValue = if(newValue) OuinetValue.ENABLED else OuinetValue.DISABLED,
            null,
            object : OuinetResponseListener {
                override fun onSuccess(message: String, data: Any?) {
                    logFileReset = !newValue
                }
                override fun onError() {
                    /* Still flag reset complete on error, since not flagging will cause dialog to hang */
                    logFileReset = !newValue
                }
            }
        )
        // network request to update log level based on preference value
        CenoSettings.ouinetClientRequest(
            context = requireContext(),
            key = OuinetKey.LOG_LEVEL,
            newValue = null,
            stringValue = if(newValue) Config.LogLevel.DEBUG.toString() else Config.LogLevel.INFO.toString(),
            object : OuinetResponseListener {
                override fun onSuccess(message: String, data: Any?) {
                    logLevelReset = !newValue
                }
                override fun onError() {
                    /* Still flag reset complete on error, since not flagging will cause dialog to hang */
                    logLevelReset = !newValue
                }
            }
        )
    }

    private fun monitorOuinet() {
        lifecycleScope.launch {
            while (!hasOuinetStopped) {
                delay(DELAY_ONE_SECOND)
            }
            requireComponents.ouinet.setConfig()
            requireComponents.ouinet.setBackground(requireContext())
            requireComponents.ouinet.background.startup {
                hasOuinetStopped = false
                /* if debug log previously enabled, re-enable it after startup completes */
                if (wasLogEnabled) {
                    CenoSettings.setCenoEnableLog(requireContext(), true)
                    setLogFileAndLevel(true)
                }
            }
        }
    }

    private fun exportAndroidLogs() {
        /*
        To test the scrubbing locally, uncomment the lines below
        These test logs would be in the last lines of the generated logs and can thus be analyzed
        */

//                    val logTag = "test"
//
//                    Log.d(logTag,"Phone number: 123-456-7890")
//                    Log.d(logTag,"Email address: sample@samplemail.com")
//                    Log.d(logTag,"Mac address: 00:1A:2B:3C:4D:5E")
//                    Log.d(logTag,"Local ipv4 address: 192.168.0.1")
//                    Log.d(logTag,"Non-local ipv4 address: 8.8.8.8")
//                    Log.d(logTag,"Ipv6 address: 2001:0db8:85a3:0000:0000:8a2e:0370:7334\n")

        // Ask user to choose log filter
        val exportDialog = ExportAndroidLogsDialog(requireContext(), this)
        exportDialog.getDialog().show()
    }

    companion object {
        private const val AMO_COLLECTION_OVERRIDE_EXIT_DELAY = 3000L
        const val TAG = "SettingsFragment"
        const val LOG = "log"

        const val LOG_FILE_SIZE_LIMIT_MB = 20.0

        const val LOGS_LAST_5_MINUTES = 300000L
        const val LOGS_LAST_10_MINUTES = 600000L

        const val AVERAGE_TOTAL_LOGS = 3000F

        const val SCROLL_TO_BRIDGE = "scrollToBridge"
        const val DELAY_ONE_SECOND = 1000L

        fun getCurrentLocale() = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        } else {
            Locale.getDefault()
        })
    }
}
