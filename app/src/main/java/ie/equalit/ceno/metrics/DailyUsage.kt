package ie.equalit.ceno.metrics

import org.cleaninsights.sdk.CleanInsights

class DailyUsage(private val cleanInsights: CleanInsights) {

    fun measureVisit(scenePath : List<String>) {
        cleanInsights.measureVisit(
            scenePath = scenePath,
            campaignId = ID,
        )
    }

    fun enableCampaign() {
        cleanInsights.grant(ID)
    }

    fun disableCampaign() {
        cleanInsights.deny(ID)
    }

    companion object {
        const val ID = "dailyUsage"
    }
}