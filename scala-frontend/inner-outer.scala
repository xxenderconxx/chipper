import Direction._
import Builder._

class Inner(instanceName: String) extends Module(instanceName, "Inner") {
  val io = Data(new Bundle("io") { 
    val in = Bits("in", INPUT, 8); val out = Bits("out", OUTPUT, 8); elts ++= Array(in, out) })
  io.out := io.in + Bits(1)
}

class Outer(instanceName: String) extends Module(instanceName, "Outer") {
  val io = Data(new Bundle("io") { 
    val in = Bits("in", INPUT, 8); val out = Bits("out", OUTPUT, 8); elts ++= Array(in, out) })
  val c  = Module(new Inner("c"))
  val w = Wire(Bits("w", NO_DIR, 8))
  w := io.in
  c.io.in := w
  io.out  := c.io.out * Bits(2)
}
