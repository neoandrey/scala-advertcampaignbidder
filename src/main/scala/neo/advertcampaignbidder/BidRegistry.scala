
package neo.advertcampaignbidder

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable
import scala.util.Random

case class Targeting(targetedSiteIds: Vector[String]) 
case class Banner(id: Int, src: String, width: Int, height: Int) 
case class Campaign(id: Int, country: String, targeting: Targeting, banners: Vector[Banner], bid: Double)

case class BidRequest(id: String, imp: Option[Vector[Impression]], site: Site, user: Option[User], device: Option[Device])
case class Impression(id: String, wmin: Option[Int], wmax: Option[Int], w: Option[Int], hmin: Option[Int], hmax: Option[Int], h: Option[Int], bidFloor: Option[Double])
case class Site(id: String, domain: String)
case class User(id: String, geo: Option[Geo])
case class Device(id: String, geo: Option[Geo])
case class Geo(country: Option[String])

case class BidResponse(id: String, bidRequestId: String, price: Double, adid: Option[String], banner: Option[Banner])

final case class Bids(bids: immutable.Vector[BidRequest])
final case class Campaigns(campaigns: immutable.Vector[Campaign])

object BidRegistry{

    sealed trait Command

    final case class GetCampaigns(replyTo: ActorRef[Campaigns]) extends Command
    final case class AddCampaign(campaign: Campaign, replyTo: ActorRef[OperationDone]) extends Command
    final case class GetCampaign(id: String,replyTo: ActorRef[GetCampaignInfo]) extends Command
    final case class GetBids(replyTo: ActorRef[Bids]) extends Command
    final case class PlaceBid(bid: BidRequest, replyTo: ActorRef[Option[BidResponse]]) extends Command
    final case class GetBid(id: String, replyTo: ActorRef[GetBidInfo]) extends Command
    final case class GetCampaignInfo(possibleCampaign: Option[Campaign])
    final case class GetBidInfo(possibleBid: Option[BidRequest])
    final case class OperationDone(description: String)
    
    type BidMap  =scala.collection.mutable.Map[BidRequest, BidResponse]
    var bidResponseMap:  BidMap = scala.collection.mutable.Map()

    def apply(): Behavior[Command] = registry(Vector.empty[Campaign],Vector.empty[BidRequest])

