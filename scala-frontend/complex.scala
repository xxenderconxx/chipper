import Chipper._
import Direction._
import Builder._

class Complex[T <: Data](val re: T, val im: T) extends Bundle {
  override def cloneType: this.type = {
    println("CLONING COMPLEX")
    new Complex(re.cloneType, im.cloneType).asInstanceOf[this.type]
  }
}

class ComplexAssign(W: Int) extends Module {
  val io = new Bundle {
    val c  = new Complex(Bits(INPUT, W), Bits(INPUT, W))
    val re = new Bits(OUTPUT, W)
    val e  = new Bool(INPUT)
  }
  when (io.e) {
    val w = Wire(new Complex(Bits(NO_DIR, W), Bits(NO_DIR, W)))
    w := io.c
    io.re := w.re
  } .otherwise {
    io.re := Bits(0)
  }
}
