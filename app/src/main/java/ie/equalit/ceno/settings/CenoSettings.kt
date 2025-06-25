package ie.equalit.ceno.settings

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.preference.PreferenceManager
import ie.equalit.ceno.BuildConfig
import ie.equalit.ceno.R
import ie.equalit.ceno.ext.components
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mozilla.components.concept.fetch.Request
import mozilla.components.support.base.log.logger.Logger
import java.util.Locale
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.random.Random
import androidx.core.content.edit
import mozilla.components.concept.fetch.MutableHeaders


@Serializable
data class OuinetStatus(
    val auto_refresh : Boolean,
    val bt_extra_bootstraps : Array<String>,
    val distributed_cache : Boolean,
    val external_udp_endpoints : Array<String>? = null,
    val injector_access : Boolean,
    val is_upnp_active : String,
    val local_cache_size : Long? = null,
    val bridge_announcement : Boolean,
    val local_udp_endpoints : Array<String>? = null,
    val logfile : Boolean,
    val max_cached_age : Int,
    val metrics_enabled: Boolean,
    val origin_access : Boolean,
    val ouinet_build_id : String,
    val ouinet_protocol : Int,
    val ouinet_version: String,
    val proxy_access : Boolean,
    val public_udp_endpoints: Array<String>? = null,
    val state: String,
    val udp_world_reachable : String? = null,
    val current_metrics_record_id: String
)

enum class OuinetKey(val command : String) {
    API_STATUS("api/status"),
    PURGE_CACHE("?purge_cache=do"),
    ORIGIN_ACCESS("?origin_access"),
    PROXY_ACCESS("?proxy_access"),
    INJECTOR_ACCESS("?injector_access"),
    DISTRIBUTED_CACHE("?distributed_cache"),
    GROUPS_TXT("groups.txt"),
    LOGFILE("?logfile"),
    EXTRA_BOOTSTRAPS("?bt_extra_bootstraps"),
    LOG_LEVEL("log_level"),
    CENO_METRICS("?metrics"),
    ADD_METRICS("api/metrics/set_key_value?record_id")
}

enum class OuinetValue(val string: String) {
    DISABLED("disabled"),
    ENABLED("enabled"),
    OTHER("other"),
    ENABLE("enable"),
    DISABLE("disable")
}

object CenoSettings {

    const val SET_VALUE_ENDPOINT = "http://127.0.0.1:" + BuildConfig.FRONTEND_PORT
    const val LOGFILE_TXT = "logfile.txt"
    private const val TOKEN_LENGTH = 27

    var currentMetricsRecordId:String = ""

    private fun log2(n: Double): Double {
        return ln(n) / ln(2.0)
    }

    private fun bytesToString(b: Long): String {
        // originally from <https://stackoverflow.com/a/42408230>
        // ported from extension JS code to kotlin
        if (b == 0L) {
            return "0 B"
        }
        val i = floor(log2(b.toDouble()) / 10).toInt()
        val v = b / 1024.0.pow(i)
        val u =
            if (i > 0)
                "KMGTPEZY"[i - 1] + "iB"
            else
                "B"
        return String.format(Locale.getDefault(), "%.2f %s", v, u)
    }

