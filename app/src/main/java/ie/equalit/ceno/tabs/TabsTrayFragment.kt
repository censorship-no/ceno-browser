/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.tabs

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ie.equalit.ceno.BrowserActivity
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.tabstray.DefaultTabViewHolder
import mozilla.components.browser.tabstray.TabsAdapter
import mozilla.components.browser.tabstray.TabsTray
import mozilla.components.browser.tabstray.TabsTrayStyling
import mozilla.components.browser.tabstray.ViewHolderProvider
import mozilla.components.browser.thumbnails.loader.ThumbnailLoader
import mozilla.components.feature.tabs.tabstray.TabsFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.browser.BrowsingModeManager
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.ext.requireComponents
import ie.equalit.ceno.ui.theme.DefaultThemeManager
import ie.equalit.ceno.ui.theme.ThemeManager

/**
 * A fragment for displaying the tabs tray.
 */
class TabsTrayFragment : Fragment(), UserInteractionHandler {
    private var tabsFeature: TabsFeature? = null

    lateinit var themeManager: ThemeManager
    lateinit var browsingModeManager: BrowsingModeManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        themeManager = (activity as BrowserActivity).themeManager
        browsingModeManager = (activity as BrowserActivity).browsingModeManager
        return inflater.inflate(R.layout.fragment_tabstray, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireComponents.metrics.autoTracker.measureVisit(listOf(TAG))

        val trayAdapter = createAndSetupTabsTray(requireContext())

        tabsFeature = TabsFeature(
            trayAdapter,
            requireComponents.core.store,
            { closeTabsTray(true) },
        ) {
            /* CENO: check if current tab is normal/private, set tabs panel and filter to match */
            if(requireComponents.core.store.state.selectedTab?.content?.private == true) {
                it.content.private
            } else {
                !it.content.private
            }
        }

        val tabsPanel: TabsPanel = view.findViewById(R.id.tabsPanel)
        val tabsToolbar: TabsToolbar = view.findViewById(R.id.tabsToolbar)

        tabsPanel.initialize(tabsFeature, browsingModeManager, updateTabsToolbar = ::updateTabsToolbar)
        tabsToolbar.initialize(tabsFeature, browsingModeManager, ::closeTabsTray)

    }

    override fun onStart() {
        super.onStart()
        tabsFeature?.start()
        themeManager.applyStatusBarThemeTabsTray()
    }

    override fun onStop() {
        super.onStop()

        tabsFeature?.stop()
    }

    override fun onBackPressed(): Boolean {
        val newTab = requireComponents.core.store.state.selectedTabId == ""
        closeTabsTray(newTab)
        return true
    }

    /* CENO: Modify closeTabsTray function to take booleans for determining
     * how to close the TabsTrayFragment, i.e. to open the Home or Browser Fragment,
     * with or without a new blank tab? */
    private fun closeTabsTray(newTab: Boolean = false) {
        findNavController().popBackStack() //This way, clicking back-button on the next fragment would not lead back here
        if(newTab) {
            findNavController().navigate(R.id.action_global_home)
        }
        else {
            findNavController().navigate(R.id.action_global_browser)
        }
    }

    private fun updateTabsToolbar(isPrivate: Boolean) {
        val tabsToolbar = requireView().findViewById<TabsToolbar>(R.id.tabsToolbar)
        tabsToolbar.updateToolbar(isPrivate)
    }

    /* CENO: Needed method to select normal/private tab during init of TabsTray */
    private fun selectTabInPanel(isPrivate: Boolean) {
        val tabsPanel = requireView().findViewById<TabsPanel>(R.id.tabsPanel)
        tabsPanel.selectTab(isPrivate)
    }

    private fun createAndSetupTabsTray(context: Context): TabsTray {
        val layoutManager = LinearLayoutManager(context)
        val thumbnailLoader = ThumbnailLoader(context.components.core.thumbnailStorage)
        val trayStyling = TabsTrayStyling(
                itemBackgroundColor = Color.TRANSPARENT,
                itemTextColor = ContextCompat.getColor(requireContext(), R.color.fx_mobile_text_color_primary)
        )
        val viewHolderProvider: ViewHolderProvider = { viewGroup ->
            val view = LayoutInflater.from(context)
                .inflate(R.layout.browser_tabstray_item, viewGroup, false)

            DefaultTabViewHolder(view, thumbnailLoader)
        }
        val tabsAdapter = TabsAdapter(
            thumbnailLoader = thumbnailLoader,
            viewHolderProvider = viewHolderProvider,
            styling = trayStyling,
            delegate = object : TabsTray.Delegate {
                override fun onTabSelected(tab: TabSessionState, source: String?) {
                    browsingModeManager.mode = BrowsingMode.fromBoolean(tab.content.private)
                    requireComponents.useCases.tabsUseCases.selectTab(tab.id)
                    closeTabsTray(false)
                }

                override fun onTabClosed(tab: TabSessionState, source: String?) {
                    requireComponents.useCases.tabsUseCases.removeTab(tab.id)
                }
            },
        )

        val tabsTray = requireView().findViewById<RecyclerView>(R.id.tabsTray)
        tabsTray.layoutManager = layoutManager
        tabsTray.adapter = tabsAdapter

        TabsTouchHelper {
            requireComponents.useCases.tabsUseCases.removeTab(it.id)
        }.attachToRecyclerView(tabsTray)

        return tabsAdapter
    }

    companion object {
        private const val TAG = "TabsTrayFragment"
    }
}
