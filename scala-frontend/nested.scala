import Direction._
import Builder._

class Nested(val W: Int)  extends Module {
  val io = Data(new Bundle {
    val in = Bits(INPUT, W)
    val out = Bits(OUTPUT, W)
  })

  io.out := io.in
}

class Nester(val W: Int) extends Module {
  val io = Data(new Bundle {
    val in = Bits(INPUT, W)
    val out = Bits(OUTPUT, W)
  })

  val nest = Module(new Nested(W))
  nest.io.in := io.in
  io.out := nest.io.out
}
