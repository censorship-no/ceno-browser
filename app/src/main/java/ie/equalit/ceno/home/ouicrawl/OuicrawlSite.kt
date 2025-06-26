package ie.equalit.ceno.home.ouicrawl

import kotlinx.serialization.Serializable

@Serializable
data class OuicrawlSite (
    val Description:String,
    val NextCrawlDuration:String,
    val NextCrawlDurationTS:String,
    val LastCrawlStatusImage:String,
    val LastCrawlStatus:String,
    val LastCrawlUpdated:String,
    val LastCrawlUpdatedTS:String,
    val LastCrawlDuration:String,
    val LastCrawlDurationTime:Int,
    val LastCrawlSize:String,
    val FaviconURL:String,
    val FaviconURLFixed:String,
    val SiteURL:String,
    val GatewayURL:String,
    val GatewayFaviconURL:String,
    val SiteName:String,
    val CrawlInterval:String,
    val Language:String,
    val Country:String,
)
@Serializable
data class OuicrawledSitesListItem (
    val Sites:List<OuicrawlSite>,
    val IsViaOuinet:Boolean
)