import Direction._
import Builder._

class Inner extends Module {
  val io = new Bundle {
    val in = Bits(INPUT, 8)
    val out = Bits(OUTPUT, 8)
  }
  io.out := io.in + Bits(1)
}

class Outer extends Module {
  val io = new Bundle { 
    val in = Bits(INPUT, 8)
    val out = Bits(OUTPUT, 8)
  }
  val c = Module(new Inner)
  val w = Wire(Bits(NO_DIR, 8))
  w := io.in
  c.io.in := w
  io.out  := c.io.out * Bits(2)
}
