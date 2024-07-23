package ie.equalit.ceno.tabs

import androidx.lifecycle.LifecycleOwner
import ie.equalit.ceno.browser.BrowsingModeManager
import ie.equalit.ceno.ui.theme.ThemeManager
import mozilla.components.browser.state.selector.findCustomTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.ui.tabcounter.TabCounterMenu

class TabCounterView(
    var toolbar: Toolbar,
    store: BrowserStore,
    sessionId: String? = null,
    lifecycleOwner: LifecycleOwner,
    showTabs: () -> Unit,
    tabCounterMenu: TabCounterMenu? = null,
    countBasedOnSelectedTabType: Boolean = true,
    browsingModeManager: BrowsingModeManager,
    themeManager: ThemeManager
) {
    private lateinit var tabsAction: TabCounterToolbarButton

    init {
        run {
            // this feature is not used for Custom Tabs
            if (sessionId != null && store.state.findCustomTab(sessionId) != null) return@run

            tabsAction = TabCounterToolbarButton(
                store = store,
                lifecycleOwner = lifecycleOwner,
                showTabs = showTabs,
                menu = tabCounterMenu,
                countBasedOnSelectedTabType = countBasedOnSelectedTabType,
                browsingModeManager = browsingModeManager,
                themeManager = themeManager
            )
            toolbar.addBrowserAction(tabsAction)
        }
    }

    fun update() {
        //  Need to find a better way to update the color of the tab counter
        toolbar.removeBrowserAction(tabsAction)
        toolbar.addBrowserAction(tabsAction)
    }
}