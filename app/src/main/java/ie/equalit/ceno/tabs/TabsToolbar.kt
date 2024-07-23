/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.tabs

import android.content.Context
import android.util.AttributeSet
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingMode
import ie.equalit.ceno.browser.BrowsingModeManager
import ie.equalit.ceno.ext.components
import mozilla.components.feature.tabs.tabstray.TabsFeature

/* CENO: Modify closeTabsTray function to take booleans for determining
 * how to close the TabsTrayFragment, i.e. to open the Home or Browser Fragment,
 * with or without a new blank tab? */
class TabsToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : androidx.appcompat.widget.Toolbar(context, attrs) {
    private var tabsFeature: TabsFeature? = null
    private var isPrivateTray = false
    private var closeTabsTray: ((Boolean) -> Unit)? = null
    private lateinit var browsingModeManager: BrowsingModeManager

    init {
        navigationContentDescription = "back"
        setNavigationIcon(R.drawable.mozac_ic_back)
        setNavigationOnClickListener {
            val newTab = components.core.store.state.selectedTabId == ""
            closeTabsTray?.invoke(newTab)
        }
        inflateMenu(R.menu.tabstray_menu)
        setOnMenuItemClickListener {
            val tabsUseCases = components.useCases.tabsUseCases
            when (it.itemId) {
                R.id.newTab -> {
                    //set browsing mode
                    browsingModeManager.mode = BrowsingMode.fromBoolean(isPrivateTray)
                    closeTabsTray?.invoke(true)
//                    when (isPrivateTray) {
//                        true -> {
//                            tabsUseCases.addTab.invoke("about:privatebrowsing", selectTab = true, private = true)
//                            closeTabsTray?.invoke(false)
//                        }
//                        false -> {
//                            closeTabsTray?.invoke(true)
//                        }
//                    }
                }

                R.id.closeTab -> {
                    when (isPrivateTray) {
                        true -> tabsUseCases.removePrivateTabs.invoke()
                        false -> tabsUseCases.removeNormalTabs.invoke()
                    }
                }
            }
            true
        }
    }

    fun initialize(
        tabsFeature: TabsFeature?,
        browsingModeManager: BrowsingModeManager,
        closeTabsTray: (Boolean) -> Unit
    ) {
        this.tabsFeature = tabsFeature
        this.closeTabsTray = closeTabsTray
        this.browsingModeManager = browsingModeManager
    }

    fun updateToolbar(isPrivate: Boolean) {
        // Store the state for the menu option
        isPrivateTray = isPrivate

        // Update the menu option text
        menu.findItem(R.id.closeTab).title = if (isPrivate) {
            context.getString(R.string.menu_action_close_tabs_private)
        } else {
            context.getString(R.string.menu_action_close_tabs)
        }
    }

    private val components = context.components
}
