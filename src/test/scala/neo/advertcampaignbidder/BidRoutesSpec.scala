package neo.advertcampaignbidder

//#campaign-routes-spec
//#test-top
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

//#set-up
class BidRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  //#test-top

  // the Akka HTTP route testkit does not yet support a typed actor system (https://github.com/akka/akka-http/issues/2036)
  // so we have to adapt for now
  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =  testKit.system.classicSystem

  // Here we need to implement all the abstract members of BidRoutes.
  // We use the real BidRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe
  // created with testKit.createTestProbe()
  val bidRegistry = testKit.spawn(BidRegistry())
  lazy val routes = new BidRoutes(bidRegistry).BidRoutes

  // use the json formats to marshal and unmarshall objects in the test
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import BidJsonFormats._
  //#set-up

  //#actual-test
  "BidRoutes" should {
    "return no campaigns if no present (GET /campaigns)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/campaigns")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"campaigns":[]}""")
      }
    }
	
	"return no bids if no present (GET /bids)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/bids")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"bids":[]}""")
      }
    }
    "be able to add campaigns (POST /campaigns)" in {
        val targeting: Targeting  = Targeting(Vector("0006a522ce0f4bbbbaa6b3c38cafaa0f"))
        val banner:Banner  = Banner(1,  "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",300,250)  
        val campaign = Campaign(1, "LT", targeting, Vector(banner), 5)
        val campaignEntity = Marshal(campaign).to[MessageEntity].futureValue 
        val campgID: Int = campaign.id
        val request = Post("/campaigns").withEntity(campaignEntity)

        request ~> routes ~> check {
            status should ===(StatusCodes.Created)
            contentType should ===(ContentTypes.`application/json`)
            entityAs[String] should ===("""{"description":"Campaign 1 has been added."}""")
          }
    }
	
	 "be able to match bids with campaigns (POST /bids)" in {
	 
	  val site   :Site             = Site(id= "0006a522ce0f4bbbbaa6b3c38cafaa0f",domain="fake.tld")
    val geo    :Geo              = Geo(country=Some("LT"));
	  val geoLT  :Option[Geo]      = Some(geo)
	  val user   :Option[User]     = Some(User(id="USARIO1",geo=geoLT))
	  val dvc    :Option[Device]   = Some(Device(id="440579f4b408831516ebd02f6e1c31b4", geo=geoLT))
    val impress: Impression      = Impression(id="1", wmin=Some(50),wmax=Some(300),hmin=Some(100),hmax=Some(300),h=Some(250),w=Some(300),bidFloor=Some(3.12123))
	  val bidReq :BidRequest       = BidRequest("SGu1Jpq1IO", Some(Vector(impress)), site, user, device=dvc)
    val bidReqEntity             = Marshal(bidReq).to[MessageEntity].futureValue
    
    val request = Post("/bids").withEntity(bidReqEntity)
    request ~> routes ~> check {
        status should      ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"adid":"1","banner":{"height":250,"id":1,"src":"https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg","width":300},"bidRequestId":"SGu1Jpq1IO","id":"1","price":3.12123}""")
      }
    }
	

  }
  //#actual-test

  //#set-up
}
//#set-up
//#campaign-routes-spec
