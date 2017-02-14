package comp.bio.aging.benchling

import fr.hmil.roshttp.HttpRequest
import monix.execution.Scheduler.Implicits.global
import scala.util.{Failure, Success}
import fr.hmil.roshttp.response.SimpleHttpResponse


object Benchling extends BenchlingAPI{

}

trait BenchlingAPI {

  lazy val key = ""

  //HttpRequest("long.source.of/data").withHeader("Authorization")


}