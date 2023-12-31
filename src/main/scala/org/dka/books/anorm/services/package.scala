package org.dka.books.anorm.services

import anorm.*
import anorm.SqlParser.*
import org.dka.books.domain.model.fields.{CreateDate, ID, LocationID, UpdateDate, Version}

import java.time.LocalDateTime

def getID: RowParser[ID] = get[String](ID.fieldName).map(ID.build)

def getVersion: RowParser[Version] = get[Int](Version.fieldName).map(Version.build)

def getCreateDate: RowParser[CreateDate] = get[LocalDateTime](CreateDate.fieldName).map(CreateDate.build)

def getUpdateDate: RowParser[Option[UpdateDate]] =
  get[Option[LocalDateTime]](UpdateDate.fieldName).map(UpdateDate.build)

def getLocationId: RowParser[Option[LocationID]] = get[Option[String]](LocationID.fieldName).map(LocationID.fromOpt)
