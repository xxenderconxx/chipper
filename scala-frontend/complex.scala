import Chipper._
import Direction._
import Builder._

class Complex[T <: Data](dir: Direction, val re: T, val im: T) extends Bundle(dir) {
  override def cloneType: this.type = {
    println("CLONING COMPLEX")
    new Complex(dir, re.cloneType, im.cloneType).asInstanceOf[this.type]
  }
}

class ComplexAssign(W: Int) extends Module {
  val io = new Bundle {
    val c  = new Complex(INPUT, Bits(OUTPUT, W), Bits(OUTPUT, W))
    val re = new Bits(OUTPUT, W)
    val e  = new Bool(INPUT)
  }
  when (io.e) {
    val w = Wire(new Complex(NO_DIR, Bits(OUTPUT, W), Bits(OUTPUT, W)))
    w := io.c
    io.re := w.re
  } .otherwise {
    io.re := Bits(0)
  }
}
