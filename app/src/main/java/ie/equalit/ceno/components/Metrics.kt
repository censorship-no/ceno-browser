package ie.equalit.ceno.components

import ie.equalit.ceno.metrics.campaign001.Campaign001
import org.cleaninsights.sdk.CleanInsights

class Metrics {
    lateinit var campaign001 : Campaign001

    fun initCampaign001(cleanInsights: CleanInsights) {
        campaign001 = Campaign001(cleanInsights, Campaigns.C001.campaignId())
    }

    enum class Campaigns {
        C001;

        fun campaignId() = when (this) {
            C001 -> "ouinet-startup-time"
        }
    }
}