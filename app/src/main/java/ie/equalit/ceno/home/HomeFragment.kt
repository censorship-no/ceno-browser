package ie.equalit.ceno.home

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BaseBrowserFragment
import ie.equalit.ceno.browser.BrowserFragment
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.components.ceno.appstate.AppAction
import ie.equalit.ceno.databinding.FragmentHomeBinding
import ie.equalit.ceno.ext.ceno.sort
import ie.equalit.ceno.ext.cenoPreferences
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.ext.getPreferenceKey
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.home.announcements.RSSAnnouncementViewHolder
import ie.equalit.ceno.home.sessioncontrol.DefaultSessionControlController
import ie.equalit.ceno.home.sessioncontrol.SessionControlAdapter
import ie.equalit.ceno.home.sessioncontrol.SessionControlInteractor
import ie.equalit.ceno.home.sessioncontrol.SessionControlView
import ie.equalit.ceno.home.topsites.DefaultTopSitesView
import ie.equalit.ceno.settings.CenoSettings
import ie.equalit.ceno.settings.Settings
import ie.equalit.ceno.tooltip.CenoTooltip
import ie.equalit.ceno.tooltip.CenoTourStartOverlay
import ie.equalit.ceno.utils.CenoPreferences
import ie.equalit.ceno.utils.XMLParser
import ie.equalit.ouinet.Ouinet.RunningState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.concept.fetch.Request
import mozilla.components.concept.storage.FrecencyThresholdOption
import mozilla.components.feature.top.sites.TopSitesConfig
import mozilla.components.feature.top.sites.TopSitesFeature
import mozilla.components.feature.top.sites.TopSitesFrecencyConfig
import mozilla.components.feature.top.sites.TopSitesProviderConfig
import mozilla.components.lib.state.ext.consumeFrom
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import java.util.Locale

/**
 * A [BaseBrowserFragment] subclass that will display the custom CENO Browser homepage
 */
class HomeFragment : BaseHomeFragment() {

    private var tooltip: CenoTooltip? = null
    private var startTooltip:CenoTourStartOverlay? = null
    var adapter: SessionControlAdapter? = null

    private var _sessionControlInteractor: SessionControlInteractor? = null
    private val sessionControlInteractor: SessionControlInteractor
        get() = _sessionControlInteractor!!

    private var sessionControlView: SessionControlView? = null

    private val topSitesFeature = ViewBoundFeatureWrapper<TopSitesFeature>()

    private val scope = MainScope()

    private var ouinetStatus = RunningState.Started

    private var isNetworkStatusDialogVisible:Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val activity = activity as BrowserActivity
        val components = requireComponents
        themeManager = activity.themeManager
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        components.useCases.tabsUseCases.selectTab("")

//        components.appStore.dispatch(AppAction.ModeChange(themeManager.currentMode))

        /* Run coroutine to update the top site store in case it changed since last load */
        scope.launch {
            components.core.cenoTopSitesStorage.getTopSites(components.cenoPreferences.topSitesMaxLimit)
            components.appStore.dispatch(
                AppAction.Change(
                    topSites = components.core.cenoTopSitesStorage.cachedTopSites.sort()
                )
            )
        }

        topSitesFeature.set(
            feature = TopSitesFeature(
                view = DefaultTopSitesView(
                    settings = components.cenoPreferences
                ),
                storage = components.core.cenoTopSitesStorage,
                config = ::getTopSitesConfig
            ),
            owner = viewLifecycleOwner,
            view = binding.root
        )

        _sessionControlInteractor = SessionControlInteractor(
            controller = DefaultSessionControlController(
                activity = activity,
                preferences = components.cenoPreferences,
                appStore = components.appStore,
                viewLifecycleScope = viewLifecycleOwner.lifecycleScope,
                object : RSSAnnouncementViewHolder.RssAnnouncementSwipeListener {
                    override fun onSwipeCard(index: Int) {
                        /**
                         * Using minus(1) below because CenoAnnouncementItem is the second item in SessionControlView.kt
                         * AdapterItem.TopPlaceholderItem is the first item
                         * This should be updated if/when there's any change to the ordering in SessionControlView
                         */

                        // Using minus() below because CenoAnnouncementItem is the second item in SessionControlView.kt.
                        // AdapterItem.TopPlaceholderItem is the first item in SessionControlView.kt
                        // This should be updated if/when there's any change to the ordering in SessionControlView
                        val guid = Settings.getAnnouncementData(binding.root.context)?.items?.get(
                            index.minus(1)
                        )?.guid
                        guid?.let { Settings.addSwipedAnnouncementGuid(binding.root.context, it) }

                        updateSessionControlView()
                    }
                }
            )
        )

        sessionControlView = SessionControlView(
            binding.sessionControlRecyclerView,
            viewLifecycleOwner,
            sessionControlInteractor
        )


        (binding.homeAppBar.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
            topMargin = if (prefs.getBoolean(
                    requireContext().getPreferenceKey(R.string.pref_key_toolbar_position),
                    false
                )
            ) {
                resources.getDimensionPixelSize(R.dimen.browser_toolbar_height)
            } else {
                0
            }
        }

