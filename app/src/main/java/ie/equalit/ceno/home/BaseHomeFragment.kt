/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import ie.equalit.ceno.AppPermissionCodes.REQUEST_CODE_DOWNLOAD_PERMISSIONS
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.BuildConfig
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowserFragment
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.components.ceno.ClearButtonFeature
import ie.equalit.ceno.components.ceno.ClearToolbarAction
import ie.equalit.ceno.components.toolbar.ToolbarIntegration
import ie.equalit.ceno.databinding.FragmentHomeBinding
import ie.equalit.ceno.downloads.DownloadService
import ie.equalit.ceno.ext.*
import ie.equalit.ceno.search.AwesomeBarWrapper
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.tabs.TabCounterView
import ie.equalit.ceno.ui.theme.ThemeManager
import mozilla.components.browser.thumbnails.BrowserThumbnails
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.browser.toolbar.display.DisplayToolbar
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.feature.awesomebar.provider.SearchSuggestionProvider
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.downloads.manager.FetchDownloadManager
import mozilla.components.feature.downloads.temporary.ShareDownloadFeature
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.tabs.WindowFeature
import mozilla.components.feature.webauthn.WebAuthnFeature
import mozilla.components.support.base.feature.ActivityResultHandler
import mozilla.components.support.base.feature.PermissionsFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.base.log.logger.Logger

/**
 * Base fragment extended by [BrowserFragment] and [ExternalAppBrowserFragment].
 * This class only contains shared code focused on the main browsing content.
 * UI code specific to the app or to custom tabs can be found in the subclasses.
 */
@Suppress("TooManyFunctions")
abstract class BaseHomeFragment : Fragment(), UserInteractionHandler, ActivityResultHandler {
    private lateinit var tabConterView: TabCounterView
    var _binding: FragmentHomeBinding? = null
    val binding get() = _binding!!

    lateinit var themeManager: ThemeManager

    private val sessionFeature = ViewBoundFeatureWrapper<SessionFeature>()
    private val toolbarIntegration = ViewBoundFeatureWrapper<ToolbarIntegration>()
    private val downloadsFeature = ViewBoundFeatureWrapper<DownloadsFeature>()
    private val shareDownloadsFeature = ViewBoundFeatureWrapper<ShareDownloadFeature>()
    private val windowFeature = ViewBoundFeatureWrapper<WindowFeature>()
    private val webAuthnFeature = ViewBoundFeatureWrapper<WebAuthnFeature>()

    private val backButtonHandler: List<ViewBoundFeatureWrapper<*>> = listOf(
        toolbarIntegration,
        sessionFeature,
    )

    private val activityResultHandler: List<ViewBoundFeatureWrapper<*>> = listOf(
        webAuthnFeature,
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

    lateinit var clearCenoAction:ClearToolbarAction

    /* CENO: do not make onCreateView "final", needs to be overridden by HomeFragment */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        themeManager = (activity as BrowserActivity).themeManager
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        container?.background = ContextCompat.getDrawable(requireContext(), R.drawable.blank_background)
        (activity as AppCompatActivity).supportActionBar!!.hide()
        return binding.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        sessionFeature.set(
            feature = SessionFeature(
                requireComponents.core.store,
                requireComponents.useCases.sessionUseCases.goBack,
                requireComponents.useCases.sessionUseCases.goForward,
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

        windowFeature.set(
            feature = WindowFeature(requireComponents.core.store, requireComponents.useCases.tabsUseCases),
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
        val clearButtonFeature = ClearButtonFeature(
            requireContext(),
            prefs.getString(
                requireContext().getPreferenceKey(R.string.pref_key_clear_behavior), "0")!!
                .toInt()
        )
        clearCenoAction = ClearToolbarAction(
            listener = {
                clearButtonFeature.onClick()
            },
            context = themeManager.getContext()
        )

        if (prefs.getBoolean(requireContext().getPreferenceKey(R.string.pref_key_clear_in_toolbar), true)) {
            binding.toolbar.addBrowserAction(clearCenoAction)
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
                    searchUseCase = if (themeManager.currentMode.isPersonal) {
                        requireComponents.useCases.searchUseCases.newPrivateTabSearch
                    } else {
                        requireComponents.useCases.searchUseCases.newTabSearch
                    },
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
                requireComponents.useCases.customLoadUrlUseCase
            )
            it.addClipboardProvider(requireContext(), requireComponents.useCases.sessionUseCases.loadUrl)
        }

        /* Redefine onStopListener to open browser fragment in addition to closing toolbar */
        awesomeBar.setOnStopListener {
            toolbar.displayMode()
            (activity as BrowserActivity).openToBrowser(private = themeManager.currentMode.isPersonal, newTab = true)
        }

        tabConterView = TabCounterView(
            toolbar = binding.toolbar,
            sessionId = sessionId,
            store = requireComponents.core.store,
            showTabs = ::showTabs,
            lifecycleOwner = this,
            browsingModeManager = (activity as BrowserActivity).browsingModeManager,
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
    }

    override fun onStart() {
        super.onStart()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        themeManager.applyTheme(binding.toolbar)

        val isToolbarPositionTop = prefs.getBoolean(
            requireContext().getPreferenceKey(R.string.pref_key_toolbar_position),
            false
        )
        binding.toolbar.display.progressGravity = if(isToolbarPositionTop) {
            DisplayToolbar.Gravity.BOTTOM
        }
        else {
            DisplayToolbar.Gravity.TOP
        }
    }

    private fun showTabs() {
        (activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).apply {
            navController.navigate(
                R.id.action_global_tabsTray
            )
        }
    }

    fun applyTheme() {
        //modify clear ceno button color
        if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(requireContext().getPreferenceKey(R.string.pref_key_clear_in_toolbar), true)) {
            clearCenoAction.updateIconColor(themeManager.getContext())
        }
        //modify tab counter icon color
        tabConterView.update()
        //modify rest of the toolbar
        themeManager.applyTheme(binding.toolbar)
    }

    internal fun updateSearch(mode: BrowsingMode) {
        if (Settings.shouldShowSearchSuggestions(requireContext())) {
            awesomeBar.removeProviders(awesomeBar.searchSuggestionProvider)
            awesomeBar.addProviders(
                SearchSuggestionProvider(
                    requireContext(),
                    requireComponents.core.store,
                    searchUseCase = if (themeManager.currentMode.isPersonal) {
                        requireComponents.useCases.searchUseCases.newPrivateTabSearch
                    } else {
                        requireComponents.useCases.searchUseCases.newTabSearch
                    },
                    fetchClient = requireComponents.core.client,
                    limit = 5,
                    mode = SearchSuggestionProvider.Mode.MULTIPLE_SUGGESTIONS,
                    engine = requireComponents.core.engine,
                    filterExactMatch = true,
                )
            )
        }
    }

    @CallSuper
    override fun onBackPressed(): Boolean {
        return backButtonHandler.any { it.onBackPressed() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        val feature: PermissionsFeature? = when (requestCode) {
            REQUEST_CODE_DOWNLOAD_PERMISSIONS -> downloadsFeature.get()
            else -> null
        }
        feature?.onPermissionsResult(permissions, grantResults)
    }

    companion object {
        private const val SESSION_ID = "session_id"
    }

    override fun onActivityResult(requestCode: Int, data: Intent?, resultCode: Int): Boolean {
        Logger.info(
            "Fragment onActivityResult received with " +
                "requestCode: $requestCode, resultCode: $resultCode, data: $data",
        )

        return activityResultHandler.any { it.onActivityResult(requestCode, data, resultCode) }
    }
}
