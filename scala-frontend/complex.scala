import Chipper._
import Direction._
import Builder._

class Complex[T <: Data](val re: T, val im: T, dir: Direction = OUTPUT)
    extends Bundle(dir) {
  override def cloneType: this.type = {
    println("CLONING COMPLEX")
    new Complex(re.cloneType, im.cloneType, dir).asInstanceOf[this.type]
  }
}

class ComplexAssign(W: Int) extends Module {
  val io = new Bundle {
    val c  = new Complex(Bits(NO_DIR, W), Bits(NO_DIR, W), INPUT)
    val d = new Complex(Bits(OUTPUT, W), Bits(OUTPUT, W))
    val e  = new Bool(INPUT)
  }
  when (io.e) {
    val w = Wire(new Complex(Bits(NO_DIR, W), Bits(NO_DIR, W)))
    w := io.c
    io.d.re := w.re
    io.d.im := w.im
  } .otherwise {
    io.d.re := Bits(0)
    io.d.im := Bits(0)
  }
}
