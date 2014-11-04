package models.api

import play.api.libs.json._
import play.api.libs.functional.syntax._

object Employee {
  implicit val employeeWrites: Writes[Employee] = (
    (JsPath \ "id").write[Int] and
    (JsPath \ "email").write[String] and
    (JsPath \ "firstName").write[String] and
    (JsPath \ "lastName").write[String] and
    (JsPath \ "role").writeNullable[String] and
    (JsPath \ "activated").write[Boolean] and
    (JsPath \ "isActive").write[Boolean]
  )(unlift(Employee.unapply))

  implicit val employeeReads: Reads[Employee] = (
      (JsPath \ "id").read[Int] and
      (JsPath \ "user" \ "email").read[String] and
      (JsPath \ "user" \ "first_name").read[String] and
      (JsPath \ "user" \ "last_name").read[String] and
      (JsPath \ "position").readNullable[String] and
      (JsPath \ "activated").read[Boolean] and
      (JsPath \ "user" \ "is_active").read[Boolean]
    )(Employee.apply _)

}

case class Employee(id: Int, email: String, firstName: String, lastName: String, role: Option[String], activated: Boolean, isActive: Boolean)


