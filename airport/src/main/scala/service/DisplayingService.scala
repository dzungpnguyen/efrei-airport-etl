// excute SQL 

package service

import scala.io.StdIn.readLine
import scala.collection.immutable.ListMap
import scala.util.Try

import model.Country
import model.Airport
import model.Runway

import service.StoringService

object DisplayingService {

  val list_countries = StoringService.getObjects("src/resources/countries.csv", Country.fromCsvLine)
  val list_airports = StoringService.getObjects("src/resources/airports.csv", Airport.fromCsvLine)
  val list_runways = StoringService.getObjects("src/resources/runways.csv", Runway.fromCsvLine)

  // mapping country code -> country name
  // if name not found, return original code
  val mapCountry = list_countries.map(_.code).zip(list_countries.map(_.name)).toMap.withDefault(i => i)

  def getUserOption: Unit =
    println("----------------------------------------")
    println("Query or Reports?")
    val userOption = readLine
    userOption.toLowerCase match
      case "query" => queryOption
      case "reports" => reportsOption
      case "exit" => exit
      case _ =>
        println("Keyword not found. Please re-enter your answer.")
        getUserOption

  def queryOption: Unit =
    println("----------------------------------------")
    println("Enter a country code or a country name:")
    // get user's answer
    val identifier = readLine
    // get user's country
    val userCountry = list_countries.filter(x => x.code.equals(identifier.toUpperCase) || x.name.toUpperCase.startsWith(identifier.toUpperCase))
    userCountry.size match
      // country not found
      case 0 =>
        // user entered 'exit'
        if (identifier.toLowerCase.equals("exit")) then
          exit
        else
          println("Country not found. Please re-enter your answer.")
          queryOption

      // one or more than one country found because of fuzzy matching
      case _ =>
        // map each country with its index
        val mapNumberCountry = userCountry.zipWithIndex.map{ case (v,i) => (i,v) }.toMap
        
        println("Please verify that your country is in the list by entering its number.")
        println("Number | Country")
        // display all possible countries
        mapNumberCountry.foreach(x => println("---" + x._1 + "---+---" + x._2.name))
        
        // get user's country's index
        val n = readLine
        // convert to Int, if not successful then assign to -1
        val num = Try(n.toInt).toOption.getOrElse(-1)
        
        // if index exists
        if num >= 0 && num < userCountry.size then
          println("Country Code | Country Name | Airport ID | Runway ID")
          println("-------------+--------------+------------+----------") 
          list_airports.foreach(airport =>
            // get airports in chosen country
            if (airport.iso_country.equals(userCountry(num).code)) then
              list_runways.foreach(runway =>
                // get runways in chosen airports
                if (runway.airport_ident.equals(airport.ident)) then
                  // display
                  printf("-- %s --|-- %s --|-- %s --|-- %s --\n", userCountry(num).code, userCountry(num).name, airport.id, runway.id)
              )
          )
        // if index doesn't exist
        else
          println("Number not found.")

        println("----------------------------------------")
        println("Query ended.\nYou can continue the program or enter 'exit' to quit.")

        // restart the program
        getUserOption

  def reportsOption: Unit =
    println("----------------------------------------")
    println("Choose your report type by entering its number:\n1. 10 countries with highest number of airports and countries with lowest number of airports.\n2. Type of runways per country.\n3. Top 10 most common runway latitude.\n*** Enter exit to end the program ***")
    val reportType = readLine
    reportType.toLowerCase match
      case "1" => topNumberOfAirports
      case "2" => runwaysType
      case "3" => mostCommonRunwayLatitude
      case "exit" => exit
      case _ =>
        println("Report type not found. Please re-enter your answer.")
        reportsOption

  def topNumberOfAirports: Unit =
    // mapping country -> count Airport
    val mapCountryCount = list_airports.map(_.iso_country).map(mapCountry(_)).groupBy(identity).mapValues(_.size)

    println("----------------------------------------")
    println("Top 10 countries with highest number of airports")
    println("Country   |   Number of airports")
    // top 10 highest
    ListMap(mapCountryCount.toSeq.sortWith(_._2 > _._2):_*).take(10).foreach(x => println(x._1 + "--+--" + x._2))

    println("----------------------------------------")
    println("Top 10 countries with lowest number of airports")
    println("Country   |   Number of airports")
    // top 10 lowest
    ListMap(mapCountryCount.toSeq.sortWith(_._2 < _._2):_*).take(10).foreach(x => println(x._1 + "--+--" + x._2))
  
  def runwaysType: Unit = println("ok")
  def mostCommonRunwayLatitude: Unit = println("ok")

  def exit: Unit =
    println("----------------------------------------")
    println("Program ended.")

}