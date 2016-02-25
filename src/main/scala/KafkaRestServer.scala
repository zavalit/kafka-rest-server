import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.IOException
import java.util.concurrent.{Future => JFuture}
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.math._
import spray.json.DefaultJsonProtocol
import org.apache.kafka.clients.producer._
import java.util.Properties
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{`Access-Control-Max-Age`, `Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.model.headers.Origin
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.MethodRejection
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.model._
import scala.concurrent.duration._

case class Msg(location: String)
case class MsgRequest(body: Msg)

trait Protocol extends DefaultJsonProtocol {
  implicit val MsgFormat = jsonFormat1(Msg.apply)
}



trait CorsSupport  extends TopicProducer{

  protected def corsAllowOrigins: List[String]

  protected def corsAllowedHeaders: List[String]

  protected def corsAllowCredentials: Boolean

  protected def optionsCorsHeaders: List[HttpHeader]

  protected def corsRejectionHandler(allowOrigin: `Access-Control-Allow-Origin`) = RejectionHandler
    .newBuilder().handle {
      case MethodRejection(supported) =>
        complete(HttpResponse().withHeaders(

        ))
    }
    .result()

  private def originToAllowOrigin(origin: Origin): Option[`Access-Control-Allow-Origin`] =
    if (corsAllowOrigins.contains("*") || corsAllowOrigins.contains(origin.value))
      origin.origins.headOption.map(`Access-Control-Allow-Origin`.apply)
    else
      None

  def cors[T]: Directive0 = mapInnerRoute { route => context =>

    ((context.request.method, context.request.header[Origin].flatMap(originToAllowOrigin)) match {
      case (OPTIONS, Some(allowOrigin)) =>
        handleRejections(corsRejectionHandler(allowOrigin)) {
          respondWithHeaders(allowOrigin, `Access-Control-Allow-Credentials`(corsAllowCredentials), `Access-Control-Allow-Headers`(corsAllowedHeaders.mkString(", "))) {
            route
          }
        }
      case (_, Some(allowOrigin)) =>
        respondWithHeaders(allowOrigin, `Access-Control-Allow-Credentials`(corsAllowCredentials)) {

          Unmarshal(context.request.entity).to[String].flatMap {
            entity =>
             produce(entity)
             Future.successful(Left(s"$entity: incorrect IP format"))
          }


          route
        }
      case (_, _) =>
        route
    })(context)
  }
}

trait TopicProducer extends Protocol {
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def produce(msg: String) = {

    val props = new java.util.Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("client.id", "KafkaProducer")
    props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val topic = s"test-topic"

    val now  = java.util.Calendar.getInstance().getTime()
    val producer = new KafkaProducer[Integer, String](props)
    val record = new ProducerRecord[Integer, String](topic, 1, s"$msg")
    val metaF: JFuture[RecordMetadata] = producer.send(record)
    val meta = metaF.get() // blocking!
    val msgLog =
      s"""
         |offset    = ${meta.offset()}
         |partition = ${meta.partition()}
         |topic     = ${meta.topic()}
       """.stripMargin
    producer.close()
    msgLog
  }
}

object KafkaRestServer extends App with CorsSupport{

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()


  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  override val corsAllowOrigins: List[String] = List("*")

  override val corsAllowedHeaders: List[String] = List("Origin", "X-Requested-With", "Content-Type", "Accept", "Accept-Encoding", "Accept-Language", "Host", "Referer", "User-Agent")

  override val corsAllowCredentials: Boolean = true

  override val optionsCorsHeaders: List[HttpHeader] = List[HttpHeader](
   `Access-Control-Allow-Headers`(corsAllowedHeaders.mkString(", ")),
   `Access-Control-Max-Age`(60 * 60 * 24 * 20), // cache pre-flight response for 20 days
   `Access-Control-Allow-Credentials`(corsAllowCredentials)
 )

 val routes = cors {
      complete {
         "pong"
       }

 }

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
