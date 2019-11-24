
//#quick-start-server
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success }
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
//import com.cloriko.cassandra.Cassandra
import com.cloriko.db.CassandraConfig

object HttpServer extends App {

  implicit val system: ActorSystem = ActorSystem("ClorikoServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val webRootRoutes: Route = {
    pathPrefix("hello") {
      get {
        complete("hello!")
      }
    }
  }
  lazy val routes: Route = concat(webRootRoutes)

  /*
  Future {
    val server = new MonixMasterServer()
    server.start()
    server.blockUntilShutdown()
  }*/

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "0.0.0.0", 8081)
  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)

}
