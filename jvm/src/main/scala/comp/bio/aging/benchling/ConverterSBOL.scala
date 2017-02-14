package comp.bio.aging.benchling

import java.net.URI

import better.files.File
import fr.hmil.roshttp.HttpRequest
import io.circe.{Json, parser}
import org.sbolstandard.core2.{ComponentDefinition, SBOLReader}
// the following is equivalent to `implicit val ec = ExecutionContext.global`
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



object ConverterSBOL {

  lazy val colors = Vector(
    "#F58A5E", "#FAAC61", "#FFEF86", "#F8D3A9",
    "#B1FF67", "#75C6A9", "#B7E6D7", "#85DAE9",
    "#84B0DC", "#9EAFD2", "#C7B0E3", "#FF9CCD",
    "#D6B295", "#D59687", "#B4ABAC", "#C6C9D1",
    "#FF6699", "#FF6666", "#FF9900" ,"#999966",
    "#6666FF", "#FF5050",  "#993399", "#3366CC" //added colors
  )

  lazy val seqontology = "http://www.ebi.ac.uk/ols/api/ontologies/so/terms?iri=http://purl.obolibrary.org/obo/"

  def roleJSON(term: String): Future[Json] = {
    val path = seqontology + term.toUpperCase
    HttpRequest(path).send()(monix.execution.Scheduler.Implicits.global).flatMap {
      resp =>
        parser.parse(resp.body) match {
          case Left(f) => Future.failed(new Exception(f.getMessage()))
          case Right(result) => Future.successful(result)
        }
    }
  }

  def loadSBOLComponents(file: File): Vector[ComponentDefinition] = {
    val document = SBOLReader.read(file.toJava)
    document.getComponentDefinitions.asScala.toVector
  }

  protected def rolesWithColors(components: Seq[ComponentDefinition], colors: Seq[String]): Map[URI, (String, String)] = {
    val rolesMap = getRoleTitleMap(components)
    require(colors.length >= rolesMap.size, "we need more colors for the feature types")
    rolesMap.zip(colors).map{ case ((url, title), color) => url-> (title, color) }
  }

  def extractFeatures(components: Seq[ComponentDefinition]): Seq[BenchlingFeature] = {
    val rolesMap = rolesWithColors(components, ConverterSBOL.colors)
    for{
      comp <- components
      role <- comp.getRoles.asScala.headOption
      (roleTitle, color) = rolesMap(role)
      name = comp.getName
      sequence <- comp.getSequences.asScala.headOption //here we assume one sequence per components
      elements = sequence.getElements
      if elements.length > 3
    } yield BenchlingFeature(name, elements.toUpperCase(), roleTitle, color)
  }

  def writeFeatures(file: File, features: Seq[BenchlingFeature]): Unit = {
    import purecsv.safe._
    features.writeCSVToFile(file.toJava)
  }

  protected def getRoles(components: Seq[ComponentDefinition]): Set[URI] = {
    components.flatMap(comp=>comp.getRoles.asScala).toSet
  }

  def getRoleTitleMap(components: Seq[ComponentDefinition]) = {
    new RoleSBOLExtractor(getRoles(components)).titleMap
  }

}

// [name], [feature], [type*], [color*].
case class BenchlingFeature(name: String, feature: String, `type`: String, color: String)