        container?.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.blank_background)
        (activity as AppCompatActivity).supportActionBar!!.hide()

        return binding.root
    }

    /** CENO: Copied from Fenix
     * Returns a [TopSitesConfig] which specifies how many top sites to display and whether or
     * not frequently visited sites should be displayed.
     */
    @VisibleForTesting
    internal fun getTopSitesConfig(): TopSitesConfig {
        val settings = requireContext().cenoPreferences()
        return TopSitesConfig(
            totalSites = settings.topSitesMaxLimit,
            frecencyConfig = TopSitesFrecencyConfig(
                FrecencyThresholdOption.SKIP_ONE_TIME_PAGES,
            ),
            providerConfig = TopSitesProviderConfig(
                showProviderTopSites = false,//settings.showContileFeature,
                maxThreshold = CenoPreferences.TOP_SITES_PROVIDER_MAX_THRESHOLD,
            )
        )
    }

    /** CENO: Copied from Fenix
     * The [SessionControlView] is forced to update with our current state when we call
     * [HomeFragment.onCreateView] in order to be able to draw everything at once with the current
     * data in our store. The [View.consumeFrom] coroutine dispatch
     * doesn't get run right away which means that we won't draw on the first layout pass.
     */
    private fun updateSessionControlView() {
        binding.root.consumeFrom(requireComponents.appStore, viewLifecycleOwner) {
            context?.let { context ->
                sessionControlView?.update(
                    it,
                    Settings.getAnnouncementData(context)?.items /* From local storage */
                )
                updateUI(it.mode)
                updateSearch(it.mode)
                if (ouinetStatus != it.ouinetStatus) {
                    updateOuinetStatus(context, it.ouinetStatus)
                }
            }
        }
        context?.let { context ->
            viewLifecycleOwner.lifecycleScope.launch {
                // Switch context to make network call
                withContext(Dispatchers.IO) {

                    // Get language code or fall back to 'en'
                    val languageCode = Locale.getDefault().language.ifEmpty { "en" }

                    var response = CenoSettings.webClientRequest(
                        context,
                        Request(Settings.getRSSAnnouncementUrl(context, languageCode))
                    )

                    // if the network call fails, try to load 'en' locale
                    if (response == null) {
                        response = CenoSettings.webClientRequest(
                            context,
                            Request(Settings.getRSSAnnouncementUrl(context, "en"))
                        )
                    }

                    response?.let { result ->
                        val rssResponse = XMLParser.parseRssXml(result)

                        // perform null-check and save announcement data in local
                        rssResponse?.let { Settings.saveAnnouncementData(context, it) }

                        // check for null and refresh homepage adapter if necessary
                        // Set announcement data from local since filtering happens there (i.e Settings.getAnnouncementData())
                        if (Settings.getAnnouncementData(context) != null) {
                            withContext(Dispatchers.Main) {
                                val state = context.components.appStore.state
                                sessionControlView?.update(
                                    state,
                                    Settings.getAnnouncementData(context)?.items
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateOuinetStatus(context: Context, status: RunningState) {
        ouinetStatus = status
        var message:String?
        when(ouinetStatus) {
            RunningState.Started -> {
                message = getString(R.string.ceno_ouinet_connected)
                //set connected icon
                binding.cenoNetworkStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ceno_connected_icon))
            }
            RunningState.Degraded -> {
                message = getString(R.string.ceno_ouinet_connecting)
                binding.cenoNetworkStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ceno_degraded_icon))
            }
            else -> {
                message = getString(R.string.ceno_ouinet_disconnected)
                //set disconnected icon
                binding.cenoNetworkStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ceno_disconnected_icon))
            }
        }
        message.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUI(mode: BrowsingMode) {
        context?.let {
            if (mode == BrowsingMode.Personal) {
                binding.homeAppBar.background =
                    ContextCompat.getDrawable(it, R.color.fx_mobile_private_layer_color_3)
                binding.sessionControlRecyclerView.background =
                    ContextCompat.getDrawable(it, R.color.fx_mobile_private_layer_color_3)
                binding.wordmark.drawable.setTint(ContextCompat.getColor(it, R.color.photonWhite))
            } else {
                binding.homeAppBar.background =
                    ContextCompat.getDrawable(it, R.color.ceno_home_background)
                binding.sessionControlRecyclerView.background =
                    ContextCompat.getDrawable(it, R.color.ceno_home_background)
                binding.wordmark.drawable.setTint(
                    ContextCompat.getColor(
                        it,
                        R.color.ceno_home_card_public_text
                    )
                )
            }
        }
        applyTheme()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        updateUI(themeManager.currentMode)
        binding.sessionControlRecyclerView.visibility = View.VISIBLE

        binding.sessionControlRecyclerView.itemAnimator = null

        binding.cenoNetworkStatusIcon.setOnClickListener {
            if(!isNetworkStatusDialogVisible) {
                CenoNetworkStatusDialog(requireContext(), this, ouinetStatus) {
                    isNetworkStatusDialogVisible = false
                }.getDialog().show()
                isNetworkStatusDialogVisible = true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateSessionControlView()
        if (requireComponents.ouinet.background.getState() == RunningState.Started.name)
            showTooltip()
    }

    override fun onPause() {
        super.onPause()
        startTooltip?.remove()
        tooltip?.dismiss()
    }

    fun showTooltip() {
        when (requireComponents.cenoPreferences.nextTooltip) {
            BEGIN_TOUR_TOOLTIP -> {
                startTooltip = CenoTourStartOverlay(this, false,
                    skipListener =
                    {
                        exitCenoTour()
                    },
                    startListener = {
                        requireComponents.cenoPreferences.nextTooltip += 1
                        showTooltip()
                    }
                )
                startTooltip?.show()
            }
            PUBLIC_PERSONAL_TOOLTIP -> {
                tooltip = CenoTooltip(this,
                    R.id.ceno_mode_item,
                    primaryText = getString(R.string.onboarding_public_or_personal_title),
                    secondaryText = getString(R.string.onboarding_public_personal_text),
                    promptFocal = RectanglePromptFocal().setCornerRadius(25f, 25f),
                    stopCaptureTouchOnFocal = true,
                    listener = { prompt: MaterialTapTargetPrompt, state: Int ->
                        when(state) {
                            MaterialTapTargetPrompt.STATE_REVEALED -> {
                                tooltip?.addButtons {
                                    exitCenoTour()
                                }
                            }
                        }
                    },
                    onNextButtonPressListener = ::goToNextTooltip
                )
                tooltip?.tooltip?.show()
            }
            SHORTCUTS_TOOLTIP -> {
                tooltip = CenoTooltip(this,
                    R.id.shortcuts_layout,
                    primaryText = getString(R.string.top_sites_title),
                    secondaryText = getString(R.string.tooltip_shortcuts_description),
                    promptFocal = RectanglePromptFocal().setCornerRadius(25f, 25f),
                    stopCaptureTouchOnFocal = true,
                    listener = { _: MaterialTapTargetPrompt, state: Int ->
                        when(state) {
                            MaterialTapTargetPrompt.STATE_REVEALED -> {
                                tooltip?.addButtons {
                                    exitCenoTour()
                                }
                            }
                        }
                    },
                    onNextButtonPressListener = ::goToNextTooltip

                )
                tooltip?.tooltip?.show()
            }
            TOOLBAR_TOOLTIP -> {
                tooltip = CenoTooltip(this,
                    R.id.mozac_browser_toolbar_origin_view,
                    primaryText = getString(R.string.tooltip_toolbar_title),
                    secondaryText = getString(R.string.tooltip_toolbar_description),
                    promptFocal = RectanglePromptFocal().setCornerRadius(25f, 25f),
                    buttonText = R.string.dialog_ok,
                    listener = { prompt: MaterialTapTargetPrompt, state: Int ->
                        when (state) {
                            MaterialTapTargetPrompt.STATE_REVEALED -> {
                                tooltip?.addButtons {
                                    exitCenoTour()
                                }
                            }
                            MaterialTapTargetPrompt.STATE_FOCAL_PRESSED -> {
                                tooltip?.dismiss()
                                requireComponents.cenoPreferences.nextTooltip += 1
                            }
                        }
                    },
                    onNextButtonPressListener = {
                        requireComponents.cenoPreferences.nextTooltip += 1
                        tooltip?.dismiss()
                    }
                )

                tooltip?.tooltip?.show()
            }
            BrowserFragment.TOOLTIP_PERMISSION -> {
                startTooltip = CenoTourStartOverlay(
                    this,
                    true,
                    startListener = {
                        requireComponents.cenoPreferences.nextTooltip = -1
                        askForPermissions()
                    },
                    skipListener = {}
                )
                startTooltip?.show()
            }
        }
    }

    private fun goToNextTooltip(view:View) {
        requireComponents.cenoPreferences.nextTooltip += 1
        tooltip?.dismiss()
        showTooltip()
    }
    private fun exitCenoTour() {
        //exit tour
        tooltip?.dismiss()
        if (requireComponents.permissionHandler.shouldShowPermissionsTooltip()) {
            requireComponents.cenoPreferences.nextTooltip = BrowserFragment.TOOLTIP_PERMISSION
            showTooltip()
        }
        else {
            requireComponents.cenoPreferences.nextTooltip = -1
        }
        Settings.setShowOnboarding(requireContext(), false)
    }

    private fun askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            /* This is Android 13 or later, ask for permission POST_NOTIFICATIONS */
            requireComponents.permissionHandler.requestPostNotificationsPermission(this)
        } else {
            /* This is NOT Android 13, just ask to disable battery optimization */
            requireComponents.permissionHandler.requestBatteryOptimizationsOff(requireActivity())
        }
    }

    companion object {
        const val PUBLIC_PERSONAL_TOOLTIP = 2
        const val SHORTCUTS_TOOLTIP = 3
        const val TOOLBAR_TOOLTIP = 4
        const val BEGIN_TOUR_TOOLTIP = 1

        const val TAG = "HOMEPAGE"
    }
}