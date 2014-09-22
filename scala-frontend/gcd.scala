import Chipper._
import Direction._
import Builder._

class GCD extends Module {
  val io = new Bundle {
    val a  = Bits(INPUT,  16)
    val b  = Bits(INPUT,  16)
    val e  = Bool(INPUT)
    val z  = Bits(OUTPUT, 16)
    val v  = Bool(OUTPUT)
  }
  val x = Reg(Bits(NO_DIR, 16))
  val y = Reg(Bits(NO_DIR, 16))
  when (x > y)   { y := y - x }
  .otherwise     { x := x - y }
  when (io.e) { x := io.a; y := io.b }
  io.z := x
  io.v := y === Bits(0)
}
