import Direction._
import Builder._

class Tbl extends Module {
  val io = Data(new Bundle {
    val  i  = Bits(INPUT,  16)
    val we  = Bool(INPUT)
    val  d  = Bits(INPUT,  16)
    val  o  = Bits(OUTPUT, 16)
  })
  val m = Mem(Bits(NO_DIR, 10), 256)
  io.o := Bits(0)
  when (io.we) { m(io.i) := io.d }
  .otherwise   { io.o := m(io.i) }
}
