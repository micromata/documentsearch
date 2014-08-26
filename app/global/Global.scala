package global

import play.api._
import com.typesafe.config.ConfigFactory
import org.slf4j.{LoggerFactory, Logger}
import helper.ElasticSearchHelper
import java.io.File
import akka.actor.{Props, Cancellable}
import play.libs.Akka
import actors.CronActor
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

object Global extends GlobalSettings {
  private val log: Logger = LoggerFactory.getLogger(Global.getClass)
  private val conf = ConfigFactory.load()
  private var cronJob: Cancellable = null

  override def onStart(app: Application) = {
    log.info("Starting documentsearch application")
    ElasticSearchHelper.start()
    // cron actor
    val cron = Akka.system.actorOf(Props[CronActor])
    cronJob = Akka.system.scheduler.schedule(1 second, cronIntervalInSeconds seconds, cron, "sync")
  }

  override def onStop(app: Application) = {
    log.info("Stopping documentsearch application")
    cronJob.cancel()
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
  private val cronIntervalInSeconds: Int = conf.getInt("cronIntervalInSeconds")
}
