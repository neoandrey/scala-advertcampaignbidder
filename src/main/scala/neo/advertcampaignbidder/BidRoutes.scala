package neo.advertcampaignbidder

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.ClientError
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import neo.advertcampaignbidder.BidRegistry._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout

class BidRoutes(bidRegistry: ActorRef[BidRegistry.Command])(implicit val system: ActorSystem[_]) {
  

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import BidJsonFormats._
  private implicit val timeout                               =  Timeout.create(system.settings.config.getDuration("settings.routes.ask-timeout"))
  private final val  noMatchCode:StatusCode                  =  StatusCodes.custom(204,  "No campaign match found","Not Found",true, false)
  def getCampaigns(): Future[Campaigns]                      =  bidRegistry.ask(GetCampaigns)
  def getCampaign(id: String): Future[GetCampaignInfo]       =  bidRegistry.ask(GetCampaign(id, _))
  def addCampaign(campaign: Campaign): Future[OperationDone] =  bidRegistry.ask(AddCampaign(campaign, _))
  def getBids(): Future[Bids]                                =  bidRegistry.ask(GetBids)
  def getBid(id: String): Future[GetBidInfo]                 =  bidRegistry.ask(GetBid(id, _))
  def placeBid(bid: BidRequest): Future[Option[BidResponse]] =  bidRegistry.ask(PlaceBid(bid, _))

  val BidRoutes: Route =
  concat(
    pathPrefix("campaigns") {
      concat(
        pathEnd {
          concat(
            get {
              complete(getCampaigns())
            },
            post {
              entity(as[Campaign]) { campaign=>
                onSuccess(addCampaign(campaign)) { opDone =>
                       complete((StatusCodes.Created, opDone))
                }
              }
            })
        },
        path(Segment) { id =>
            get {
              rejectEmptyResponse {
                onSuccess(getCampaign(id)) { response =>
                  complete(response.possibleCampaign)
                }
              }
    
            }
        })
    },
    pathPrefix("bids") {
      concat(
        pathEnd {
          concat(
            get {
              complete(getBids())
            },
            post {
              entity(as[BidRequest]) { bid =>
                onSuccess(placeBid(bid)) { opDone: Option[BidResponse] =>
                   complete((StatusCodes.Created, opDone))
                   opDone  match {
                      case  x if x.nonEmpty =>   complete((StatusCodes.Created, opDone))
                      case  _   =>   complete(noMatchCode, OperationDone(s"Not Found"))
                   }
                      
                }
            
            }})
        },
        path(Segment) { id =>
     
            get {
              rejectEmptyResponse {
                onSuccess(getBid(id)) { response =>
                  complete(response.possibleBid)
                }
              }
              
            }
        })
      
    }
  )
 
}
