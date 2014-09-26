import Chipper._
import Direction._
import Builder._

class DirChange extends Module {
  val io = new Bundle {
    val test1 = UInt(INPUT, 5).asOutput
    val test2 = UInt(OUTPUT, 5).asInput
    val test3 = Vec.fill(10){ UInt(OUTPUT, 2) }.flip
    val test4 = new Bundle {
      val test41 = SInt(INPUT, 5)
      val test42 = SInt(OUTPUT, 5)
    }.asInput
  }.flip
}
