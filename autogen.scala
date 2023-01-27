import scala.io.Source

val errorsRaml = Source.fromFile("/Users/tomlloyd/workspace/mtd-api/new-services/individuals-income-received-api/resources/public/api/conf/1.0/errors.raml").getLines().toList
val errorsScala = Source.fromFile("/Users/tomlloyd/workspace/mtd-api/new-services/individuals-income-received-api/app/api/models/errors/mtdErrors.scala").getLines().toList

val newErrorIndices = errorsRaml.zipWithIndex.filter(_._1.matches("^  [A-Za-z]+:$")).map(_._2)

println(newErrorIndices)

case class Error(name: String, description: String, code: String)