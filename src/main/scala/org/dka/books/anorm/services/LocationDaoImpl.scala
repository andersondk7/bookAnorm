package org.dka.books.anorm.services

import java.sql.Connection
import java.util.UUID
import scala.util.Try
import scala.concurrent.{ExecutionContext, Future}

import anorm.*
import anorm.SqlParser.*

import com.typesafe.scalalogging.Logger
import com.zaxxer.hikari.HikariDataSource

import org.dka.books.domain.services.*
import org.dka.books.domain.services.DaoException.DaoErrorsOr
import org.dka.books.domain.model.fields.{CountryID, ID, LocationAbbreviation, LocationName, Version}
import org.dka.books.domain.model.item.Location
import org.dka.books.anorm.services.*

class LocationDaoImpl(override val dataSource: HikariDataSource, override val dbEx: ExecutionContext)
  extends CrudDaoImpl[Location]
    with LocationDao {

  import LocationDaoImpl.*

  override val tableName = "locations"

  //
  // queries
  //
  override protected def insertQ(location: Location): SimpleSql[Row] =
    SQL(
      """
      insert into locations(id, version, location_name, location_abbreviation, country_id, create_date)
      values ({id}, {version}, {locationName}, {locationAbbreviation}, {countryId}, {createDate})
      """
    ).on(
      "id"                   -> location.id.value.toString,
      "version"              -> location.version.value,
      "locationName"         -> location.locationName.value,
      "locationAbbreviation" -> location.locationAbbreviation.value,
      "countryId"            -> location.countryID.value.toString,
      "createDate"           -> location.createDate.asTimestamp
    )

  override protected def updateQ(location: Location): SimpleSql[Row] =
    SQL("""
      update locations
      set 
        version = {version},
        location_name = {locationName},
        location_abbreviation = {locationAbbreviation},
        country_id = {countryId},
        update_date = {lastUpdate}
      where id = {id}
  """)
      .on(
        "version"              -> location.version.value,
        "locationName"         -> location.locationName.value,
        "locationAbbreviation" -> location.locationAbbreviation.value,
        "countryId"            -> location.countryID.value.toString,
        "lastUpdate"           -> location.lastUpdate.map(_.value).orNull,
        "id"                   -> location.id.value.toString
      )

  //
  // parsers
  // /
  override protected def itemParser: RowParser[Location] =
    getID ~ getVersion ~ getLocationName ~ getLocationAbbreviation ~ getCountryId ~ getCreateDate ~ getUpdateDate map {
      case id ~ v ~ ln ~ la ~ ci ~ cd ~ ud =>
        Location(
          id = id,
          version = v,
          locationName = ln,
          locationAbbreviation = la,
          countryID = ci,
          createDate = cd,
          lastUpdate = ud
        )
    }

}

object LocationDaoImpl {

  def getLocationName: RowParser[LocationName] = get[String](LocationName.fieldName).map(LocationName.build)

  def getLocationAbbreviation: RowParser[LocationAbbreviation] =
    get[String](LocationAbbreviation.fieldName).map(LocationAbbreviation.build)

  def getCountryId: RowParser[CountryID] = get[String](CountryID.fieldName).map(CountryID.build)

}
