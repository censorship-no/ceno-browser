/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.browser

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.lifecycleScope
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import ie.equalit.ceno.AppPermissionCodes.REQUEST_CODE_APP_PERMISSIONS
import ie.equalit.ceno.AppPermissionCodes.REQUEST_CODE_DOWNLOAD_PERMISSIONS
import ie.equalit.ceno.AppPermissionCodes.REQUEST_CODE_PROMPT_PERMISSIONS
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.BuildConfig
import ie.equalit.ceno.R
import ie.equalit.ceno.addons.WebExtensionActionPopupPanel
import ie.equalit.ceno.components.ceno.ClearButtonFeature
import ie.equalit.ceno.components.ceno.ClearToolbarAction
import ie.equalit.ceno.components.toolbar.ToolbarIntegration
import ie.equalit.ceno.databinding.FragmentBrowserBinding
import ie.equalit.ceno.downloads.DownloadService
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.ext.createSegment
import ie.equalit.ceno.ext.disableDynamicBehavior
import ie.equalit.ceno.ext.enableDynamicBehavior
import ie.equalit.ceno.ext.getPreferenceKey
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.pip.PictureInPictureIntegration
import ie.equalit.ceno.search.AwesomeBarWrapper
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.tabs.TabCounterView
import ie.equalit.ceno.ui.theme.ThemeManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import ie.equalit.ouinet.Ouinet
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.thumbnails.BrowserThumbnails
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.browser.toolbar.display.DisplayToolbar
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.app.links.AppLinksFeature
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.feature.awesomebar.provider.SearchSuggestionProvider
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.downloads.manager.FetchDownloadManager
import mozilla.components.feature.downloads.temporary.ShareDownloadFeature
import mozilla.components.feature.findinpage.view.FindInPageView
import mozilla.components.feature.prompts.PromptFeature
import mozilla.components.feature.session.FullScreenFeature
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.session.SwipeRefreshFeature
import mozilla.components.feature.sitepermissions.SitePermissionsFeature
import mozilla.components.feature.tabs.WindowFeature
import mozilla.components.feature.webauthn.WebAuthnFeature
import mozilla.components.lib.state.ext.consumeFlow
import mozilla.components.lib.state.ext.consumeFrom
import mozilla.components.support.base.feature.ActivityResultHandler
import mozilla.components.support.base.feature.PermissionsFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.android.content.res.resolveAttribute
import mozilla.components.support.ktx.android.view.enterImmersiveMode
import mozilla.components.support.ktx.android.view.exitImmersiveMode
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifAnyChanged
import org.json.JSONObject
import org.mozilla.geckoview.WebExtension
import mozilla.components.support.ktx.kotlin.tryGetHostFromUrl

/**
 * Base fragment extended by [BrowserFragment] and [ExternalAppBrowserFragment].
 * This class only contains shared code focused on the main browsing content.
 * UI code specific to the app or to custom tabs can be found in the subclasses.
 */
@Suppress("TooManyFunctions")
abstract class BaseBrowserFragment : Fragment(), UserInteractionHandler, ActivityResultHandler {
    private var ouinetStatus: Ouinet.RunningState = Ouinet.RunningState.Started
    var _binding: FragmentBrowserBinding? = null
    val binding get() = _binding!!

