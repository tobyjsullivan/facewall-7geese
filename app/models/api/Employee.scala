package models.api

import play.api.libs.json._
import play.api.libs.functional.syntax._

object Employee {
  implicit val employeeWrites: Writes[Employee] = (
    (JsPath \ "id").write[Int] and
    (JsPath \ "email").write[String] and
    (JsPath \ "firstName").write[String] and
    (JsPath \ "lastName").write[String] and
    (JsPath \ "role").write[String]
  )(unlift(Employee.unapply))

  implicit val employeeReads: Reads[Employee] = (
      (JsPath \ "id").read[Int] and
      (JsPath \ "user" \ "email").read[String] and
      (JsPath \ "user" \ "first_name").read[String] and
      (JsPath \ "user" \ "last_name").read[String] and
      (JsPath \ "position").read[String]
    )(Employee.apply _)

}

case class Employee(id: Int, email: String, firstName: String, lastName: String, role: String)


