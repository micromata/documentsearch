package controllers

import play.api._
import play.api.mvc._
import helper.ElasticSearchSearchHelper

object Application extends Controller {

  def index = Action {
    implicit request =>
      Ok(views.html.index())
  }

  def search(query: String) = Action {
    implicit request =>
      Ok(views.html.index(ElasticSearchSearchHelper.search(query), query))
  }

}