    private val sessionFeature = ViewBoundFeatureWrapper<SessionFeature>()
    private val toolbarIntegration = ViewBoundFeatureWrapper<ToolbarIntegration>()
    private val contextMenuIntegration = ViewBoundFeatureWrapper<ContextMenuIntegration>()
    private val downloadsFeature = ViewBoundFeatureWrapper<DownloadsFeature>()
    private val shareDownloadsFeature = ViewBoundFeatureWrapper<ShareDownloadFeature>()
    private val appLinksFeature = ViewBoundFeatureWrapper<AppLinksFeature>()
    private val promptsFeature = ViewBoundFeatureWrapper<PromptFeature>()
    private val fullScreenFeature = ViewBoundFeatureWrapper<FullScreenFeature>()
    private val findInPageIntegration = ViewBoundFeatureWrapper<FindInPageIntegration>()
    private val sitePermissionFeature = ViewBoundFeatureWrapper<SitePermissionsFeature>()
    private val pictureInPictureIntegration = ViewBoundFeatureWrapper<PictureInPictureIntegration>()
    private val swipeRefreshFeature = ViewBoundFeatureWrapper<SwipeRefreshFeature>()
    private val windowFeature = ViewBoundFeatureWrapper<WindowFeature>()
    private val webAuthnFeature = ViewBoundFeatureWrapper<WebAuthnFeature>()
    private var webExtensionActionPopupPanel: WebExtensionActionPopupPanel? = null
    private lateinit var runnable: Runnable
    private var handler = Handler(Looper.getMainLooper())


    private val backButtonHandler: List<ViewBoundFeatureWrapper<*>> = listOf(
        fullScreenFeature,
        findInPageIntegration,
        toolbarIntegration,
        sessionFeature,
    )

    private val activityResultHandler: List<ViewBoundFeatureWrapper<*>> = listOf(
        webAuthnFeature,
        promptsFeature,
    )

    private val thumbnailsFeature = ViewBoundFeatureWrapper<BrowserThumbnails>()

    private val awesomeBar: AwesomeBarWrapper
        get() = requireView().findViewById(R.id.awesomeBar)
    private val toolbar: BrowserToolbar
        get() = requireView().findViewById(R.id.toolbar)
    private val engineView: EngineView
        get() = requireView().findViewById<View>(R.id.engineView) as EngineView

    protected val sessionId: String?
        get() = arguments?.getString(SESSION_ID)

    protected var webAppToolbarShouldBeVisible = true

    private lateinit var browsingModeManager: BrowsingModeManager
    internal lateinit var themeManager: ThemeManager

    private var cachedSourceCounts: JSONObject? = null

