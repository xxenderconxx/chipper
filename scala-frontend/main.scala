import Chipper._
import Direction._
import Builder._

object MiniChisel {
  def main(args: Array[String]): Unit = {
    if (args.length < 1)
      println("Need an argument")
    val emitter = new Emitter
    args(0) match {
      case "vec" => println(emitter.emit(build{ Module(new VecApp(4,8)) }))
      case "tbl" => println(emitter.emit(build{ Module(new Tbl) }))
      case "gcd" => println(emitter.emit(build{ Module(new GCD) }))
      case "outer" => println(emitter.emit(build{ Module(new Outer) }))
      case "complex" => println(emitter.emit(build{ Module(new ComplexAssign(10)) }))
      case "uintops" => println(emitter.emit(build{ Module(new UIntOps) }))
      case "sintops" => println(emitter.emit(build{ Module(new SIntOps) }))
      case "bitsops" => println(emitter.emit(build{ Module(new BitsOps) }))
    }
  }
}
