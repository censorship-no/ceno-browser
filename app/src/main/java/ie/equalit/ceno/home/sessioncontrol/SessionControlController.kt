/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.home.sessioncontrol

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
/*
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.feature.tabs.TabsUseCases
 */
import mozilla.components.feature.top.sites.TopSite
import mozilla.components.support.ktx.android.view.showKeyboard
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.components.ceno.AppStore
import ie.equalit.ceno.components.ceno.appstate.AppAction
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.home.HomepageCardType
import ie.equalit.ceno.home.announcements.RSSAnnouncementViewHolder
import ie.equalit.ceno.home.ouicrawl.OuicrawlSite
import ie.equalit.ceno.utils.CenoPreferences

/**
 * [HomeFragment] controller. An interface that handles the view manipulation of the Tabs triggered
 * by the Interactor.
 */
@Suppress("TooManyFunctions")
interface SessionControlController {
    /**
     * @see [TopSiteInteractor.onRenameTopSiteClicked]
     */
    fun handleRenameTopSiteClicked(topSite: TopSite)

    /**
     * @see [TopSiteInteractor.onRemoveTopSiteClicked]
     */
    fun handleRemoveTopSiteClicked(topSite: TopSite)

    /**
     * @see [TopSiteInteractor.onSelectTopSite]
     */
    fun handleSelectTopSite(topSite: TopSite, position: Int)

    /**
     * @see [TopSiteInteractor.onOpenInPrivateTabClicked]
     */
    fun handleOpenInPrivateTabClicked(url: String)

    /**
     * @see [TopSiteInteractor.onSettingsClicked]
     */
    fun handleTopSiteSettingsClicked()

    /**
     * @see [CollectionInteractor.onCollectionMenuOpened] and [TopSiteInteractor.onTopSiteMenuOpened]
     */
    fun handleMenuOpened()

    fun handleCardClicked(homepageCardType: HomepageCardType, mode: BrowsingMode)

    fun handleMenuItemClicked(homepageCardType: HomepageCardType)

    fun handleRemoveCard(homepageCardType: HomepageCardType)

    fun handleRemoveAnnouncementCard(index: Int)

    fun handleUrlClicked(homepageCardType: HomepageCardType, url: String)

    fun handleAddToShortcuts(ouicrawlSite: OuicrawlSite, isTopSite: Boolean)
}

