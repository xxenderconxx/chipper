import Direction._
import Builder._

class GCD(instanceName: String) extends Module(instanceName, "GCD") {
  val io = Data(new Bundle("io") {
    val a  = Bits("a", INPUT,  16)
    val b  = Bits("b", INPUT,  16)
    val e  = Bool("e", INPUT)
    val z  = Bits("z", OUTPUT, 16)
    val v  = Bool("v", OUTPUT)
  })
  val x = Reg(Bits("x", NO_DIR, 16))
  val y = Reg(Bits("y", NO_DIR, 16))
  when (x > y)   { y := y - x } 
  .otherwise     { x := x - y }
  when (io.e) { x := io.a; y := io.b }
  io.z := x
  io.v := y === Bits(0)
}

