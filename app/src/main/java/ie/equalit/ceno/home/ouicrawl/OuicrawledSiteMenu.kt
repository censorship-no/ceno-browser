package ie.equalit.ceno.home.ouicrawl

import android.content.Context
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.components
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem

class OuicrawledSiteMenu (
    private val context: Context,
    private val site: OuicrawlSite,
    private val onItemTapped: (Item) -> Unit = {}
) {
    sealed class Item {
        object OpenInPersonalTab : Item()
        object AddToShortcut : Item()
    }

    val menuBuilder by lazy { BrowserMenuBuilder(menuItems) }

    private val menuItems by lazy {
        listOfNotNull(
            SimpleBrowserMenuItem(
                context.getString(R.string.bookmark_menu_open_in_private_tab_button)
            ) {
                onItemTapped.invoke(Item.OpenInPersonalTab)
            },
            SimpleBrowserMenuItem(
                context.getString(
                    if((context.components.appStore.state.topSites.any {it.url == ("https://${site.SiteURL}/")}))
                        R.string.browser_menu_remove_from_shortcuts
                    else R.string.browser_menu_add_to_shortcuts)
            ) {
                onItemTapped.invoke(Item.AddToShortcut)
            },
        )
    }

}