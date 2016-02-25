import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Flow
import org.scalatest._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{`Origin`, `Access-Control-Allow-Headers`}

class ServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with CorsSupport {
  override def testConfigSource = "akka.loglevel = WARNING"
  override val corsAllowOrigins: List[String] = List("*")
  override val corsAllowedHeaders: List[String] = List("*")
  override val corsAllowCredentials: Boolean = true
  override val optionsCorsHeaders: List[HttpHeader] =
    List[HttpHeader](`Access-Control-Allow-Headers`(corsAllowedHeaders.mkString(", ")))
  val msg = Msg("{}")


  it should "respond to ping request with pong" in {
    check {
      OK shouldBe OK
    }
  }
}
