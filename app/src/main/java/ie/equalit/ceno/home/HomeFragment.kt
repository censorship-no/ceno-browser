package ie.equalit.ceno.home

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
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
import ie.equalit.ceno.tooltip.ToolbarTooltip
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
import java.util.Locale

/**
 * A [BaseBrowserFragment] subclass that will display the custom CENO Browser homepage
 */
class HomeFragment : BaseHomeFragment() {

    private lateinit var urlTooltip: MaterialTapTargetPrompt.Builder
    var adapter: SessionControlAdapter? = null

    private var _sessionControlInteractor: SessionControlInteractor? = null
    private val sessionControlInteractor: SessionControlInteractor
        get() = _sessionControlInteractor!!

    private var sessionControlView: SessionControlView? = null

    private val topSitesFeature = ViewBoundFeatureWrapper<TopSitesFeature>()

    private val scope = MainScope()

    private var ouinetStatus = RunningState.Started

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val activity = activity as BrowserActivity
        val components = requireComponents
        themeManager = activity.themeManager
        _binding = FragmentHomeBinding.inflate(inflater, container, false);

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
                object: RSSAnnouncementViewHolder.RssAnnouncementSwipeListener {
                    override fun onSwipeCard(index: Int) {
                        /**
                         * Using minus(1) below because CenoAnnouncementItem is the second item in SessionControlView.kt
                         * AdapterItem.TopPlaceholderItem is the first item
                         * This should be updated if/when there's any change to the ordering in SessionControlView
                         */

                        // Using minus() below because CenoAnnouncementItem is the second item in SessionControlView.kt.
                        // AdapterItem.TopPlaceholderItem is the first item in SessionControlView.kt
                        // This should be updated if/when there's any change to the ordering in SessionControlView
                        val guid = Settings.getAnnouncementData(binding.root.context)?.items?.get(index.minus(1))?.guid
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
            topMargin = if(prefs.getBoolean(requireContext().getPreferenceKey(R.string.pref_key_toolbar_position), false)) {
                    resources.getDimensionPixelSize(R.dimen.browser_toolbar_height)
                }
                else {
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
                        Request(CenoSettings.getRSSAnnouncementUrl(languageCode))
                    )

                    // if the network call fails, try to load 'en' locale
                    if(response == null) {
                        response = CenoSettings.webClientRequest(
                            context,
                            Request(CenoSettings.getRSSAnnouncementUrl("en"))
                        )
                    }

                    response?.let { result ->
                        val rssResponse = XMLParser.parseRssXml(result)

                        // perform null-check and save announcement data in local
                        rssResponse?.let { Settings.saveAnnouncementData(context, it) }

                        // check for null and refresh homepage adapter if necessary
                        // Set announcement data from local since filtering happens there (i.e Settings.getAnnouncementData())
                        if(Settings.getAnnouncementData(context) != null) {
                            withContext(Dispatchers.Main) {
                                val state = context.components.appStore.state
                                sessionControlView?.update(state, Settings.getAnnouncementData(context)?.items)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateOuinetStatus(context: Context, status: RunningState) {
        ouinetStatus = status
        val message = if (ouinetStatus == RunningState.Started) {
            getString(R.string.ceno_ouinet_connected)
        } else if (ouinetStatus == RunningState.Stopped){
            getString(R.string.ceno_ouinet_disconnected)
        } else {
            getString(R.string.ceno_ouinet_connecting)
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun updateUI(mode: BrowsingMode) {
        context?.let {
            if (mode == BrowsingMode.Personal) {
                binding.homeAppBar.background = ContextCompat.getDrawable(it, R.color.fx_mobile_private_layer_color_3)
                binding.sessionControlRecyclerView.background = ContextCompat.getDrawable(it, R.color.fx_mobile_private_layer_color_3)
                binding.wordmark.drawable.setTint(ContextCompat.getColor(it, R.color.photonWhite))
            } else {
                binding.homeAppBar.background = ContextCompat.getDrawable(it, R.color.ceno_home_background)
                binding.sessionControlRecyclerView.background = ContextCompat.getDrawable(it, R.color.ceno_home_background)
                binding.wordmark.drawable.setTint(ContextCompat.getColor(it, R.color.ceno_home_card_public_text))
            }
        }
        applyTheme()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        updateUI(themeManager.currentMode)
        binding.sessionControlRecyclerView.visibility = View.VISIBLE

        binding.sessionControlRecyclerView.itemAnimator = null

        urlTooltip = ToolbarTooltip(this, R.id.mozac_browser_toolbar_origin_view) {
            prompt: MaterialTapTargetPrompt, state: Int ->
            when(state) {
                MaterialTapTargetPrompt.STATE_DISMISSING -> {
                    //Show dialog - do you want to skip the tour?
                    getSkipTourDialog().show()
                }
                MaterialTapTargetPrompt.STATE_FOCAL_PRESSED -> {
                    requireComponents.cenoPreferences.nextTooltip =+ 1
                }
            }

        }.tooltip
    }

    private fun getSkipTourDialog(): AlertDialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Skip?")
            .setMessage("Do you want to skip the Ceno tour?")
            .setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                requireComponents.cenoPreferences.nextTooltip =+ 1
            }
            .setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
                //show tooltip again
                urlTooltip.show()
            }
            .create()

    }

    override fun onStart() {
        super.onStart()
        updateSessionControlView()
        if (requireComponents.cenoPreferences.nextTooltip == TOOLBAR_TOOLTIP) {
            urlTooltip.show()
        }
    }

    companion object {
        const val PUBLIC_PERSONAL_TOOLTIP = 0
        const val TOOLBAR_TOOLTIP = 1


    }
}