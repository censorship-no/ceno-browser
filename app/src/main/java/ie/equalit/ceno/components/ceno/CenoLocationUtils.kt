package ie.equalit.ceno.components.ceno

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.telephony.TelephonyManager
import android.text.TextUtils
import java.util.Locale
import java.util.logging.Logger

class CenoLocationUtils constructor(app: Application) {
    private val appContext: Context

    /**
     * This guesses the current country from the first of these sources that
     * succeeds (also in order of likelihood of being correct):
     *
     *
     *  * Phone network. This works even when no SIM card is inserted, or a
     * foreign SIM card is inserted.
     *  * SIM card. This is only an heuristic and assumes the user is not
     * roaming.
     *  * User locale. This is an even worse heuristic.
     *
     *
     * Note: this is very similar to
     * [this API](https://android.googlesource.com/platform/frameworks/base/+/cd92588%5E/location/java/android/location/CountryDetector.java)
     * except it seems that Google doesn't want us to use it for some reason - both that class and `Context.COUNTRY_CODE` are
     * annotated `@hide`.
     *
     * This is a port of Briar source code to Kotlin, see link for java source,
     * https://code.briarproject.org/briar/briar/-/blob/master/bramble-android/src/main/java/org/briarproject/bramble/system/AndroidLocationUtils.java
     */
    @get:SuppressLint("DefaultLocale")
    val currentCountry: String
        get() {
            var countryCode = countryFromPhoneNetwork
            if (!TextUtils.isEmpty(countryCode)) return countryCode.uppercase(Locale.getDefault())
            LOG.info("Falling back to SIM card country")
            countryCode = countryFromSimCard
            if (!TextUtils.isEmpty(countryCode)) return countryCode.uppercase(Locale.getDefault())
            LOG.info("Falling back to user-defined locale")
            return Locale.getDefault().country
        }
    private val countryFromPhoneNetwork: String
        get() {
            val o = appContext.getSystemService(Context.TELEPHONY_SERVICE)
            val tm = o as TelephonyManager
            return tm.networkCountryIso
        }
    private val countryFromSimCard: String
        get() {
            val o = appContext.getSystemService(Context.TELEPHONY_SERVICE)
            val tm = o as TelephonyManager
            return tm.simCountryIso
        }

    companion object {
        private val LOG = Logger.getLogger(
            CenoLocationUtils::class.java.name
        )
    }

    init {
        appContext = app.applicationContext
    }
}
