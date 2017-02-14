package comp.bio.aging.benchling

import cats.Foldable
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import cats.implicits._
import java.net._

object RoleSBOLExtractor{
  def apply(urls: Iterable[String]) ={
    new RoleSBOLExtractor(urls.map(u=>new URI(u)))
  }
}

class RoleSBOLExtractor(urls: Iterable[URI]) {
  val browser = JsoupBrowser()

  def extract(url: String): Option[String] = {
    val doc = browser.get(url)
    doc >?> text("[property=name]")
  }

  lazy val titleMap: Map[URI, String] = urls.map(u=> u-> extract(u.toString).getOrElse(u.getPath)).toMap
}


