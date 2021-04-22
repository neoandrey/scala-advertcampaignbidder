package neo.advertcampaignbidder

import neo.advertcampaignbidder.BidRegistry._
import spray.json.DefaultJsonProtocol

object BidJsonFormats  {

  import DefaultJsonProtocol._

  implicit val targetingJsonFormat       = jsonFormat1(Targeting)
  implicit val siteJsonFormat            = jsonFormat2(Site)
  implicit val bannerJsonFormat          = jsonFormat4(Banner)
  implicit val campaignJsonFormat        = jsonFormat5(Campaign)
  implicit val impressionJsonFormat      = jsonFormat8(Impression)

  implicit val geoJsonFormat             = jsonFormat1(Geo)
  implicit val deviceJsonFormat          = jsonFormat2(Device)
  implicit val userJsonFormat            = jsonFormat2(User)

  implicit val bidRequestJsonFormat      = jsonFormat5(BidRequest)
  implicit val bidResponseJsonFormat     = jsonFormat5(BidResponse)
  implicit val OperationDoneJsonFormat   = jsonFormat1(OperationDone)
  implicit val campaignsJsonFormat       = jsonFormat1(Campaigns)
  implicit val bidsJsonFormat            = jsonFormat1(Bids)

}