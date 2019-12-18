package pl.com.britenet.nodes

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import net.liftweb.json.{DefaultFormats, Serialization}

import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route =
      path("hello") {
        get {
          val reader = new Reader()
          val orphanage = new Orphanage()
          val families = orphanage.createFamilies(reader.read(), 3)

          implicit val formats: DefaultFormats.type = DefaultFormats

          complete(HttpEntity(ContentTypes.`application/json`, Serialization.writePretty(families)))
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
