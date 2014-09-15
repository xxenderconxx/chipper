import Direction._
import Builder._

object MiniChisel {
  def main(args: Array[String]): Unit = {
    val emitter = new Emitter
    // println(emitter.emit(build{ Module(new Outer("main")) }))
    println(emitter.emit(build{ Module(new GCD("gcd")) }))
  }
}
