package ie.equalit.ceno.home.sessioncontrol
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.home.HomepageCardType
import ie.equalit.ceno.home.ouicrawl.OuicrawlSite
import mozilla.components.feature.top.sites.TopSite

/**
 * Interface for tab related actions in the [SessionControlInteractor].
 */
/*
interface TabSessionInteractor {
    /**
     * Shows the Private Browsing Learn More page in a new tab. Called when a user clicks on the
     * "Common myths about private browsing" link in private mode.
     */
    fun onPrivateBrowsingLearnMoreClicked()

    /**
     * Called when a user clicks on the Private Mode button on the homescreen.
     */
    fun onPrivateModeButtonClicked(newMode: BrowsingMode, userHasBeenOnboarded: Boolean)

    /**
     * Called when there is an update to the session state and updated metrics need to be reported
     *
     * * @param state The state the homepage from which to report desired metrics.
     */
    fun reportSessionMetrics(state: AppState)
}
 */


/**
 * Interface for onboarding related actions in the [SessionControlInteractor].
 */
/*
interface OnboardingInteractor {
    /**
     * Hides the onboarding and navigates to Search. Called when a user clicks on the "Start Browsing" button.
     */
    fun onStartBrowsingClicked()

    /**
     * Opens a custom tab to privacy notice url. Called when a user clicks on the "read our privacy notice" button.
     */
    fun onReadPrivacyNoticeClicked()

    /**
     * Show the onboarding dialog to onboard users about recentTabs,recentBookmarks,
     * historyMetadata and pocketArticles sections.
     */
    fun showOnboardingDialog()
}
 */

/*
interface CustomizeHomeIteractor {
    /**
     * Opens the customize home settings page.
     */
    fun openCustomizeHomePage()
}
 */


interface HomePageInteractor {
    fun onRemoveCard(homepageCardType: HomepageCardType)

    fun onCardSwipe(homepageCardType: HomepageCardType)

    fun onAnnouncementCardSwiped(index: Int)

    fun onClicked(homepageCardType: HomepageCardType, mode: BrowsingMode)

    fun onMenuItemClicked(homepageCardType: HomepageCardType)

    fun onUrlClicked(homepageCardType: HomepageCardType, url: String)
}

/**
 * Interface for top site related actions in the [SessionControlInteractor].
 */
interface TopSiteInteractor {
    /**
     * Opens the given top site in private mode. Called when an user clicks on the "Open in private
     * tab" top site menu item.
     *
     * @param topSite The top site that will be open in private mode.
     */
    fun onOpenInPrivateTabClicked(topSite: TopSite)

    /**
     * Opens a dialog to rename the given top site. Called when an user clicks on the "Rename" top site menu item.
     *
     * @param topSite The top site that will be renamed.
     */
    fun onRenameTopSiteClicked(topSite: TopSite)

    /**
     * Removes the given top site. Called when an user clicks on the "Remove" top site menu item.
     *
     * @param topSite The top site that will be removed.
     */
    fun onRemoveTopSiteClicked(topSite: TopSite)

    /**
     * Selects the given top site. Called when a user clicks on a top site.
     *
     * @param topSite The top site that was selected.
     * @param position The position of the top site.
     */
    fun onSelectTopSite(topSite: TopSite, position: Int)

    /**
     * Called when top site menu is opened.
     */
    fun onTopSiteMenuOpened()
}

interface OuicrawlSiteInteractor {
    fun onOuicrawlSiteMenuOpened()
    fun onOpenInPersonalTabClicked(ouicrawlSite: OuicrawlSite)
    fun onShortcuts(ouicrawlSite: OuicrawlSite, isTopSite:Boolean)
    fun onOuicrawlSiteClicked(siteURL: String)
}

/**
 * Interactor for the Home screen. Provides implementations for the CollectionInteractor,
 * OnboardingInteractor, TopSiteInteractor, TabSessionInteractor, ToolbarInteractor,
 * ExperimentCardInteractor, RecentTabInteractor, RecentBookmarksInteractor
 * and others.
 */
@SuppressWarnings("TooManyFunctions")
class SessionControlInteractor(
    private val controller: SessionControlController,
) :
    //OnboardingInteractor,
    TopSiteInteractor,
    HomePageInteractor,
        OuicrawlSiteInteractor
    {
        override fun onOpenInPrivateTabClicked(topSite: TopSite) {
            controller.handleOpenInPrivateTabClicked(topSite.url)
        }

        override fun onRenameTopSiteClicked(topSite: TopSite) {
            controller.handleRenameTopSiteClicked(topSite)
        }

        override fun onRemoveTopSiteClicked(topSite: TopSite) {
            controller.handleRemoveTopSiteClicked(topSite)
        }

        override fun onSelectTopSite(topSite: TopSite, position: Int) {
            controller.handleSelectTopSite(topSite, position)
        }

        override fun onTopSiteMenuOpened() {
            controller.handleMenuOpened()
        }

        override fun onRemoveCard(homepageCardType: HomepageCardType) {
            controller.handleRemoveCard(homepageCardType)
        }

        override fun onCardSwipe(homepageCardType: HomepageCardType) {
            controller.handleRemoveCard(homepageCardType)
        }

        override fun onAnnouncementCardSwiped(index: Int) {
            controller.handleRemoveAnnouncementCard(index)
        }

        override fun onClicked(homepageCardType: HomepageCardType, mode: BrowsingMode) {
            controller.handleCardClicked(homepageCardType, mode)
        }

        override fun onMenuItemClicked(homepageCardType: HomepageCardType) {
            controller.handleMenuItemClicked(homepageCardType)
        }

        override fun onUrlClicked(homepageCardType: HomepageCardType, url: String) {
            controller.handleUrlClicked(homepageCardType, url)
        }

        override fun onOuicrawlSiteMenuOpened() {
            controller.handleMenuOpened()
        }

        override fun onOpenInPersonalTabClicked(ouicrawlSite: OuicrawlSite) {
            controller.handleOpenInPrivateTabClicked(ouicrawlSite.SiteURL)
        }

        override fun onShortcuts(ouicrawlSite: OuicrawlSite, isTopSite: Boolean) {
            controller.handleAddToShortcuts(ouicrawlSite, isTopSite)
        }

        override fun onOuicrawlSiteClicked(siteURL: String) {
            controller.handleUrlClicked(HomepageCardType.OUICRAWLED_SITE_CARD, siteURL)
        }
    }
