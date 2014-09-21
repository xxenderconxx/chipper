import Direction._
import Builder._

class Complex(dir: Direction, w: Int) extends Bundle {
  val re = Bits(dir, w)
  val im = Bits(dir, w)
}

class ComplexAssign(W: Int) extends Module {
  val io = new Bundle {
    val c = new Complex(INPUT, W)
    val re = new Bits(OUTPUT, W)
    val e = new Bool(INPUT)
  }
  when (io.e) {
    val w = Wire(new Complex(NO_DIR, W))
    w := io.c
    io.re := w.re
  } .otherwise {
    io.re := Bits(0)
  }
}
