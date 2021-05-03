package neo.advertcampaignbidder

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import scala.util.Failure
import scala.util.Success
import com.typesafe.config._
import scala.util.Properties

object StartBiddingApp {
 
    private val  config = ConfigFactory.load()
    def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    
    import system.executionContext
    val host: String          = config.getString("settings.http.host")
    val configPort: String    = config.getString("settings.http.port")
    val port:Int              = Properties.envOrElse("PORT",configPort).toInt
    val futureBinding         = Http().newServerAt(host, port).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }
 
  def main(args: Array[String]): Unit = {
    
  val rootBehavior = Behaviors.setup[Nothing] { context =>
       val BidRegistryActor = context.spawn(BidRegistry(), "BidRegistryActor")
       context.watch(BidRegistryActor)

       val routes = new BidRoutes(BidRegistryActor)(context.system)
       startHttpServer(routes.BidRoutes)(context.system)

       Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "BiddingHttpServer")
   
  }
}

