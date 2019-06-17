package ubiqore.takibi.csv.spec.version1
import classy.generic._
import classy.config._
import com.typesafe.config.{Config, ConfigFactory}

object QuickApplication {
  val configString = """
  people = [
    { firstName : Augusta,
      lastName  : Ada },

    { firstName : Donald,
      lastName  : Knuth },

    { firstName : Grace,
      lastName  : Hopper }
  ]"""

  // we're going to load a list of people and some trivial actions from configuration data
  case class Options(
                      people : List[Person],
                      actions: List[Action])

  case class Person(
                     firstName: String,
                     lastName: String)

  sealed abstract class Action extends Product with Serializable
  case class Dance(dance: String) extends Action
  case class Sing(song: String) extends Action
  case class Shout(expletive: String) extends Action

  // main method that might abort if the configuration is invalid
  def main(args: Array[String]): Unit = {

    val rawConfig = ConfigFactory.parseString(configString)

    val decoder = deriveDecoder[Config,Options]

    println(decoder.decode(rawConfig))
    // decoder.load() is just a shortcut for Typesafe's ConfigFactory.load()
    decoder.load() match {

      case Left(error) =>
        // loading the config failed! this will print a reasonable error message
        System.err.println("config error: " + error)
        System.exit(-1)

      case Right(options) =>
        main(options)
    }
  }

  // main method that is only called with valid config
  def main(options: Options): Unit = {

    // assembly messages of everyone doing every action
    val res: List[String] = for {
      person <- options.people
      action <- options.actions
      text    = action match {
        case Dance(dance) => s"dances $dance"
        case Sing(song)   => s"sings $song"
        case Shout(word)  => s"shouts $word at the top of his/her lungs"
      }

    } yield s"$person $text"

    // end of the world: print
    res.foreach(println)
  }

}
