package edu.knoldus.introduction.sql

import org.apache.spark.{SparkConf, SparkContext}

object DFWithRDDReflection extends App {

  val conf = new SparkConf().setAppName("BigApple").setMaster("local")
  val sc = new SparkContext(conf)
  val sqlContext = new org.apache.spark.sql.SQLContext(sc)

  // this is used to implicitly convert an RDD to a DataFrame.

  case class Address(city: String, state: String, country: String)
  case class Person(name: String, age: Int, add: Address)


  //create an RDD from a file

  val peopleRDD = sc.textFile("src/main/resources/people.txt")

  val peopleSplitRDD = peopleRDD.map(_.split(","))
  val peopleRDDExtractPerson = peopleSplitRDD.map(p => Person(p(0), p(1).trim.toInt, Address(p(2), p(3), p(4))))

  // this is used to implicitly convert an RDD to a DataFrame.
  import sqlContext.implicits._

  val peopleDF = peopleRDDExtractPerson.toDF()

  peopleDF foreach println

  peopleDF.registerTempTable("people")

  // SQL statements can be run by using the sql methods provided by sqlContext.
  val teenagers = sqlContext.sql("SELECT name, age FROM people WHERE age >= 13 AND age <= 19")
  val inPune = sqlContext.sql("SELECT * FROM people where add.city='Pune'")


  inPune foreach println

  // The results of SQL queries are DataFrames and support all the normal RDD operations.

  // The columns of a row in the result can be accessed by field index:
  teenagers.map(t => "Name: " + t(0)) foreach println

  // or by field name:
  teenagers.map(t => "Name: " + t.getAs[String]("name")).collect().foreach(println)

  // row.getValuesMap[T] retrieves multiple columns at once into a Map[String, T]

  teenagers.map(_.getValuesMap[Any](List("name", "age"))).collect().foreach(println)
  // Map("name" -> "Justin", "age" -> 19)


}
