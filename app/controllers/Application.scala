package controllers

import models.api.{Employee, GeeseApi}
import models.api.Employee._
import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.http.{Status => HttpStatus}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  private lazy val hostUrl: String = "https://" + ??? + "/"
  private val tokenCookie = "7geeseToken"
  private val postLoginRedirect = "/facewall"

  def index = Action.async { request =>

    // Try reading auth token from cookies
    val optToken = request.cookies.get(tokenCookie).map(_.value)

    optToken.map(GeeseApi.validateToken) match {
      case None => Future(Redirect("/login", HttpStatus.TEMPORARY_REDIRECT))
      case Some(fValid) =>
        // We have a token cookie, so validate it's still legit
        fValid.map {
          if(_) Redirect(postLoginRedirect, HttpStatus.TEMPORARY_REDIRECT)
          else Redirect("/login", HttpStatus.TEMPORARY_REDIRECT)
        }
    }

  }

  def login = Action { request =>
    Redirect(
      url = GeeseApi.authorizeUrl,
      queryString = Map(
        "client_id" -> Seq(GeeseApi.clientId),
        "response_type" -> Seq("code"),
        "scope" -> Seq(),
        "redirect_uri" -> Seq(hostUrl + "login-callback")
      ),
      status = HttpStatus.TEMPORARY_REDIRECT
    )
  }

  def loginCallback(optCode: Option[String], optError: Option[String]) = Action.async { request =>
    (optCode, optError) match {
      case (None, None) => Future(BadRequest("Callback must include one of 'code' or 'error'"))
      case (None, Some(error)) => Future(Unauthorized("Unable to sign in to 7geese: "+ error))
      case (Some(code), _) =>
        // Exchange code for token
        val fToken = GeeseApi.exchangeCodeForToken(code)

        fToken.map { token =>
          Redirect(postLoginRedirect, HttpStatus.TEMPORARY_REDIRECT).withCookies(
            Cookie(tokenCookie, token)
          )
        }
    }
  }

  def employees = Action.async { request =>

    request.cookies.get(tokenCookie).map(_.value) match {
      case None => Future(Redirect("/login", HttpStatus.TEMPORARY_REDIRECT))
      case Some(token) =>
        val fEmployees = GeeseApi.getAllEmployees(token)

        fEmployees.map { employees =>
          Ok(Json.toJson(Map("users" -> employees)))
        }
    }



  }

  def facewall = Action {
    Ok("Hi there!")

  }
}