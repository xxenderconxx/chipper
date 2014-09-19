import Direction._
import Builder._

object MiniChisel {
  def main(args: Array[String]): Unit = {
    val emitter = new Emitter
    println(emitter.emit(build{ Module(new Tbl) }))
    // println(emitter.emit(build{ Module(new Outer) }))
    // println(emitter.emit(build{ Module(new GCD) }))
  }
}
