import Direction._
import Builder._

class Inner(instanceName: String) extends Module(instanceName) {
  val io = Data(new Bundle {
    val in = Bits(INPUT, 8)
    val out = Bits(OUTPUT, 8)
  })
  io.out := io.in + Bits(1)
}

class Outer(instanceName: String) extends Module(instanceName) {
  val io = Data(new Bundle { 
    val in = Bits(INPUT, 8)
    val out = Bits(OUTPUT, 8)
  })
  val c  = Module(new Inner("c"))
  val w = Wire(Bits(NO_DIR, 8))
  w := io.in
  c.io.in := w
  io.out  := c.io.out * Bits(2)
}
