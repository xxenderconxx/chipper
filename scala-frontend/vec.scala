import Chipper._
import Direction._
import Builder._

class VecApp(n: Int, W: Int) extends Module {
  val io = new Bundle {
    val i = Vec.fill(n, INPUT){ Bits(OUTPUT, W) }
    val o = Vec.fill(n, OUTPUT){ Bits(OUTPUT, W) }
  }
  // for (j <- 0 until n)
  //   io.o(j) := io.i(j)
  val w = Wire(Vec.fill(n, NO_DIR){ Bits(NO_DIR, W) })
  w := io.i
  io.o := w
  // io.o := io.i
}
