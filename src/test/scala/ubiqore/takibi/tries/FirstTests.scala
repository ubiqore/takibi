package ubiqore.takibi.tries

import org.scalatest._


class FirstTests extends FlatSpec {

  "An empty Set" should "have size 0" in {
    assert(Set.empty.size == 0)
  }


  it should "produce NoSuchElementException when head is invoked" in {
    assertThrows[NoSuchElementException] {
      Set.empty.head
    }
  }

  val toto=Option(1)

  "an Option(1" should "be an instance of Some(1)  " in {
     assert(toto.isInstanceOf[Some[Int]] == true)
  }

}