    /* CENO: do not make onCreateView "final", needs to be overridden by CenoHomeFragment */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentBrowserBinding.inflate(inflater, container, false)
        container?.background = ContextCompat.getDrawable(requireContext(), R.drawable.blank_background)
        (activity as AppCompatActivity).supportActionBar!!.hide()
        return binding.root
    }

    /* CENO: not using Jetpack ComposeUI anywhere yet
    *  option was removed from Settings, will be added back if needed */
    //abstract val shouldUseComposeUI: Boolean

    @SuppressLint("ClickableViewAccessibility")
    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sessionFeature.set(
            feature = SessionFeature(
                requireComponents.core.store,
                requireComponents.useCases.sessionUseCases.goBack,
                binding.engineView,
                sessionId
            ),
            owner = this,
            view = view,
        )

        /* CENO: Add onTabUrlChanged listener to toolbar, to handle fragment transactions */
        toolbarIntegration.set(
            feature = ToolbarIntegration(
                requireContext(),
                requireActivity(),
                binding.toolbar,
                requireComponents.core.historyStorage,
                requireComponents.core.store,
                requireComponents.useCases.sessionUseCases,
                requireComponents.useCases.tabsUseCases,
                requireComponents.useCases.webAppUseCases,
                sessionId,
            ),
            owner = this,
            view = view,
        )

        contextMenuIntegration.set(
            feature = ContextMenuIntegration(
                requireContext(),
                parentFragmentManager,
                requireComponents.core.store,
                requireComponents.useCases.tabsUseCases,
                requireComponents.useCases.contextMenuUseCases,
                binding.engineView,
                view,
                sessionId,
            ),
            owner = this,
            view = view,
        )

        shareDownloadsFeature.set(
            ShareDownloadFeature(
                context = requireContext().applicationContext,
                httpClient = requireComponents.core.client,
                store = requireComponents.core.store,
                tabId = sessionId,
            ),
            owner = this,
            view = view,
        )

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
                    notificationsDelegate = requireComponents.notificationsDelegate,
                ),
                onNeedToRequestPermissions = { permissions ->
                    // The Fragment class wants us to use registerForActivityResult
                    @Suppress("DEPRECATION")
                    requestPermissions(permissions, REQUEST_CODE_DOWNLOAD_PERMISSIONS)
                },
            ),
            owner = this,
            view = view,
        )

        appLinksFeature.set(
            feature = AppLinksFeature(
                requireContext(),
                store = requireComponents.core.store,
                sessionId = sessionId,
                fragmentManager = parentFragmentManager,
                launchInApp = {
                    prefs.getBoolean(requireContext().getPreferenceKey(R.string.pref_key_launch_external_app), false)
                },
            ),
            owner = this,
            view = view,
        )

        promptsFeature.set(
            feature = PromptFeature(
                fragment = this,
                store = requireComponents.core.store,
                tabsUseCases = requireComponents.useCases.tabsUseCases,
                customTabId = sessionId,
                fileUploadsDirCleaner = requireComponents.core.fileUploadsDirCleaner,
                fragmentManager = parentFragmentManager,
                onNeedToRequestPermissions = { permissions ->
                    // The Fragment class wants us to use registerForActivityResult
                    @Suppress("DEPRECATION")
                    requestPermissions(permissions, REQUEST_CODE_PROMPT_PERMISSIONS)
                },
            ),
            owner = this,
            view = view,
        )

        windowFeature.set(
            feature = WindowFeature(requireComponents.core.store, requireComponents.useCases.tabsUseCases),
            owner = this,
            view = view,
        )

        fullScreenFeature.set(
            feature = FullScreenFeature(
                store = requireComponents.core.store,
                sessionUseCases = requireComponents.useCases.sessionUseCases,
                tabId = sessionId,
                viewportFitChanged = ::viewportFitChanged,
                fullScreenChanged = ::fullScreenChanged,
            ),
            owner = this,
            view = view,
        )

        findInPageIntegration.set(
            feature = FindInPageIntegration(
                requireComponents.core.store,
                sessionId,
                binding.findInPageBar as FindInPageView,
                binding.engineView
            ),
            owner = this,
            view = view,
        )

        sitePermissionFeature.set(
            feature = SitePermissionsFeature(
                context = requireContext(),
                fragmentManager = parentFragmentManager,
                sessionId = sessionId,
                storage = requireComponents.core.geckoSitePermissionsStorage,
                onNeedToRequestPermissions = { permissions ->
                    // The Fragment class wants us to use registerForActivityResult
                    @Suppress("DEPRECATION")
                    requestPermissions(permissions, REQUEST_CODE_APP_PERMISSIONS)
                },
                onShouldShowRequestPermissionRationale = { shouldShowRequestPermissionRationale(it) },
                store = requireComponents.core.store,
            ),
            owner = this,
            view = view,
        )

        pictureInPictureIntegration.set(
            feature = PictureInPictureIntegration(
                requireComponents.core.store,
                requireActivity(),
                sessionId,
            ),
            owner = this,
            view = view,
        )

        swipeRefreshFeature.set(
            feature = SwipeRefreshFeature(
                requireComponents.core.store,
                requireComponents.useCases.sessionUseCases.reload,
                binding.swipeRefresh,
            ),
            owner = this,
            view = view,
        )

        if (BuildConfig.MOZILLA_OFFICIAL) {
            webAuthnFeature.set(
                feature = WebAuthnFeature(
                    requireComponents.core.engine,
                    requireActivity(),
                    requireComponents.useCases.sessionUseCases.exitFullscreen::invoke,
                ) { requireComponents.core.store.state.selectedTabId },
                owner = this,
                view = view,
            )
        }

        /* CENO: Add purge button to toolbar */
        if (prefs.getBoolean(requireContext().getPreferenceKey(R.string.pref_key_clear_in_toolbar), true)) {
            val clearButtonFeature = ClearButtonFeature(
                requireContext(),
                prefs.getString(
                    requireContext().getPreferenceKey(R.string.pref_key_clear_behavior), "0")!!
                    .toInt()
            )
            binding.toolbar.addBrowserAction(
                ClearToolbarAction(
                    listener = {
                        clearButtonFeature.onClick()
                    },
                    context = themeManager.getContext()
                )
            )
        }

        /*
        // Disable scroll-to-hide until it is fixed, https://gitlab.com/censorship-no/ceno-browser/-/issues/144
        if (prefs.getBoolean(requireContext().getPreferenceKey(R.string.pref_key_toolbar_hide), false)) {
            binding.toolbar.enableDynamicBehavior(
                requireContext(),
                binding.engineView,
                prefs.getBoolean(
                    requireContext().getPreferenceKey(R.string.pref_key_toolbar_position),
                    false
                )
            )
        }
        else {
        */
        binding.toolbar.disableDynamicBehavior(
            binding.engineView,
            prefs.getBoolean(
                requireContext().getPreferenceKey(R.string.pref_key_toolbar_position),
                false
            )
        )
        //}


        AwesomeBarFeature(awesomeBar, toolbar, engineView).let {
            if (Settings.shouldShowSearchSuggestions(requireContext())) {
                it.addSearchProvider(
                    requireContext(),
                    requireComponents.core.store,
                    requireComponents.useCases.searchUseCases.defaultSearch,
                    fetchClient = requireComponents.core.client,
                    mode = SearchSuggestionProvider.Mode.MULTIPLE_SUGGESTIONS,
                    engine = requireComponents.core.engine,
                    limit = 5,
                    filterExactMatch = true
                )
            }
            it.addSessionProvider(
                resources,
                requireComponents.core.store,
                requireComponents.useCases.tabsUseCases.selectTab
            )
            it.addHistoryProvider(
                requireComponents.core.historyStorage,
                requireComponents.useCases.sessionUseCases.loadUrl
            )
            it.addClipboardProvider(requireContext(), requireComponents.useCases.sessionUseCases.loadUrl)
        }

        TabCounterView(
            toolbar = toolbar,
            sessionId = sessionId,
            store = requireComponents.core.store,
            showTabs = ::showTabs,
            lifecycleOwner = this,
            browsingModeManager = browsingModeManager,
            themeManager = themeManager
        )

        thumbnailsFeature.set(
            feature = BrowserThumbnails(
                requireContext(),
                engineView,
                requireComponents.core.store
            ),
            owner = this,
            view = view
        )

        observeTabSelection(requireComponents.core.store)

        // start runnable to continuously fetch new source counts
        runnable = Runnable {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    updateStats()
                    handler.postDelayed(runnable, SOURCES_COUNT_FETCH_DELAY)
                }
            }
        }

        handler.postDelayed(runnable, SOURCES_COUNT_FETCH_DELAY)

        /* CENO: not using Jetpack ComposeUI anywhere yet */
        /*
        val composeView = view.findViewById<ComposeView>(R.id.compose_view)
        if (shouldUseComposeUI) {
            composeView.visibility = View.VISIBLE
            composeView.setContent { BrowserToolbar() }

            val params = swipeRefresh.layoutParams as CoordinatorLayout.LayoutParams
            params.topMargin = resources.getDimensionPixelSize(R.dimen.browser_toolbar_height)
            swipeRefresh.layoutParams = params
        }
        */
    }

    override fun onStart() {
        super.onStart()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        /* CENO: Set toolbar appearance based on whether current tab is private or not
         * Doing this in onStart so it does not depend onViewCreated, which isn't run on returning to activity
         */

        binding.toolbar.private = themeManager.currentMode.isPersonal
        themeManager.applyTheme(binding.toolbar)

        binding.progressBarShield.background = ContextCompat.getDrawable(
            requireContext(),
            if (themeManager.currentMode.isPersonal) {
                R.color.fx_mobile_private_layer_color_1
            } else {
                requireContext().theme.resolveAttribute(
                    R.attr.toolbarBackground
                )
            }
        )

        val statusIcon = ContextCompat.getDrawable(themeManager.getContext(), R.drawable.ic_status)!!

        /* CENO: this is replaces the shield icon in the address bar
         * with the ceno logo, regardless of tracking protection state
         */
        binding.toolbar.display.icons = DisplayToolbar.Icons(
            emptyIcon = null,
            trackingProtectionTrackersBlocked = statusIcon,
            trackingProtectionNothingBlocked = statusIcon,
            trackingProtectionException = statusIcon,
            highlight = ContextCompat.getDrawable(requireContext(), R.drawable.mozac_dot_notification)!!,
        )
        val isToolbarPositionTop = prefs.getBoolean(
            requireContext().getPreferenceKey(R.string.pref_key_toolbar_position),
            false
        )

        val toolBarCoordinatorLayoutParams = binding.toolbar.layoutParams as CoordinatorLayout.LayoutParams

        val progressBarShieldLayoutParams = binding.progressBarShield.layoutParams as CoordinatorLayout.LayoutParams

        val sourcesProgressCoordinatorLayoutParams = binding.sourcesProgressBar.layoutParams as CoordinatorLayout.LayoutParams
        sourcesProgressCoordinatorLayoutParams.anchorId = binding.toolbar.id

        binding.toolbar.display.progressGravity = if(isToolbarPositionTop) {
            // reset layout_gravity of toolbar layout
            toolBarCoordinatorLayoutParams.gravity = Gravity.TOP

            // reset constraint of the sources progress bar
            sourcesProgressCoordinatorLayoutParams.anchorGravity = Gravity.BOTTOM

            // reset constraint of the progress bar shield
            progressBarShieldLayoutParams.anchorGravity = Gravity.TOP

            DisplayToolbar.Gravity.TOP
        }
        else {
            // reset layout_gravity of toolbar layout
            toolBarCoordinatorLayoutParams.gravity = Gravity.BOTTOM

            // reset constraint of the sources progress bar
            sourcesProgressCoordinatorLayoutParams.anchorGravity = Gravity.TOP

            // reset constraint of the progress bar shield
            progressBarShieldLayoutParams.anchorGravity = Gravity.BOTTOM

            DisplayToolbar.Gravity.BOTTOM
        }

        binding.sourcesProgressBar.requestLayout()
        binding.toolbar.layoutParams = toolBarCoordinatorLayoutParams

        updateOuinetStatus()
    }

    private fun updateOuinetStatus() {
        binding.root.consumeFrom(requireComponents.appStore, viewLifecycleOwner) {
            if (ouinetStatus != it.ouinetStatus) {
                ouinetStatus = it.ouinetStatus
                activity?.applicationContext?.let { ctx ->
                    val message = when (ouinetStatus) {
                        Ouinet.RunningState.Started -> {
                            ctx.getString(R.string.ceno_ouinet_connected)
                        }
                        Ouinet.RunningState.Stopped -> {
                            ctx.getString(R.string.ceno_ouinet_disconnected)
                        }
                        else -> {
                            ctx.getString(R.string.ceno_ouinet_connecting)
                        }
                    }
                    Toast.makeText(ctx, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun showWebExtensionPopupPanel() {
        val tab = requireContext().components.core.store.state.selectedTab!!

        webExtensionActionPopupPanel = WebExtensionActionPopupPanel(
                context = requireContext(),
                lifecycleOwner = this,
                tabUrl = tab.content.url,
                isConnectionSecure = tab.content.securityInfo.secure,
                cachedSourceCounts
        ).also { currentEtp -> currentEtp.show() }
    }

    private fun showTabs() {
        (activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).apply {
            navController.navigate(
                R.id.action_global_tabsTray
            )
        }
    }

    internal fun observeTabSelection(store: BrowserStore) {
        consumeFlow(store) { flow ->
            flow.ifAnyChanged {
                arrayOf(it.selectedTabId)
            }
                .mapNotNull {
                it.selectedTab
                }
                .collect {
                    handleTabSelected(it)
                }
        }
    }

    private fun handleTabSelected(selectedTab: TabSessionState) {
        if (!this.isRemoving ) {
            updateThemeForSession(selectedTab)
        }
    }

    private fun updateThemeForSession(selectedTab: TabSessionState) {
        val sessionMode = BrowsingMode.fromBoolean(selectedTab.content.private)
        if (sessionMode != browsingModeManager.mode) {
            browsingModeManager.mode = sessionMode
            //reload fragment
            val fragmentId = findNavController().currentDestination?.id
            findNavController().popBackStack(fragmentId!!,true)
            findNavController().navigate(fragmentId)
        }
    }

    private fun fullScreenChanged(enabled: Boolean) {
        if (enabled) {
            activity?.enterImmersiveMode()
            binding.toolbar.visibility = View.GONE
            binding.engineView.setDynamicToolbarMaxHeight(0)
        } else {
            activity?.exitImmersiveMode()
            binding.toolbar.visibility = View.VISIBLE
            binding.engineView.setDynamicToolbarMaxHeight(resources.getDimensionPixelSize(R.dimen.browser_toolbar_height))
        }
    }

    private fun viewportFitChanged(viewportFit: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            requireActivity().window.attributes.layoutInDisplayCutoutMode = viewportFit
        }
    }

    @CallSuper
    override fun onBackPressed(): Boolean {
        return backButtonHandler.any { it.onBackPressed() }
    }

    final override fun onHomePressed(): Boolean {
        return pictureInPictureIntegration.get()?.onHomePressed() ?: false
    }

    final override fun onPictureInPictureModeChanged(enabled: Boolean) {
        val session = requireComponents.core.store.state.selectedTab
        val fullScreenMode = session?.content?.fullScreen ?: false
        // If we're exiting PIP mode and we're in fullscreen mode, then we should exit fullscreen mode as well.
        if (!enabled && fullScreenMode) {
            onBackPressed()
            fullScreenChanged(false)
        }
    }

    final override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        val feature: PermissionsFeature? = when (requestCode) {
            REQUEST_CODE_DOWNLOAD_PERMISSIONS -> downloadsFeature.get()
            REQUEST_CODE_PROMPT_PERMISSIONS -> promptsFeature.get()
            REQUEST_CODE_APP_PERMISSIONS -> sitePermissionFeature.get()
            else -> null
        }
        feature?.onPermissionsResult(permissions, grantResults)
    }
    /**
     * Initializes themeManager and browsingModeManager
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeManager = (activity as BrowserActivity).themeManager
        browsingModeManager = (activity as BrowserActivity).browsingModeManager
    }

    companion object {
        private const val SESSION_ID = "session_id"
        private const val SOURCES_COUNT_FETCH_DELAY = 500L

        const val DIST_CACHE = "dist-cache"
        const val ORIGIN = "origin"
        const val INJECTOR = "injector"
        const val PROXY = "proxy"
//        const val LOCAL_CACHE = "local-cache"

        const val URL = "url"
    }

    override fun onActivityResult(requestCode: Int, data: Intent?, resultCode: Int): Boolean {
        Logger.info(
            "Fragment onActivityResult received with " +
                "requestCode: $requestCode, resultCode: $resultCode, data: $data",
        )

        return activityResultHandler.any { it.onActivityResult(requestCode, data, resultCode) }
    }

    private val portDelegate: WebExtension.PortDelegate = object : WebExtension.PortDelegate {
        override fun onPortMessage(
            message: Any, port: WebExtension.Port
        ) {
            if (context == null)
                return
            requireContext().components.core.store.state.selectedTab?.let { tab ->
                // the percentage progress for the webpage
                val webPageLoadProgress = tab.content.progress ?: 0

                // `message` returns as undefined sometimes. This check handles that
                if ((message as String?) != null && message.isNotEmpty() && message != "undefined") {
                    // set sources progress bar
                    val response = JSONObject(message)
                    // cache the values gotten; caching is done through SourceCountFetchListener interface
                    response.put(URL, tab.content.url.tryGetHostFromUrl())
                    cachedSourceCounts = response

                    // update sources BottomSheet if the reference is not null
                    webExtensionActionPopupPanel?.onCountsFetched(response)

                    val distCache = if(response.has(DIST_CACHE)) response.getString(DIST_CACHE).toFloat() else 0F
                    val origin = if(response.has(ORIGIN)) response.getString(ORIGIN).toFloat() else 0F
                    val injector = if(response.has(INJECTOR)) response.getString(INJECTOR).toFloat() else 0F
                    val proxy = if(response.has(PROXY)) response.getString(PROXY).toFloat() else 0F
//                val localCache = if(response.has(LOCAL_CACHE)) response.getString(LOCAL_CACHE).toFloat() else 0F

                    val sum = distCache + origin + injector + proxy

                    binding.sourcesProgressBar.removeAllViews()

                    // Add direct-from-website source
                    if(origin > 0) binding.sourcesProgressBar.addView(
                        requireContext().createSegment(
                            origin.div(sum).times(100).run {
                                if(webPageLoadProgress == 100) this else this.times((100 - webPageLoadProgress).div(100.0F))
                            },
                            R.color.ceno_sources_green
                        )
                    )

                    // Add via-ceno-network source
                    if((proxy + injector) > 0) binding.sourcesProgressBar.addView(
                        requireContext().createSegment(
                            (proxy + injector).div(sum).times(100).run {
                                if(webPageLoadProgress == 100) this else this.times((100 - webPageLoadProgress).div(100.0F))
                            },
                            R.color.ceno_sources_orange
                        )
                    )

                    // Add via Ceno cache
                    if(distCache > 0) binding.sourcesProgressBar.addView(
                        requireContext().createSegment(
                            distCache.div(sum).times(100).run {
                                if(webPageLoadProgress == 100) this else this.times((100 - webPageLoadProgress).div(100.0F))
                            },
                            R.color.ceno_sources_blue
                        )
                    )

                    // Add progressbar if the webpage hasn't loaded completely
                    if(webPageLoadProgress < 100) binding.sourcesProgressBar.addView(
                        requireContext().createSegment(
                            (100 - webPageLoadProgress).toFloat(),
                            R.color.ceno_grey_300
                        )
                    )

                } else {
                    // The main point of this check is to make the progressBar visible (color accent) when the sources haven't been fetched yet

                    // compare the URL key in `cachedSourceCounts` with the current tab's URL.
                    // The URL key in `cachedSourceCounts` is only set when sources have been successfully fetched at least once
                    if (cachedSourceCounts?.getString(URL) == context?.components?.core?.store?.state?.selectedTab!!.content.url.tryGetHostFromUrl()) {
                        binding.sourcesProgressBar.removeAllViews()
                        binding.sourcesProgressBar.addView(
                            requireContext().createSegment(
                                webPageLoadProgress.toFloat(),
                                R.color.accent
                            )
                        )
                    }
                }
            }
        }

        override fun onDisconnect(port: WebExtension.Port) {
            if (port === context?.components?.webExtensionPort?.mPort) {
                context?.components?.webExtensionPort?.mPort = null
            }
        }
    }

    private fun updateStats() {

        context?.components?.webExtensionPort?.mPort?.let {
            it.setDelegate(portDelegate)
            val message = JSONObject()
            message.put("requestSources", "true")
            it.postMessage(message)
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(runnable)
    }
}
