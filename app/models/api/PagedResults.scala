package models.api

case class PagedResults[T](results: Set[T], offset: Int, limit: Int, totalCount: Int)
