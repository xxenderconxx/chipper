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
    val noteqout = Bool(OUTPUT)
    val lesseqout = Bool(OUTPUT)
    val greateqout = Bool(OUTPUT)
  }

  val a = io.a
  val b = io.b

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
  io.noteqout := a != b
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
    val noteqout = Bool(OUTPUT)
    val lesseqout = Bool(OUTPUT)
    val greateqout = Bool(OUTPUT)
    val negout = SInt(OUTPUT, 16)
  }

  val a = io.a
  val b = io.b

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
  io.noteqout := a != b
  io.lesseqout := a <= b
  io.greateqout := a >= b
  io.negout := -a
}

class BitsOps extends Module {
  val io = new Bundle {
    val a = Bits(INPUT, 16)
    val b = Bits(INPUT, 16)
    val notout = Bits(OUTPUT, 16)
    val andout = Bits(OUTPUT, 16)
    val orout = Bits(OUTPUT, 16)
    val xorout = Bits(OUTPUT, 16)
  }

  io.notout := ~io.a
  io.andout := io.a & io.b
  io.orout := io.a | io.b
  io.xorout := io.a ^ io.b
}
