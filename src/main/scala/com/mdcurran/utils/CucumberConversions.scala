package com.mdcurran.utils

import cucumber.api.DataTable
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DataType, DataTypes, StructField, StructType}

import scala.collection.JavaConversions._

object CucumberConversions {

  def extractColumns(data: DataTable): List[(String, DataType)] = {
    data.topCells()
      .map(_.split(":"))
      .map(splits => (splits(0), splits(1).trim.toLowerCase))
      .map {
        case (name, "string") => (name, DataTypes.StringType)
        case (name, "int") => (name, DataTypes.IntegerType)
        case (name, _) => (name, DataTypes.StringType)
      }.toList
  }

  def deriveSchema(columns: List[(String, DataType)]): StructType = {
    StructType(columns.map {
      case (name, dataType) => StructField(name, dataType)
    })
  }

  def extractRows(data: DataTable, schema: StructType): List[Row] = {
    data.asMaps(classOf[String], classOf[String])
      .map { row =>
        val values = row
          .values()
          .zip(extractColumns(data))
          .map { case (v, (_, dt)) => (v, dt) }
          .map {
            case (v, DataTypes.StringType) => v
            case (v, DataTypes.IntegerType) => v.toInt
          }.toSeq
        Row.fromSeq(values)
      }.toList
  }

}