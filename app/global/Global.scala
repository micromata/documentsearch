package global

import play.api._
import com.typesafe.config.ConfigFactory
import org.slf4j.{LoggerFactory, Logger}
import helper.ElasticSearchHelper
import java.io.File

object Global extends GlobalSettings {
  private val log: Logger = LoggerFactory.getLogger(Global.getClass)
  private val conf = ConfigFactory.load()

  override def onStart(app: Application) = {
    log.info("Starting documentsearch application")
    ElasticSearchHelper.start()
  }

  override def onStop(app: Application) = {
    log.info("Stopping documentsearch application")
    ElasticSearchHelper.stop()
  }

  private def replaceHome(path: String): String = {
    if (path.startsWith("~")) {
      System.getProperty("user.home") + path.substring(1)
    } else {
      path
    }
  }

  val cronCommand = conf.getString("cronCommand")
  val documentFolder = replaceHome(conf.getString("documentFolder"))
  val documentBaseDir: File = new File(documentFolder)

}
