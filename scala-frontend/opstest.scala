import Chipper._
import Direction._
import Builder._

class UIntOps extends Module {
  val io = new Bundle {
    val a = UInt(INPUT, 16)
    val b = UInt(INPUT, 16)
    val addout = UInt(OUTPUT, 16)
    val subout = UInt(OUTPUT, 16)
    val timesout = UInt(OUTPUT, 16)
    val divout = UInt(OUTPUT, 16)
    val modout = UInt(OUTPUT, 16)
    val lshiftout = UInt(OUTPUT, 16)
    val rshiftout = UInt(OUTPUT, 16)
    val lessout = Bool(OUTPUT)
    val greatout = Bool(OUTPUT)
    val eqout = Bool(OUTPUT)
    val lesseqout = Bool(OUTPUT)
    val greateqout = Bool(OUTPUT)
  }

  val a = Wire(UInt(NO_DIR, 16))
  val b = Wire(UInt(NO_DIR, 16))

  io.a := a
  io.b := b

  io.addout := a + b
  io.subout := a - b
  io.timesout := a * b
  io.divout := a / b
  io.modout := a % b
  io.lshiftout := a << b
  io.rshiftout := a >> b
  io.lessout := a < b
  io.greatout := a > b
  io.eqout := a === b
  io.lesseqout := a <= b
  io.greateqout := a >= b
}

class SIntOps extends Module {
  val io = new Bundle {
    val a = SInt(INPUT, 16)
    val b = SInt(INPUT, 16)
    val addout = SInt(OUTPUT, 16)
    val subout = SInt(OUTPUT, 16)
    val timesout = SInt(OUTPUT, 16)
    val divout = SInt(OUTPUT, 16)
    val modout = SInt(OUTPUT, 16)
    val lshiftout = SInt(OUTPUT, 16)
    val rshiftout = SInt(OUTPUT, 16)
    val lessout = Bool(OUTPUT)
    val greatout = Bool(OUTPUT)
    val eqout = Bool(OUTPUT)
    val lesseqout = Bool(OUTPUT)
    val greateqout = Bool(OUTPUT)
  }

  val a = Wire(SInt(NO_DIR, 16))
  val b = Wire(SInt(NO_DIR, 16))

  io.a := a
  io.b := b

  io.addout := a + b
  io.subout := a - b
  io.timesout := a * b
  io.divout := a / b
  io.modout := a % b
  io.lshiftout := a << b
  io.rshiftout := a >> b
  io.lessout := a < b
  io.greatout := a > b
  io.eqout := a === b
  io.lesseqout := a <= b
  io.greateqout := a >= b
}
