package ie.equalit.ceno.metrics

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import ie.equalit.ceno.components.ceno.CenoLocationUtils
import ie.equalit.ceno.ext.application
import ie.equalit.ceno.settings.CenoSettings
import ie.equalit.ceno.settings.OuinetKey
import ie.equalit.ceno.settings.OuinetResponseListener
import ie.equalit.ceno.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class LocationMetrics(
    val context:Context,
    val lifecycleScope: CoroutineScope
) {
    private val metricsRecordId:Flow<String> = flow {
        var previousRecordid = ""
        while(true) {
            CenoSettings.ouinetClientRequest(context, OuinetKey.API_STATUS, forMetrics = true)
            if (CenoSettings.currentMetricsRecordId != previousRecordid)  {
                emit(CenoSettings.currentMetricsRecordId)
                previousRecordid = CenoSettings.currentMetricsRecordId
            }
            delay(recordIdRefreshInterval)
        }
    }

    fun collectLocationMetrics() {
        lifecycleScope.launch {
            metricsRecordId.collect { recordId ->
                //send metrics
                //network country
                addMetricToRecord(recordId, MetricsKeys.NETWORK_COUNTRY, CenoLocationUtils(context.application).currentCountry)
                //network operator
                addMetricToRecord(recordId, MetricsKeys.NETWORK_OPERATOR, getNetworkOperator())
            }
        }
    }

    private fun addMetricToRecord(recordId:String, key : MetricsKeys, value:String) {
        CenoSettings.ouinetClientRequest(
            context,
            OuinetKey.ADD_METRICS,
            stringValue = value,
            ouinetResponseListener = object : OuinetResponseListener {
                override fun onSuccess(message: String, data: Any?) {
                    Log.d(TAG, "Successfully set metrics for record: $recordId")
                }

                override fun onError() {
                    Log.e(TAG, "Failed to set metrics for record: $recordId")
                }
            },
            forMetrics = true,
            metricsKey = key.name
        )
    }

    private fun getNetworkOperator(): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.networkOperatorName
    }

    companion object {
        const val TAG = "Ceno-Metrics"
        const val recordIdRefreshInterval = 15000L
    }
}