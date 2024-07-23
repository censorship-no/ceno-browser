/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.tabs

import android.content.Context
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.tabs.TabLayout
import ie.equalit.ceno.R
import ie.equalit.ceno.browser.BrowsingModeManager
import mozilla.components.feature.tabs.tabstray.TabsFeature

class TabsPanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : TabLayout(context, attrs), TabLayout.OnTabSelectedListener {
    private var browsingModeManager: BrowsingModeManager? = null
    private var normalTab: Tab
    private var privateTab: Tab
    private var tabsFeature: TabsFeature? = null
    private var updateTabsToolbar: ((isPrivate: Boolean) -> Unit)? = null

    init {
        normalTab = newTab().apply {
            contentDescription = "Tabs"
            icon = resources.getThemedDrawable(R.drawable.ceno_home_card_public_icon)
        }

        privateTab = newTab().apply {
            contentDescription = "Personal tabs"
            icon = resources.getThemedDrawable(R.drawable.ceno_home_card_personal_icon)
        }

        addOnTabSelectedListener(this)

        addTab(normalTab)
        addTab(privateTab)
    }

    fun initialize(
        tabsFeature: TabsFeature?,
        browsingModeManager: BrowsingModeManager,
        updateTabsToolbar: (isPrivate: Boolean) -> Unit
    ) {
        this.tabsFeature = tabsFeature
        this.browsingModeManager = browsingModeManager
        this.updateTabsToolbar = updateTabsToolbar
        selectTab(browsingModeManager.mode.isPersonal)
    }

    override fun onTabSelected(tab: Tab?) {
        // Tint the selected tab's icon.
        if (tab == normalTab) {
            tab?.icon?.colorTint(R.color.ceno_home_card_public_text)
            this.tabSelectedIndicator.colorTint(R.color.ceno_home_card_public_text)
        } else {
            tab?.icon?.colorTint(R.color.ceno_home_card_personal_text)
            this.tabSelectedIndicator.colorTint(R.color.ceno_home_card_personal_text)
        }
        tabsFeature?.filterTabs { tabSessionState ->
            if (tab == normalTab) {
                !tabSessionState.content.private
            } else {
                tabSessionState.content.private
            }
        }

        updateTabsToolbar?.invoke(tab == privateTab)
    }

    /* CENO: Needed method to select normal/private tab during init of TabsTray */
    fun selectTab(isPrivate: Boolean) {
        if (isPrivate) {
            privateTab.select()
        } else {
            normalTab.select()
        }
    }

    override fun onTabReselected(tab: Tab?) {
        // no-op
    }

    override fun onTabUnselected(tab: Tab?) {
        // Clear the tint for the unselected tab's icon.
        tab?.icon?.colorFilter = null
    }

    private fun Resources.getThemedDrawable(@DrawableRes resId: Int) =
        ResourcesCompat.getDrawable(resources, resId, context.theme)

    private fun Drawable.colorTint(@ColorRes color: Int) = apply {
        mutate()
        @Suppress("DEPRECATION") // Deprecated warning appeared when switching to Java 11.
        setColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_IN)
    }
}