    def matchBidToCampaign(bid: BidRequest, campaigns: immutable.Vector[Campaign]): Option[BidResponse]  = {
        val matchThreshold                      = 10
        val dev                                 = bid.device.get.geo.get
        val usr                                 = bid.user.get.geo.get
        val impression                          = bid.imp.get
        val devCountry                          = dev.country.get
        val usrCountry                          = usr.country.get
        var bidResponse: Option[BidResponse]    = bid match {
                       case  x if   bidResponseMap.contains(x) => bidResponseMap.get(x)
                       case  x if   !bidResponseMap.contains(x) => {
                           val matchingCampaigns = campaigns.map((campaign:Campaign)  => {
                           var matchIndex:Int= 0
                           matchIndex += (impression.map(x=> x.bidFloor.get ).filter( bidFloor => bidFloor <= campaign.bid)).size
                           matchIndex += (campaign.targeting.targetedSiteIds.filter((siteId:String) => siteId == bid.site.id)).size 
                           matchIndex += (campaign.country match {
                                            case x if x==(devCountry)   =>  10 
                                            case x if x==(usrCountry)   =>  5  
                                            case _    =>  0          
                                        } )
                          var bannerMap:Array[(Impression, Banner,Int)] = Array[(Impression, Banner,Int)]()

                          campaign.banners.foreach((banner:Banner) => {
                             
                             impression.foreach( (impress:Impression) =>{
                                var bannerMatch:Int = 0
                                val h               =   impress.h.get
                                val hmax            =   impress.hmax.get
                                val hmin            =   impress.hmin.get
                                val w               =   impress.w.get
                                val wmax            =   impress.wmax.get
                                val wmin            =   impress.wmin.get
                                banner.height match {
                                    case x if x==h       =>   bannerMatch +=10
                                    case x if x==hmax    =>   bannerMatch +=5
                                    case x if x==hmin    =>   bannerMatch +=5
                                    case _               =>   bannerMatch +=0
                                }  

                                banner.width match {
                                        case x if x==w    =>   bannerMatch +=10
                                        case x if x==wmax =>   bannerMatch += 5
                                        case x if x==wmin =>   bannerMatch += 5
                                        case _            =>   bannerMatch += 0
                                }
                                bannerMap :+  (impress,  banner,  bannerMatch)
                            } 
                            
                            )
                            
                            } ) 
                             val bestBannerMatchvalue:Int =  bannerMap.size match {
                                 case x if x > 0 => bannerMap.sortBy{case (x,y,z) =>(-z)}.head._3
                                 case _  => 0    
                             } 
                             matchIndex += bestBannerMatchvalue
                             (matchIndex, campaign)
                        } ).filter(_._1 >=matchThreshold)

                      var bidRes: Option[BidResponse] =None 
                      if(matchingCampaigns.size >0 ){
                        val  preferredCampaign               = matchingCampaigns.toArray.sortBy{case (x,y) =>(-x)}.head._2
                        val  bidResponseSize                 = bidResponseMap.size +1
                        val  price                           = impression.map(x=> x.bidFloor.get ).filter( bidFloor => bidFloor <= preferredCampaign.bid).head
                        val  banner: Banner                  = preferredCampaign.banners.filter((banner:Banner) => {

                            val matchingImpressions =  impression.map( (impress:Impression) =>{
                                val h         = impress.h.get
                                val hmax      = impress.hmax.get
                                val hmin      = impress.hmin.get
                                val w         = impress.w.get
                                val wmax      = impress.wmax.get
                                val wmin      = impress.wmin.get
                                val hMatch: Boolean    =   banner.height match {
                                    case x if x==h    =>  true
                                    case x if x==hmax =>  true
                                    case x if x==hmin =>  true
                                    case _    =>  false
                                }  
                                val wMatch: Boolean    = banner.width match {
                                    case x if x==w    =>  true
                                    case x if x==wmax =>  true
                                    case x if x==wmin =>  true
                                    case _    =>  false
                                }
                                (impress, hMatch &&  wMatch)
                            } 
                            
                            )
                            matchingImpressions.size > 0

                            } ).head
                         bidRes   =  Some(BidResponse(
                            bidResponseSize.toString, 
                                bid.id,
                                price, 
                                Option(preferredCampaign.id.toString), 
                                Option(banner)
                        ))
                       }
                         if ( bidRes != None) {
                             bidResponseMap +=( bid -> bidRes.get)
                         }
                        bidRes
          }
          case _ => None
      }
       bidResponse                 
    }

    private def registry(campaigns: Vector[Campaign], bids: Vector[BidRequest]): Behavior[Command] =
        Behaviors.receiveMessage {
        case GetCampaigns(replyTo) =>
            replyTo ! Campaigns(campaigns)
            Behaviors.same
        case AddCampaign(campaign, replyTo) =>
            replyTo ! OperationDone(s"Campaign ${campaign.id} has been added.")
            registry( ( campaign match{
                case x if campaigns.filter(_.id==campaign.id).isEmpty => campaigns:+ campaign
                case  _ =>campaigns
           }
            ), Vector.empty[BidRequest])
        case GetCampaign(id, replyTo) =>
            replyTo ! GetCampaignInfo(campaigns.find(_.id.toString == id.toString))
            Behaviors.same
        case GetBids(replyTo) =>
            replyTo ! Bids(bids)
            Behaviors.same
        case PlaceBid(bid, replyTo) =>
            replyTo !  matchBidToCampaign(bid, campaigns) 
            registry(Vector.empty [Campaign], ( bid match{
                case x if bids.filter(_.id==bid.id).isEmpty => bids :+bid
                case  _ =>bids
       }
            )
     
     )
        case GetBid(id, replyTo) =>
            replyTo ! GetBidInfo(bids.find(_.id == id))
            Behaviors.same

        }

  }