@Suppress("TooManyFunctions", "LargeClass", "LongParameterList")
class DefaultSessionControlController(
    private val activity: BrowserActivity,
    private val preferences: CenoPreferences,
    private val appStore: AppStore,
    /*
    private val engine: Engine,
    private val store: BrowserStore,
    private val addTabUseCase: TabsUseCases.AddNewTabUseCase,
     */
    private val viewLifecycleScope: CoroutineScope,
    private val rssAnnouncementSwipeListener: RSSAnnouncementViewHolder.RssAnnouncementSwipeListener?
) : SessionControlController {

    override fun handleMenuOpened() {
        //dismissSearchDialogIfDisplayed()
    }

    @SuppressLint("InflateParams")
    override fun handleRenameTopSiteClicked(topSite: TopSite) {
        activity.let {
            val customLayout =
                LayoutInflater.from(it).inflate(R.layout.top_sites_rename_dialog, null)
            val topSiteLabelEditText: EditText =
                customLayout.findViewById(R.id.top_site_title)
            topSiteLabelEditText.setText(topSite.title)

            AlertDialog.Builder(it).apply {
                setTitle(R.string.rename_top_site)
                setView(customLayout)
                setPositiveButton(R.string.dialog_ok) { dialog, _ ->
                    viewLifecycleScope.launch(Dispatchers.IO) {
                        with(activity.components.useCases.cenoTopSitesUseCase) {
                            updateTopSites(
                                topSite,
                                topSiteLabelEditText.text.toString(),
                                topSite.url
                            )
                        }
                    }
                    dialog.dismiss()
                }
                setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                    dialog.cancel()
                }
            }.show().also {
                topSiteLabelEditText.setSelection(0, topSiteLabelEditText.text.length)
                topSiteLabelEditText.showKeyboard()
            }
        }
    }

    override fun handleRemoveTopSiteClicked(topSite: TopSite) {
        viewLifecycleScope.launch(Dispatchers.IO) {
            with(activity.components.useCases.cenoTopSitesUseCase) {
                removeTopSites(topSite)
            }
        }
    }

    override fun handleSelectTopSite(topSite: TopSite, position: Int) {
        //dismissSearchDialogIfDisplayed()
        /*
        when (topSite.url) {
            SupportUtils.GOOGLE_URL -> TopSites.openGoogleSearchAttribution.record(NoExtras())
            SupportUtils.BAIDU_URL -> TopSites.openBaiduSearchAttribution.record(NoExtras())
            SupportUtils.POCKET_TRENDING_URL -> Pocket.pocketTopSiteClicked.record(NoExtras())
        }

        val availableEngines: List<SearchEngine> = getAvailableSearchEngines()
        val searchAccessPoint = MetricsUtils.Source.TOPSITE

        availableEngines.firstOrNull { engine ->
            engine.resultUrls.firstOrNull { it.contains(topSite.url) } != null
        }?.let { searchEngine ->
            MetricsUtils.recordSearchMetrics(
                searchEngine,
                searchEngine == store.state.search.selectedOrDefaultSearchEngine,
                searchAccessPoint
            )
        }
         */

        /*
        val tabId = addTabUseCase.invoke(
            url = appendSearchAttributionToUrlIfNeeded(topSite.url),
            selectTab = true,
            startLoading = true
        )
         */

        /*
        if (settings.openNextTabInDesktopMode) {
            activity.handleRequestDesktopMode(tabId)
        }
         */
        activity.openToBrowser(topSite.url, newTab = true)
    }

    override fun handleOpenInPrivateTabClicked(url: String) {
        with(activity) {
            openToBrowser(
                url = url,
                newTab = true,
                private = true
            )
        }
    }

    override fun handleTopSiteSettingsClicked() {
        /*
        navController.nav(
            R.id.homeFragment,
            HomeFragmentDirections.actionGlobalHomeSettingsFragment()
        )
         */
    }

    override fun handleCardClicked(homepageCardType: HomepageCardType, mode: BrowsingMode) {
        if (homepageCardType == HomepageCardType.PERSONAL_MODE_CARD) {
            activity.apply{
                openToBrowser(getString(R.string.ceno_support_link_url), newTab = true, private = true)
            }
        }
        if (homepageCardType == HomepageCardType.MODE_MESSAGE_CARD) {
            activity.switchBrowsingModeHome(mode)
        }
        if (homepageCardType == HomepageCardType.BASIC_MESSAGE_CARD) {
            activity.apply{
                openSettings()
            }
        }
    }

    override fun handleMenuItemClicked(homepageCardType: HomepageCardType) {
        if (homepageCardType == HomepageCardType.MODE_MESSAGE_CARD) {
            activity.apply{
                browsingModeManager.mode = BrowsingMode.Personal
            }
        }
        if (homepageCardType == HomepageCardType.BASIC_MESSAGE_CARD) {
            activity.apply{
                openToBrowser(getString(R.string.website_button_link), newTab = true)
            }
        }
    }

    override fun handleRemoveCard(homepageCardType: HomepageCardType) {
        if (homepageCardType == HomepageCardType.BASIC_MESSAGE_CARD) {
            preferences.showBridgeAnnouncementCard = false
            appStore.dispatch(AppAction.BridgeCardChange(false))
        }
    }

    override fun handleRemoveAnnouncementCard(index: Int) {
        rssAnnouncementSwipeListener?.onSwipeCard(index)
    }

    override fun handleUrlClicked(homepageCardType: HomepageCardType, url: String) {
        activity.openToBrowser(url, newTab = true)
    }

    override fun handleAddToShortcuts(ouicrawlSite: OuicrawlSite, isTopSite: Boolean) {
        activity.lifecycleScope.launch {
            if (isTopSite) {
                val removedTopSite: TopSite? =
                    activity.components.core.cenoPinnedSiteStorage
                        .getPinnedSites()
                        .find { it.url == ("https://${ouicrawlSite.SiteURL}/") }
                if (removedTopSite != null) {
                    with(activity.components.useCases.cenoTopSitesUseCase) {
                        removeTopSites(removedTopSite)
                    }
                }
            } else {
                val numPinnedSites = activity.components.core.cenoTopSitesStorage.cachedTopSites
                    .filter { it is TopSite.Default || it is TopSite.Pinned }.size

                if (numPinnedSites >= activity.components.cenoPreferences.topSitesMaxLimit) {
                    AlertDialog.Builder(activity).apply {
                        setTitle(R.string.shortcut_max_limit_title)
                        setMessage(R.string.shortcut_max_limit_content)
                        setPositiveButton(R.string.top_sites_max_limit_confirmation_button) { dialog, _ ->
                            dialog.dismiss()
                        }
                        create()
                    }.show()
                } else {
                    with(activity.components.useCases.cenoTopSitesUseCase) {
                        addPinnedSites(ouicrawlSite.SiteName, "https://${ouicrawlSite.SiteURL}/")
                    }
                }
            }
        }

    }
}