    fun isStatusUpdateRequired(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_ceno_status_update_required), false
        )

    fun setStatusUpdateRequired(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_ceno_status_update_required)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    fun getOuinetState(context: Context) : String? =
        PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_ouinet_state), null
        )

    fun setOuinetState(context: Context, text : String) {
        val key = context.getString(R.string.pref_key_ouinet_state)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(key, text)
            }
    }

    private fun setCenoSourcesOrigin(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_ceno_sources_origin)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    private fun setCenoSourcesPrivate(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_ceno_sources_private)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    private fun setCenoSourcesPublic(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_ceno_sources_public)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    private fun setCenoSourcesShared(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_ceno_sources_shared)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    fun getCenoCacheSize(context: Context) : String? =
        PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_ceno_cache_size), null
        )

    fun setCenoCacheSize(context: Context, text : String) {
        val key = context.getString(R.string.pref_key_ceno_cache_size)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(key, text)
            }
    }

    fun getReachabilityStatus(context: Context) : String? =
        PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_ouinet_reachability_status), null
        )

    private fun setReachabilityStatus(context: Context, text : String?) {
        val key = context.getString(R.string.pref_key_ouinet_reachability_status)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(key, text)
            }
    }

    fun getUpnpStatus(context: Context) : String? =
        PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_ouinet_upnp_status), null
        )

    private fun setUpnpStatus(context: Context, text : String?) {
        val key = context.getString(R.string.pref_key_ouinet_upnp_status)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(key, text)
            }
    }

    fun getLocalUdpEndpoint(context: Context) : String? =
        PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_ouinet_local_udp_endpoints), null
        )

    private fun setLocalUdpEndpoint(context: Context, texts : Array<String>?) {
        val key = context.getString(R.string.pref_key_ouinet_local_udp_endpoints)

        var formattedText = ""
        texts?.forEach { formattedText += "${it.trim()} " }

        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putString(key, formattedText.ifEmpty { null })
            }
    }

    fun getExternalUdpEndpoint(context: Context) : String? =
        PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_ouinet_external_udp_endpoints), null
        )

    private fun setExternalUdpEndpoint(context: Context, texts : Array<String>?) {
        val key = context.getString(R.string.pref_key_ouinet_external_udp_endpoints)

        var formattedText = ""
        texts?.forEach { formattedText += "${it.trim()} " }

        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(key, formattedText.ifEmpty { null })
            }
    }

    fun getPublicUdpEndpoint(context: Context) : String? =
        PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_ouinet_public_udp_endpoints), null
        )

    private fun setPublicUdpEndpoint(context: Context, texts : Array<String>?) {
        val key = context.getString(R.string.pref_key_ouinet_public_udp_endpoints)

        var formattedText = ""
        texts?.forEach { formattedText += "${it.trim()} " }

        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putString(key, formattedText.ifEmpty { null })
            }
    }

    fun getLocalBTSources(context: Context) : List<String>? {
        val sources = PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_ouinet_extra_bittorrent_bootstraps), null
        ) ?: return null

        return sources.split(" ")
    }

    fun setExtraBitTorrentBootstrap(context: Context, texts : Array<String>?) {
        val key = context.getString(R.string.pref_key_ouinet_extra_bittorrent_bootstraps)

        var formattedText = ""
        texts?.forEach { formattedText = "$formattedText ${it.trim()}" }
        formattedText = formattedText.trim()

        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putString(key, formattedText.ifEmpty { null })
            }
    }

    fun getCenoGroupsCount(context: Context) : Int =
        PreferenceManager.getDefaultSharedPreferences(context).getInt(
            context.getString(R.string.pref_key_ceno_groups_count), 0
        )

    private fun setCenoGroupsCount(context: Context, i : Int) {
        val key = context.getString(R.string.pref_key_ceno_groups_count)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putInt(key, i)
            }
    }

    fun getOuinetVersion(context: Context) : String? =
        PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_ouinet_version), null
        )

    private fun setOuinetVersion(context: Context, text : String) {
        val key = context.getString(R.string.pref_key_ouinet_version)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putString(key, text)
            }
    }

    fun getOuinetBuildId(context: Context) : String? =
        PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_ouinet_build_id), null
        )

    private fun setOuinetBuildId(context: Context, text : String) {
        val key = context.getString(R.string.pref_key_ouinet_build_id)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putString(key, text)
            }
    }

    fun getOuinetProtocol(context: Context) : Int =
        PreferenceManager.getDefaultSharedPreferences(context).getInt(
            context.getString(R.string.pref_key_ouinet_protocol), 0
        )

    private fun setOuinetProtocol(context: Context, i : Int) {
        val key = context.getString(R.string.pref_key_ouinet_protocol)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putInt(key, i)
            }
    }

    fun isCenoLogEnabled(context: Context) : Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_ceno_enable_log), BuildConfig.DEBUG
        )

    fun setCenoEnableLog(context: Context, value: Boolean) {
        val key = context.getString(R.string.pref_key_ceno_enable_log)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit() {
                putBoolean(key, value)
            }
    }

    fun isBridgeAnnouncementEnabled(context: Context) : Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_bridge_announcement), false
        )

    fun getCenoVersionString(context: Context) : String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            String.format(
                "%s Build ID %s",
                packageInfo.versionName,
                packageInfo.versionCode,
            )
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    suspend fun webClientRequest (context: Context, request: Request): String? {
        var responseBody : String? = null
        var tries = 0
        var success = false
        while (tries < 5 && !success) {
            try {
                context.components.core.client.fetch(request).use { response ->
                    if (response.status == 200) {
                        Logger.debug("webClientRequest succeeded try $tries")
                        Logger.debug("Response header: ${response.headers}")
                        responseBody = response.body.string()
                        success = true
                    } else {
                        tries++
                        Logger.debug("webClientRequest failed on try $tries")
                        delay(500)
                    }
                }
            } catch (ex: Exception) {
                tries++
                Logger.debug("webClientRequest failed on try $tries")
                delay(500)
            }
        }
        return responseBody
    }

    private fun updateOuinetStatus(context : Context, responseBody : String, shouldRefresh: Boolean) {
        val status = Json.decodeFromString<OuinetStatus>(responseBody)
        Logger.debug("Response body: $status")
        setOuinetState(context, status.state)
        setCenoSourcesOrigin(context, status.origin_access)
        setCenoSourcesPrivate(context, status.proxy_access)
        setCenoSourcesPublic(context, status.injector_access)
        setCenoSourcesShared(context, status.distributed_cache)
        setCenoCacheSize(context, bytesToString(status.local_cache_size!!))
        setOuinetVersion(context, status.ouinet_version)
        setOuinetBuildId(context, status.ouinet_build_id)
        setOuinetProtocol(context, status.ouinet_protocol)
        setCenoEnableLog(context, status.logfile)
        setReachabilityStatus(context, status.udp_world_reachable)
        setLocalUdpEndpoint(context, status.local_udp_endpoints)
        setExternalUdpEndpoint(context, status.external_udp_endpoints)
        setPublicUdpEndpoint(context, status.public_udp_endpoints)
        setExtraBitTorrentBootstrap(context, status.bt_extra_bootstraps)
        setUpnpStatus(context, status.is_upnp_active)
        currentMetricsRecordId = status.current_metrics_record_id
        if(shouldRefresh) context.components.cenoPreferences.sharedPrefsReload = true
    }

    private fun updateCenoGroups(context : Context, responseBody : String) {
        Logger.debug("Response body: $responseBody")
        val groups = responseBody.reader().readLines()
        setCenoGroupsCount(context, groups.count())
        context.components.cenoPreferences.sharedPrefsUpdate = true
    }

    fun ouinetClientRequest(
        context: Context,
        key : OuinetKey,
        newValue: OuinetValue? = null,
        stringValue: String? = null,
        ouinetResponseListener: OuinetResponseListener? = null,
        shouldRefresh: Boolean = true,
        forMetrics : Boolean = false,
        metricsKey : String? = null
    ) {
        MainScope().launch {
            val request : String = if (metricsKey != null) {
                "${SET_VALUE_ENDPOINT}/${key.command}=${currentMetricsRecordId}&key=$metricsKey&value=$stringValue"
            } else {
                if (newValue != null)
                    "${SET_VALUE_ENDPOINT}/${key.command}=${if (newValue == OuinetValue.OTHER && stringValue != null) stringValue else newValue.string}"
                else
                    "${SET_VALUE_ENDPOINT}/${key.command}"
            }

            webClientRequest (
                context,
                Request(request, headers = MutableHeaders(Pair("X-Ouinet-Front-End-Token",
                    context.components.ouinet.METRICS_FRONTEND_TOKEN)))
            ).let { response ->

                if(response == null) ouinetResponseListener?.onError()

                when (key) {
                    OuinetKey.API_STATUS -> {
                        if (response != null)
                            if(forMetrics)
                                currentMetricsRecordId = Json.decodeFromString<OuinetStatus>(response).current_metrics_record_id
                            else
                                updateOuinetStatus(context, response, shouldRefresh)
                        else
                            ouinetResponseListener?.onError()
                    }
                    OuinetKey.PURGE_CACHE -> {
                        val text = if (response != null) {
                            setCenoCacheSize(context, bytesToString(0))
                            setCenoGroupsCount(context, 0)
                            context.components.cenoPreferences.sharedPrefsUpdate = true
                            context.resources.getString(R.string.clear_cache_success)
                        }
                        else {
                            context.resources.getString(R.string.clear_cache_fail)
                        }
                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                    }
                    OuinetKey.EXTRA_BOOTSTRAPS -> {
                        if(response != null)
                            ouinetResponseListener?.onSuccess(stringValue ?: "")
                        else
                            ouinetResponseListener?.onError()
                    }
                    OuinetKey.ORIGIN_ACCESS,
                    OuinetKey.PROXY_ACCESS,
                    OuinetKey.INJECTOR_ACCESS,
                    OuinetKey.DISTRIBUTED_CACHE,
                    OuinetKey.LOG_LEVEL,
                    OuinetKey.LOGFILE
                    -> {
                        if (response == null) {
                            Toast.makeText(
                                context,
                                context.resources.getString(R.string.ouinet_client_fetch_fail),
                                Toast.LENGTH_SHORT
                            ).show()
                            ouinetResponseListener?.onError()
                        }
                        else {
                            ouinetResponseListener?.onSuccess(stringValue ?: "")
                        }
                    }
                    OuinetKey.GROUPS_TXT -> {
                        if (response != null) {
                            updateCenoGroups(context, response)
                            ouinetResponseListener?.onSuccess(response)
                        }
                        else
                            ouinetResponseListener?.onError()
                    }
                    OuinetKey.CENO_METRICS -> {
                        if (response == null) {
                            ouinetResponseListener?.onError()
                        } else {
                            ouinetResponseListener?.onSuccess(response)
                        }
                    }
                    OuinetKey.ADD_METRICS -> {

                    }
                }
            }
            return@launch
        }
    }
    fun generateRandomToken() : String{
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..TOKEN_LENGTH)
            .map { Random.nextInt(0, charPool.size).let { charPool[it] } }
            .joinToString("")
    }
}
