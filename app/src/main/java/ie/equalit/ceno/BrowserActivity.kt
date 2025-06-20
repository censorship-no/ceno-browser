/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.AttributeSet
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import ie.equalit.ceno.addons.WebExtensionActionPopupActivity
import ie.equalit.ceno.base.BaseActivity
import ie.equalit.ceno.browser.BrowserFragment
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.browser.BrowsingModeManager
import ie.equalit.ceno.browser.DefaultBrowsingManager
import ie.equalit.ceno.browser.ExternalAppBrowserFragment
import ie.equalit.ceno.browser.notification.AbstractPublicNotificationService
import ie.equalit.ceno.browser.notification.CenoNotificationBroadcastReceiver
import ie.equalit.ceno.browser.notification.PublicNotificationFeature
import ie.equalit.ceno.browser.notification.PublicNotificationService
import ie.equalit.ceno.components.ceno.TopSitesStorageObserver
import ie.equalit.ceno.components.ceno.appstate.AppAction
import ie.equalit.ceno.ext.ceno.sort
import ie.equalit.ceno.ext.cenoPreferences
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.ext.isInstallFromUpdate
import ie.equalit.ceno.home.HomeFragment.Companion.BEGIN_TOUR_TOOLTIP
import ie.equalit.ceno.metrics.ConsentRequestDialog
import ie.equalit.ceno.settings.CenoSettings
import ie.equalit.ceno.settings.OuinetKey
import ie.equalit.ceno.settings.OuinetResponseListener
import ie.equalit.ceno.settings.OuinetValue
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.settings.SettingsFragment
import ie.equalit.ceno.standby.StandbyFragment
import ie.equalit.ceno.ui.theme.DefaultThemeManager
import ie.equalit.ceno.ui.theme.ThemeManager
import ie.equalit.ceno.utils.sentry.SentryOptionsConfiguration
import ie.equalit.ouinet.Ouinet.RunningState
import ie.equalit.ouinet.OuinetNotification
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mozilla.components.browser.state.selector.findCustomTabOrSelectedTab
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.state.WebExtensionState
import mozilla.components.browser.state.state.searchEngines
import mozilla.components.browser.state.state.selectedOrDefaultSearchEngine
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.engine.manifest.WebAppManifest
import mozilla.components.feature.intent.ext.EXTRA_SESSION_ID
import mozilla.components.feature.pwa.ext.putWebAppManifest
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.utils.SafeIntent
import mozilla.components.support.webextensions.WebExtensionPopupObserver
import java.util.regex.Pattern
import kotlin.system.exitProcess

/**
 * Activity that holds the [BrowserFragment].
 */
open class BrowserActivity : BaseActivity(), CenoNotificationBroadcastReceiver.NotificationListener {

    lateinit var themeManager: ThemeManager
    lateinit var browsingModeManager: BrowsingModeManager
    private val screenStartTime = System.currentTimeMillis()
    private var ouinetStartupTime = 0.0
    private var hasOuinetStarted = false
    private var hasRanChecksAndPermissions = false

    private val sessionId: String?
        get() = SafeIntent(intent).getStringExtra(EXTRA_SESSION_ID)

    private val tab: SessionState?
        get() = components.core.store.state.findCustomTabOrSelectedTab(sessionId)

    private var publicNotificationObserver: PublicNotificationFeature<PublicNotificationService>? =
        null
    private lateinit var cenoNotificationBroadcastReceiver: CenoNotificationBroadcastReceiver

    private val webExtensionPopupObserver by lazy {
        WebExtensionPopupObserver(components.core.store, ::openPopup)
    }

