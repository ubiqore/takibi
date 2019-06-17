package ubiqore.takibi.fhir.valuesets
//import shapeless._
//import syntax.std.tuple._
//
//object ShapelessTry extends App {
//   val hlist= "a" :: true :: 2 :: HNil
//
//  // take : prendre les elements à gauche de la liste sans le param
//  println(hlist.take(Nat._2))
//  // a :: true :: HNil
//  println(hlist.drop(Nat._1)) // prendre les elements à droite en commancant par le param
//  // true :: 2 :: HNil
//  println(hlist.updatedAt(Nat._2,7))
//  // a :: true :: 7 :: HNil
//
//
//  // select finds the first element of a given type in a HList
//  // Note that scalac will correctly infer the type of s to be String.
//  println(hlist.select[String]) // returns "a".
//
//  // println(hlist.select[List[Double]]) // Compilation error. hlist does not contain a List[Int]
//
//  // Again i is correctly inferred as Int
//  println(hlist.head) // returns "a".
//
//  // You can also us pattern matching on HList
//  val s :: b :: i :: HNil = hlist
//  println(s + " " + b + " "+ i ) // a true 2
//
//  trait Fruit
//  case class Apple() extends Fruit
//  case class Pear() extends Fruit
//
//  type FFFF = Fruit :: Fruit :: Fruit :: Fruit :: HNil
//  type APAP = Apple :: Pear :: Apple :: Pear :: HNil
//
//  val a: Apple = Apple()
//  val p: Pear = Pear()
//
//  val apap: APAP = a :: p :: a :: p :: HNil
//  println(apap.toList)
//
//
//  println((23, "foo", true).drop(2))
//
//  type ISB = Int :+: String :+: Boolean :+: CNil
//
//  val isb = Coproduct[ISB]("foo")
//
//  println(isb.select[Int])
//}
