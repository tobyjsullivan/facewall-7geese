package models.api

import play.api.libs.ws._
import play.api.libs.json._
import play.api.Play.current

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object GeeseApi {
  val oauthUrl = "https://www.7geese.com/oauth2/"
  val authorizeUrl = oauthUrl + "authorize/"

  val apiUrl = "https://www.7geese.com/api/"
  val userProfilesUrl = apiUrl + "v1/userprofiles/"

  val clientId: String = play.api.Play.application.configuration.getString("geese.clientid").get
  val clientSecret: String = play.api.Play.application.configuration.getString("geese.clientsecret").get


  def exchangeCodeForToken(code: String): Future[String] = {
    val holder: WSRequestHolder = WS.url(oauthUrl + "access_token/").withQueryString(
      "grant_type" -> "authorization_code",
      "client_id" -> clientId,
      "client_secret" -> clientSecret,
      "code" -> code
    )

    val fResponse = holder.post("")



    fResponse.map { wsResponse =>
//      val jsResp = Json.parse(wsResponse.body)
//
//      val optError = (jsResp \ "error").asOpt[String]
//
//      val optToken = (jsResp \ "token").asOpt[String]
//
//      (optToken, optError) match {
//        case (Some(token), _) => token
//        case (None, Some(error)) => throw new Exception("Error fetching access token: "+error)
//        case (None, None) => throw new Exception("Error finding data in response: "+wsResponse.body)
//      }

      wsResponse.body
    }
  }

  def validateToken(token: String): Future[Boolean] = {
    // The only way to validate a token is to make a standard request. Slow :(
    val holder = WS.url(userProfilesUrl).withQueryString("oauth_consumer_key" -> token)

    val fResp = holder.get()

    fResp.map(response =>
      response.status == 200
    )
  }

  def getAllEmployees(token: String): Future[Set[Employee]] = {
    val pageLimit = 20

    val futureFirstSet = getEmployees(token, 0, pageLimit)

    // We have to wait for the first set to know how many more pages to fetch
    val finalRes = futureFirstSet.flatMap {
      case pagedResults: PagedResults[Employee] => {
        val totalCount = pagedResults.totalCount

        val remainingSets = for(
          offset <- (0 to totalCount by pageLimit).toSet[Int]
        ) yield getEmployees(token, offset, pageLimit).map(_.results)

        // Reduce from Set[Future[Set[...]]] to Future[Set[...] ++ Set[...] ++ ...]
        Future.reduce(remainingSets) { (_: Set[Employee]) ++ (_: Set[Employee]) }
      }
    }

    finalRes
  }

  private def getEmployees(token: String, offset: Int, limit: Int): Future[PagedResults[Employee]] = {
    val holder = WS.url(userProfilesUrl).withQueryString(
      "offset" -> offset.toString,
      "limit" -> limit.toString,
      "oauth_consumer_key" -> token
    )

    val fResp = holder.get()

    fResp.map { response =>
      response.status match {
        case 200 => {val jsResp = Json.parse(response.body)

          val totalCount = (jsResp \ "meta" \ "total_count").as[Int]

          val objects: Set[Employee] = (jsResp \ "objects").as[Set[Employee]]

          PagedResults(objects, offset, limit, totalCount)
        }
        case status => throw new Exception("Unexpected response from userprofile endpoint: "+status)
      }
    }
  }
}
