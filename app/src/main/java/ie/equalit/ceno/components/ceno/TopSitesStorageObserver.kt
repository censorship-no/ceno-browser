package ie.equalit.ceno.components.ceno

import ie.equalit.ceno.components.ceno.appstate.AppAction
import ie.equalit.ceno.ext.ceno.sort
import ie.equalit.ceno.utils.CenoPreferences
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mozilla.components.feature.top.sites.DefaultTopSitesStorage
import mozilla.components.feature.top.sites.TopSitesStorage

/**
 * TopSitesStorageObserver implements the observer interface for the TopSitesStorage
 * to watch for changes to the storage and update the cached sites and change them in the AppStore
 */
class TopSitesStorageObserver(
    val storage: DefaultTopSitesStorage,
    val preferences: CenoPreferences,
    val store: AppStore
) : TopSitesStorage.Observer {
    private val scope = MainScope()
    override fun onStorageUpdated() {
        scope.launch {
            storage.getTopSites(preferences.topSitesMaxLimit)
            store.dispatch(
                AppAction.Change(
                    topSites = storage.cachedTopSites.sort()
                )
            )
        }
    }
}