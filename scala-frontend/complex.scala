import Chipper._
import Direction._
import Builder._

class Complex[T <: Data](val re: T, val im: T, dir: Direction = OUTPUT)
    extends Bundle(dir) {
  override def cloneType: this.type = 
    new Complex(re.cloneType, im.cloneType, dir).asInstanceOf[this.type]
}

class ComplexAssign(W: Int) extends Module {
  val io = new Bundle {
    val c = new Complex(Bits(width = W), Bits(width = W), INPUT)
    val d = new Complex(Bits(width = W), Bits(width = W))
    val e = new Bool(INPUT)
  }
  when (io.e) {
    val w = Wire(new Complex(Bits(width = W), Bits(width = W)))
    w := io.c
    io.d.re := w.re
    io.d.im := w.im
  } .otherwise {
    io.d.re := Bits(0)
    io.d.im := Bits(0)
  }
}