    private val navHost by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }

    private var isActivityResumed = false
    private var lastCall: (() -> Unit)? = null

    private lateinit var reminderNotificationIntent : PendingIntent
    private lateinit var alarmManager:AlarmManager
    /**
     * Returns a new instance of [BrowserFragment] to display.
     */
    open fun createBrowserFragment(sessionId: String?) {
        navHost.navController.navigate(R.id.action_global_browser)
    }

    /**
     * Returns a new instance of [ExternalAppBrowserFragment] to display.
     */
    open fun createExternalAppBrowserFragment(
        sessionId: String,
        manifest: WebAppManifest?,
        trustedScopes: List<Uri>
    ) {
        navHost.navController.navigate(R.id.action_global_external_browser, Bundle().apply {
            "session_id" to sessionId
            putWebAppManifest(manifest)
            putParcelableArrayList("org.mozilla.samples.browser.TRUSTED_SCOPES", ArrayList(trustedScopes))
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupThemeAndBrowsingMode(getModeFromIntentOrLastKnown(intent))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navHost.navController.addOnDestinationChangedListener { _, destination, _ ->
            if((destination.id == R.id.homeFragment || destination.id == R.id.browserFragment) && !hasRanChecksAndPermissions) {
                hasRanChecksAndPermissions = true

                if( isInstallFromUpdate() && isVersionForConsent(this) && cenoPreferences().showMetricsConsentDialog) {
                    val dialog = ConsentRequestDialog(this)
                    dialog.show (
                        complete = { granted ->
                            //web api call
                            CenoSettings.ouinetClientRequest(
                                context = this,
                                key = OuinetKey.CENO_METRICS,
                                newValue = if (granted) OuinetValue.ENABLE else OuinetValue.DISABLE,
                                stringValue = null,
                                object : OuinetResponseListener {
                                    override fun onSuccess(message: String, data: Any?) {
                                        Settings.setOuinetMetricsEnabled(this@BrowserActivity, granted)
                                    }
                                    override fun onError() {
                                        Log.e(TAG, "Failed to set metrics to newValue: $granted")
                                    }
                                }
                            )
                            cenoPreferences().showMetricsConsentDialog = false
                        },
                        openMetricsSettings = {
                            navHost.navController.navigate(R.id.action_global_metricsCampaignFragment)
                        }
                    )
                }

                if (Settings.showCrashReportingPermissionNudge(this)) {
                    showCrashReportingPermission()
                }
            }
        }

        components.useCases.customLoadUrlUseCase.onNoSelectedTab = { url ->
            openToBrowser(url, newTab = true, private = themeManager.currentMode.isPersonal)
        }

        Logger.info(" --------- Starting ouinet service")
        components.ouinet.setBackground(this)

        components.ouinet.background.startup()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Settings.setAllowNotifications(this, components.permissionHandler.isAllowingPostNotifications())

        /* CENO: Set default behavior for AppBar */
        supportActionBar!!.apply {
            hide()
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(
                ContextCompat.getColor(
                    this@BrowserActivity,
                    R.color.ceno_action_bar
                ).toDrawable())
        }

        publicNotificationObserver = PublicNotificationFeature(
            applicationContext,
            components.core.store,
            PublicNotificationService::class,
        ).also {
            it.start()
        }

        cenoNotificationBroadcastReceiver = CenoNotificationBroadcastReceiver(this)
        val notificationIntentFilter = IntentFilter()
        notificationIntentFilter.addAction(AbstractPublicNotificationService.ACTION_CLEAR)
        notificationIntentFilter.addAction(AbstractPublicNotificationService.ACTION_STOP)
        notificationIntentFilter.addAction(ACTION_FORGROUND_REMIND)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.registerReceiver(cenoNotificationBroadcastReceiver, notificationIntentFilter, Context.RECEIVER_NOT_EXPORTED)
        }
        else {
            ContextCompat.registerReceiver(
                this,
                cenoNotificationBroadcastReceiver,
                notificationIntentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        if (Settings.shouldShowOnboarding(this)) {
            components.cenoPreferences.nextTooltip = BEGIN_TOUR_TOOLTIP
        }

        navHost.navController.popBackStack() // Remove startupFragment from backstack

        when {
//                Settings.shouldShowOnboarding(this) && savedInstanceState == null -> R.id.action_global_onboarding
            components.ouinet.background.getState() != RunningState.Started.toString() -> {
                val bundle = bundleOf(StandbyFragment.shutdownCeno to false)
                navHost.navController.navigate(R.id.action_global_standbyFragment, bundle)
            }
            components.core.store.state.selectedTab == null -> navHost.navController.navigate(R.id.action_global_home)
            else -> navHost.navController.navigate(R.id.action_global_browser)
        }

        /* CENO: need to initialize top sites to be displayed in CenoHomeFragment */
        initializeTopSites()

        initializeSearchEngines()

        components.webExtensionPort.createPort()

        /* Do not notify user of data policy because we are not collecting telemetry data
        *  and we already have a notification for stopping/pausing/purging local CENO data
        * NotificationManager.checkAndNotifyPolicy(this)
         */
        lifecycle.addObserver(webExtensionPopupObserver)

        // check if a crash happened in the last session
        if(Settings.wasCrashSuccessfullyLogged(this@BrowserActivity)) {
            Settings.logSuccessfulCrashEvent(this@BrowserActivity, false)
            Toast.makeText(this@BrowserActivity, getString(R.string.crash_report_sent), Toast.LENGTH_SHORT).show()
        }

        // reset the value of lastCrash if permission nudge won't be shown
        if(!Settings.showCrashReportingPermissionNudge(this)) {
            Settings.setCrashHappened(this@BrowserActivity, false) // reset the value of lastCrash
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

            reminderNotificationIntent = Intent(ACTION_FORGROUND_REMIND).let {
                it.setPackage(packageName)
                PendingIntent.getBroadcast(
                    applicationContext, 0, it,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
        }
        updateOuinetStatus()
    }

    /* This function displays the popup that asks users if they want to opt in for
    the crash reporting feature
     */
    private fun showCrashReportingPermission() {
        // launch Sentry activation dialog
        val dialogView = View.inflate(this@BrowserActivity, R.layout.crash_reporting_nudge_dialog, null)
        val radio0 = dialogView.findViewById<RadioButton>(R.id.radio0)
        val radio1 = dialogView.findViewById<RadioButton>(R.id.radio1)

        val sentryActionDialog by lazy { AlertDialog.Builder(this).apply {
            setPositiveButton(getString(R.string.onboarding_warning_button)) { _, _ -> }
        } }

        AlertDialog.Builder(this).apply {
            setView(dialogView)
            setPositiveButton(getString(R.string.onboarding_battery_button)) { _, _ ->
                when {
                    radio0.isChecked -> {
                        Settings.alwaysAllowCrashReporting(this@BrowserActivity)
                        SentryAndroid.init(this@BrowserActivity, SentryOptionsConfiguration.getConfig(this@BrowserActivity))

                        sentryActionDialog.setMessage(getString(R.string.crash_reporting_opt_in)).show()
                    }
                    radio1.isChecked -> {
                        Settings.neverAllowCrashReporting(this@BrowserActivity)
                        sentryActionDialog.setMessage(getString(R.string.crash_reporting_opt_out)).show()
                    }
                }
            }
            setOnDismissListener {
                Settings.setCrashHappened(this@BrowserActivity, false) // reset the value of lastCrash
            }
            setNegativeButton(getString(R.string.mozac_feature_prompt_not_now)) { _, _ ->
                Settings.setCrashHappened(this@BrowserActivity, false) // reset the value of lastCrash
            }
            create()
        }.show()
    }

    private fun getModeFromIntentOrLastKnown(intent: Intent?): BrowsingMode {
        return if (components.core.store.state.selectedTab == null)
            BrowsingMode.Normal
        else cenoPreferences().lastKnownBrowsingMode
    }

    private fun setupThemeAndBrowsingMode(mode: BrowsingMode) {
        cenoPreferences().lastKnownBrowsingMode = mode
        themeManager = DefaultThemeManager(mode, this)
        browsingModeManager = DefaultBrowsingManager(mode, cenoPreferences()) {newMode ->
            themeManager.currentMode = newMode
            components.appStore.dispatch(AppAction.ModeChange(newMode))
        }
        //components.appStore.dispatch(AppAction.ModeChange(mode))
    }

    private fun updateOuinetStatus() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    val status = RunningState.valueOf(components.ouinet.background.getState())
                    if (components.appStore.state.ouinetStatus != status) {
                        components.appStore.dispatch(AppAction.OuinetStatusChange(status))
                        if(!hasOuinetStarted && status == RunningState.Started) {
                            ouinetStartupTime = (System.currentTimeMillis() - screenStartTime) / 1000.0
                            hasOuinetStarted = true
                        }

                    }
                    delay(DELAY_TWO_SECONDS)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isActivityResumed = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM)
            alarmManager.set(AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + FOREGROUND_TIMEOUT_REMINDER_DURATION, reminderNotificationIntent)
    }

    override fun onStart() {
        super.onStart()
        components.notificationsDelegate.bindToActivity(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        components.notificationsDelegate.unBindActivity(this)
        publicNotificationObserver?.stop()
        this.unregisterReceiver(cenoNotificationBroadcastReceiver)
    }

    override fun onResume() {
        super.onResume()
        if (components.ouinet.background.getState() != RunningState.Started.toString()) {
            if (navHost.navController.currentDestination?.id  != R.id.standbyFragment) {
                navHost.navController.popBackStack()
                val bundle = bundleOf(StandbyFragment.shutdownCeno to false)
                navHost.navController.navigate(R.id.action_global_standbyFragment, bundle)
            }
        }
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) && components.ouinet.background.getState() != RunningState.Started.toString()) {
            /* CENO: in Android 9 or later, it is possible that the
             * service may have stopped while app was in background
             * try sending an intent to restart the service
             */
            Logger.info(" --------- Starting ouinet service onResume")
            components.ouinet.background.start()
        }
        isActivityResumed = true
        //If we have some fragment to show do it now then clear the queue
        if(lastCall != null){
            updateView(lastCall!!)
            lastCall = null
        }

        /*
        CENO: Update behavior for AppBar
        This needs to be optimized to reduce the need to update this part of the codebase when a new fragment is created
        */
        supportActionBar!!.apply {
            when(navHost.navController.currentDestination?.id) {
                R.id.settingsFragment, R.id.networkSettingsFragment, R.id.privacySettingsFragment,
                R.id.customizationSettingsFragment, R.id.installedSearchEnginesSettingsFragment,
                R.id.deleteBrowsingDataFragment, R.id.aboutFragment, R.id.websiteSourceSettingsFragment -> show()
                else -> hide()
            }
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(
                ContextCompat.getColor(
                    this@BrowserActivity,
                    R.color.ceno_action_bar
                ).toDrawable())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM)
            alarmManager.cancel(reminderNotificationIntent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        /* If coming from settings fragment, always clear back stack and go back to root fragment */
        if (navHost.navController.currentDestination?.id == R.id.settingsFragment) {
            if (components.core.store.state.selectedTabId == "" ||
                components.core.store.state.selectedTabId == null
            ) {
                navHost.navController.popBackStack(R.id.homeFragment, true)
                navHost.navController.navigate(R.id.action_global_home)
            }
            else {
                navHost.navController.navigate(R.id.action_global_browser)
            }
            return
        }
        if (navHost.navController.currentDestination?.id == R.id.aboutFragment) {
            navHost.navController.navigate(R.id.action_global_settings)
            return
        }

        val fragment: Fragment? = navHost.childFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if ((fragment is UserInteractionHandler) && fragment.onBackPressed()) {
            return
        }

        super.onBackPressed()

        removeSessionIfNeeded()
    }

    val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        Settings.setAllowNotifications(this, isGranted)
        components.permissionHandler.requestBatteryOptimizationsOff(this)
    }

    val getLogfileLocation = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        try {
            if (uri != null) {

                // get logs from internal storage
                this.openFileInput("${getString(R.string.ceno_android_logs_file_name)}.txt").bufferedReader().useLines { lines ->
                    val fileContent = lines.toMutableList().joinToString("\n")
                    val file = contentResolver.openOutputStream(uri)
                    file?.write(fileContent.toByteArray())
                    file?.close()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
    }

    /* CENO: Handle intent sent to BrowserActivity to open to Homepage or open a homescreen shortcut link */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val safeIntent = SafeIntent(intent)
        if (safeIntent.action == AbstractPublicNotificationService.ACTION_TAP) {
            val bundle = bundleOf(SettingsFragment.SCROLL_TO_CACHE to true)
            navHost.navController.navigate(R.id.action_global_settings, bundle)
        }
        if (safeIntent.action == Intent.ACTION_MAIN &&
            safeIntent.hasExtra(OuinetNotification.FROM_NOTIFICATION_EXTRA)
        ) {
            navHost.navController.navigate(R.id.action_global_home)
        }
        if (safeIntent.action == Intent.ACTION_VIEW) {
            navHost.navController.navigate(R.id.action_global_browser)
        }

    }

    /**
     * If needed remove the current session.
     *
     * If a session is a custom tab or was opened from an external app then the session gets removed once you go back
     * to the third-party app.
     *
     * Eventually we may want to move this functionality into one of our feature components.
     */
    private fun removeSessionIfNeeded(): Boolean {
        val session = tab ?: return false

        return if (session.source is SessionState.Source.External && !session.restored) {
            finish()
            components.useCases.tabsUseCases.removeTab(session.id)
            true
        } else {
            val hasParentSession = session is TabSessionState && session.parentId != null
            if (hasParentSession) {
                components.useCases.tabsUseCases.removeTab(session.id, selectParentIfExists = true)
            }
            // We want to return to home if this session didn't have a parent session to select.
            val goToOverview = !hasParentSession
            !goToOverview
        }
    }

    override fun onUserLeaveHint() {
        val fragment: Fragment? = navHost.childFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (fragment is UserInteractionHandler && fragment.onHomePressed()) {
            return
        }

        super.onUserLeaveHint()
    }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? =
        when (name) {
            EngineView::class.java.name -> components.core.engine.createView(context, attrs).asView()
            else -> super.onCreateView(parent, name, context, attrs)
        }

    private fun openPopup(webExtensionState: WebExtensionState) {
        val intent = Intent(this, WebExtensionActionPopupActivity::class.java)
        intent.putExtra("web_extension_id", webExtensionState.id)
        intent.putExtra("web_extension_name", webExtensionState.name)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    /* CENO: Add function to open requested site in BrowserFragment */
    fun openToBrowser(url : String? = null, newTab : Boolean = false, private: Boolean = false){
        if (url != null) {
            if (newTab) {
                //set browsingMode
                browsingModeManager.mode = BrowsingMode.fromBoolean(private)
                components.useCases.tabsUseCases.addTab(
                    url = url,
                    selectTab = true,
                    private = private,
                )
            } else {
                components.useCases.sessionUseCases.loadUrl(
                    url = url
                )
            }
        }
        showBrowser()
    }

    private fun showBrowser() {

        if(navHost.navController.currentDestination?.id == R.id.browserFragment) {
            return
        }

        navHost.navController.navigate(R.id.action_global_browser)
    }
    fun switchBrowsingModeHome(currentMode: BrowsingMode) {
        browsingModeManager.mode = BrowsingMode.fromBoolean(!currentMode.isPersonal)

        components.appStore.dispatch(AppAction.ModeChange(browsingModeManager.mode))
    }

    fun updateView(action: () -> Unit){
        //If the activity is in background we register the transaction
        if(!isActivityResumed){
            lastCall = action
        } else {
            //Else we just invoke it
            action.invoke()
        }
    }

    private fun shutdownCallback(doClear: Boolean) : Runnable {
        return Runnable {
            if (doClear) {
                val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                am.clearApplicationUserData()
            }
            exitOuinetServiceProcess()
            exitProcess(0)
        }
    }

    fun beginShutdown(doClear : Boolean, stalledDuration: Long = resources.getInteger(R.integer.shutdown_fragment_stalled_duration).toLong()) {
        val handler = Handler(Looper.myLooper()!!)
        val callback = shutdownCallback(doClear)
        handler.postDelayed(
            callback,
            stalledDuration
        )
        components.ouinet.background.shutdown(doClear) {
            handler.removeCallbacks(callback)
            callback.run()
        }
        updateView {
            navHost.navController.navigate(R.id.action_global_standbyFragment, bundleOf(
                StandbyFragment.DO_CLEAR to doClear,
                StandbyFragment.shutdownCeno to true
            ))
        }
    }

    fun exitOuinetServiceProcess() {
        getSystemService(Context.ACTIVITY_SERVICE).let { am ->
            (am as ActivityManager).runningAppProcesses?.let { processes ->
                for (process in processes) {
                    if (process.processName.contains("ouinetService")){
                        Process.killProcess(process.pid)
                    }
                }
            }
        }
    }

    /* CENO: Function to initialize top site storage and observer */
    @OptIn(DelicateCoroutinesApi::class)
    private fun initializeTopSites() {
        /*  Launch a coroutine to initialize top site storage cache and update it in the store */
        GlobalScope.launch(Dispatchers.IO) {
            components.core.cenoTopSitesStorage.getTopSites(
                totalSites = components.cenoPreferences.topSitesMaxLimit
            )
            components.appStore.dispatch(
                AppAction.Change(
                    topSites = components.core.cenoTopSitesStorage.cachedTopSites.sort()
                )
            )
        }

        /* Register TopSitesStorageObserver, which will update AppStore when top sites are changed/added/removed */
        components.core.cenoTopSitesStorage.apply{
            register(
                observer = TopSitesStorageObserver(
                    this,
                    components.cenoPreferences,
                    components.appStore)
            )
        }
    }

    private fun initializeSearchEngines() {
        if (Settings.shouldUpdateSearchEngines(this)) {
            components.core.store.state.search.searchEngines.filter { searchEngine ->
                searchEngine.id in listOf(
                        getString(R.string.remove_search_engine_id_1),
                        getString(R.string.remove_search_engine_id_2))
            }.forEach { searchEngine ->
                components.useCases.searchUseCases.removeSearchEngine(searchEngine)
            }
            components.core.store.state.search.searchEngines.forEach { searchEngine ->
                if (searchEngine.id == getString(R.string.default_search_engine_id)) {
                    components.useCases.searchUseCases.selectSearchEngine(searchEngine)
                }
            }
            Logger.debug("${components.core.store.state.search.searchEngines}")
            Logger.debug("${components.core.store.state.search.selectedOrDefaultSearchEngine}")
            Settings.setUpdateSearchEngines(this, false)
        }
    }

    fun openSettings() {
        val bundle = bundleOf(SettingsFragment.SCROLL_TO_BRIDGE to true)
        navHost.navController.navigate(R.id.action_global_settings, bundle)
    }

    companion object {
        private const val TAG = "BrowserActivity"
        const val DELAY_TWO_SECONDS = 2000L
        fun isVersionForConsent(context: Context) : Boolean {
            return Pattern.compile("\\A2\\.6\\.\\d\\z").matcher(
                context.packageManager.getPackageInfo(context.packageName, 0).versionName.toString()
            ).matches()
        }
        const val ACTION_FORGROUND_REMIND = "ie.equalit.ceno.browser.notification.action.REMIND"
        const val FOREGROUND_TIMEOUT_REMINDER_DURATION: Long = 18000000L
    }

    override fun onStopTapped() {
        publicNotificationObserver?.stop()
        var duration = if (this.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            resources.getInteger(R.integer.shutdown_fragment_stalled_duration).toLong()
        } else {
            500L
        }
        beginShutdown(doClear = false, stalledDuration = duration)
    }

    override fun onClearTapped() {
        publicNotificationObserver?.stop()
        //if the app is in foreground, set the duration to show standby fragment until ouinet is closed to 15seconds
        var duration = if (this.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            resources.getInteger(R.integer.shutdown_fragment_stalled_duration).toLong()
        } else {
            500L
        }
        beginShutdown(doClear = true, stalledDuration = duration)
    }
}
