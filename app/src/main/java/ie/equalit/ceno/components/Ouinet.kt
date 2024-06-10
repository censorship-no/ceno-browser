package ie.equalit.ceno.components

import android.content.Context
import ie.equalit.ceno.BuildConfig
import ie.equalit.ceno.R
import ie.equalit.ceno.components.ceno.CenoLocationUtils
import ie.equalit.ceno.ext.application
import ie.equalit.ceno.settings.CenoSettings
import ie.equalit.ouinet.Config
import ie.equalit.ouinet.NotificationConfig
import ie.equalit.ouinet.OuinetBackground
import ie.equalit.ouinet.OuinetNotification.Companion.MILLISECOND
import mozilla.components.support.base.log.logger.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class Ouinet (
        private val context : Context
    ) {

    lateinit var config: Config

    fun setConfig() {

        val errorPageFilePath = try {
            copyFileFromAssetsToInternalStorage("error_page.html", context)
            File(context.filesDir, "error_page.html").absolutePath
        } catch (e: Exception) {
            ""
        }

        config = Config.ConfigBuilder(context)
            .setCacheHttpPubKey(BuildConfig.CACHE_PUB_KEY)
            .setInjectorCredentials(BuildConfig.INJECTOR_CREDENTIALS)
            .setInjectorTlsCert(BuildConfig.INJECTOR_TLS_CERT)
            .setTlsCaCertStorePath(context.resources.getString(R.string.cacert_file_path))
            .setCacheType(context.resources.getString(R.string.cache_type))
            .setBtBootstrapExtras(getBtBootstrapExtras())
            .setListenOnTcp(context.resources.getString(R.string.loopback_ip) + ":" + BuildConfig.PROXY_PORT)
            .setFrontEndEp(context.resources.getString(R.string.loopback_ip) + ":" + BuildConfig.FRONTEND_PORT)
            .setErrorPagePath(errorPageFilePath)
            .setDisableBridgeAnnouncement(!CenoSettings.isBridgeAnnouncementEnabled(context))
            .build()
    }

    private val notificationConfig by lazy {
        NotificationConfig.Builder(context)
            .setHomeActivity("ie.equalit.ceno.BrowserActivity")
            .setNotificationIcons(
                statusIcon = R.drawable.ic_notification,
                homeIcon = R.drawable.ic_globe_pm,
                clearIcon = R.drawable.ic_cancel_pm
            )
            .setChannelName(context.resources.getString(R.string.ceno_notification_channel_name))
            .setNotificationText (
                title = context.resources.getString(R.string.ceno_notification_title),
                description = context.resources.getString(R.string.ceno_notification_description),
                homeText = context.resources.getString(R.string.ceno_notification_home_description),
                clearText = context.resources.getString(R.string.ceno_notification_clear_description),
                confirmText = context.resources.getString(R.string.ceno_notification_clear_do_description),
            )
            .setUpdateInterval(1 * MILLISECOND)
            .build()
    }

    private lateinit var onNotificationTapped : () -> Unit
    fun setOnNotificationTapped (listener : () -> Unit) {
       onNotificationTapped = listener
    }

    private lateinit var onConfirmTapped : () -> Unit
    fun setOnConfirmTapped (listener : () -> Unit) {
        onConfirmTapped = listener
    }

    lateinit var background : OuinetBackground
    fun setBackground (ctx: Context) {
        background = OuinetBackground.Builder(ctx)
            .setOuinetConfig(config)
            .setNotificationConfig(notificationConfig)
            .setOnNotifiactionTappedListener { onNotificationTapped.invoke() }
            .setOnConfirmTappedListener{ onConfirmTapped.invoke() }
            .build()
    }

    private fun getBtBootstrapExtras() : Set<String>? {
        var countryIsoCode = ""
        val locationUtils = CenoLocationUtils(context.application)
        countryIsoCode = locationUtils.currentCountry

        // Attempt getting country-specific `BT_BOOTSTRAP_EXTRAS` entry from BuildConfig,
        // fall back to empty BT bootstrap extras otherwise.
        var btbsxsStr= ""
        if (countryIsoCode.isNotEmpty()) {
            // Country code found, try getting bootstrap extras resource for this country
            for (entry in BuildConfig.BT_BOOTSTRAP_EXTRAS) {
                if (countryIsoCode == entry[0]) {
                    btbsxsStr = entry[1]
                }
            }
        }

        if (btbsxsStr != "") {
            // Bootstrap extras resource found
            val btbsxs: HashSet<String> = HashSet()
            for (x in btbsxsStr.split(" ").toTypedArray()) {
                if (x.isNotEmpty()) {
                    btbsxs.add(x)
                }
            }
            if (btbsxs.size > 0) {
                Logger.debug("Extra BT bootstraps: $btbsxs")
                return btbsxs
            }
        }
        // else no bootstrap extras included, leave null
        Logger.debug("No extra BT bootstraps required")
        return null
    }

    @Throws(IOException::class)
    private fun copyFileFromAssetsToInternalStorage(fileName: String, context: Context) {
        val inputStream: InputStream = context.assets.open(fileName)
        val outFile = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(outFile)

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }
}