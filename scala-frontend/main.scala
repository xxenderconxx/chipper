import Direction._
import Builder._

object MiniChisel {
  def main(args: Array[String]): Unit = {
    if (args.length < 1)
      println("Need an argument")
    val emitter = new Emitter
    args(0) match {
      case "tbl" => println(emitter.emit(build{ Module(new Tbl) }))
      case "gcd" => println(emitter.emit(build{ Module(new GCD) }))
      case "outer" => println(emitter.emit(build{ Module(new Outer) }))
      case "complex" => println(emitter.emit(build{ Module(new ComplexAssign(10)) }))
    }
  }
}
