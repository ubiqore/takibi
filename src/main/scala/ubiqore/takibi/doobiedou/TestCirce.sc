import io.circe.parser.decode

val test = "{ \"titi\" : \"toto\" }"
val test2=decode[String](test)
