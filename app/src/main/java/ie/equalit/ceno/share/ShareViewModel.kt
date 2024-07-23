package ie.equalit.ceno.share

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.ConnectivityManager
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ie.equalit.ceno.R
import ie.equalit.ceno.share.DefaultShareController.Companion.ACTION_COPY_LINK_TO_CLIPBOARD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.feature.share.RecentAppsStorage
import mozilla.components.support.utils.ext.queryIntentActivitiesCompat

class ShareViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        internal const val RECENT_APPS_LIMIT = 6
    }

    private val connectivityManager by lazy { application.getSystemService<ConnectivityManager>() }

    @VisibleForTesting
    internal var recentAppsStorage = RecentAppsStorage(application.applicationContext)

    @VisibleForTesting
    internal var ioDispatcher = Dispatchers.IO

    private val appsListLiveData = MutableLiveData<List<AppShareOption>>(emptyList())
    private val recentAppsListLiveData = MutableLiveData<List<AppShareOption>>(emptyList())

    /**
     * List of applications that can be shared to.
     */
    val appsList: LiveData<List<AppShareOption>> get() = appsListLiveData

    /**
     * List of recent applications that can be shared to.
     */
    val recentAppsList: LiveData<List<AppShareOption>> get() = recentAppsListLiveData

    /**
     * Load a list of devices and apps into [devicesList] and [appsList].
     * Should be called when the fragment is attached so the data can be fetched early.
     */
    fun loadDevicesAndApps(context: Context) {

        // Start preparing the data as soon as we have a valid Context
        viewModelScope.launch(ioDispatcher) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val shareAppsActivities = getIntentActivities(shareIntent, context)

            var apps = buildAppsList(shareAppsActivities, context)
            recentAppsStorage.updateDatabaseWithNewApps(apps.map { app -> app.activityName })
            val recentApps = buildRecentAppsList(apps)
            apps = filterOutRecentApps(apps, recentApps)

            // if copy app is available, prepend to the list of actions
            getCopyApp(context)?.let {
                apps = listOf(it) + apps
            }

            recentAppsListLiveData.postValue(recentApps)
            appsListLiveData.postValue(apps)
        }
    }

    private fun getCopyApp(context: Context): AppShareOption? {
        val copyIcon = AppCompatResources.getDrawable(context, R.drawable.ic_share_clipboard)

        return copyIcon?.let {
            AppShareOption(
                context.getString(R.string.share_copy_link_to_clipboard),
                copyIcon,
                ACTION_COPY_LINK_TO_CLIPBOARD,
                "",
            )
        }
    }

    private fun filterOutRecentApps(
        apps: List<AppShareOption>,
        recentApps: List<AppShareOption>,
    ): List<AppShareOption> {
        return apps.filter { app -> !recentApps.contains(app) }
    }

    @WorkerThread
    internal fun buildRecentAppsList(apps: List<AppShareOption>): List<AppShareOption> {
        val recentAppsDatabase = recentAppsStorage.getRecentAppsUpTo(RECENT_APPS_LIMIT)
        val result: MutableList<AppShareOption> = ArrayList()
        for (recentApp in recentAppsDatabase) {
            for (app in apps) {
                if (recentApp.activityName == app.activityName) {
                    result.add(app)
                }
            }
        }
        return result
    }

    @VisibleForTesting
    @WorkerThread
    fun getIntentActivities(shareIntent: Intent, context: Context): List<ResolveInfo>? {
        return context.packageManager.queryIntentActivitiesCompat(shareIntent, 0)
    }

    /**
     * Returns a list of apps that can be shared to.
     *
     * @param intentActivities List of activities from [getIntentActivities].
     * @param context Android context.
     */
    @VisibleForTesting
    @WorkerThread
    internal fun buildAppsList(
        intentActivities: List<ResolveInfo>?,
        context: Context,
    ): List<AppShareOption> {
        return intentActivities
            .orEmpty()
            .filter { it.activityInfo.packageName != context.packageName }
            .map { resolveInfo ->
                AppShareOption(
                    resolveInfo.loadLabel(context.packageManager).toString(),
                    resolveInfo.loadIcon(context.packageManager),
                    resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.name,
                )
            }
    }
}
