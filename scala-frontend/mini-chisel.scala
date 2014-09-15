import scala.collection.mutable.{ArrayBuffer, Stack, HashSet}

/// TODO:
/// uint, sint -- copy current implementation
/// optimize when 
/// unique id -> named using introspection
/// memories -- need to gen accessors and connects
/// vecs -- need to figure this out
/// init on regs -- need to figure this out
/// get rid of this
/// tester -- need internal access
/// multiple clock domains -- 
/// 

case class PrimOp(val name: String) {
  override def toString = name
}

object PrimOp {
  val AddOp = PrimOp("Add");
  val MinusOp = PrimOp("Minus");
  val TimesOp = PrimOp("Times");
  val DivideOp = PrimOp("Divide");
  val ModOp = PrimOp("Mod");
  val ShiftLeftOp = PrimOp("ShiftLeft");
  val ShiftRightOp = PrimOp("ShiftRight");
  val AndOp = PrimOp("And");
  val OrOp = PrimOp("Or");
  val BitAndOp = PrimOp("BitAnd");
  val BitOrOp = PrimOp("BitOr");
  val BitXorOp = PrimOp("BitXor");
  val ConcatOp = PrimOp("Concat");
  val BitSelectOp = PrimOp("Select");
  val LessOp = PrimOp("Less");
  val LessEqOp = PrimOp("LessEq");
  val GreaterOp = PrimOp("Greater");
  val GreaterEqOp = PrimOp("GreaterEq");
  val EqualOp = PrimOp("Equal");
  val NotOp = PrimOp("Not");
  val MultiplexOp = PrimOp("Multiplex");
}
import PrimOp._

abstract class Immediate;
case class Ref(val name: String, val kind: Type = UnknownType()) extends Immediate;
case class Field(val imm: Immediate, val name: String, val kind: Type = UnknownType()) extends Immediate;

case class Port(val name: String, val dir: Direction, val kind: Type);

abstract class Width;
case class UnknownWidth() extends Width;
case class IntWidth(val value: Int) extends Width;

abstract class Type;
case class UnknownType extends Type;
case class IntType(val width: Width) extends Type;
case class SIntType(val width: Width) extends Type;
case class BundleType(val ports: Array[Port]) extends Type;

abstract class Command;
abstract class Definition extends Command {
  def name: String
}
case class DefInt(val name: String, val value: Int) extends Definition;
case class DefSInt(val name: String, val value: Int) extends Definition;
case class DefPrim(val name: String, val op: PrimOp, val args: Array[Immediate]) extends Definition;
case class DefWire(val name: String, val kind: Type) extends Definition;
case class DefRegister(val name: String, val kind: Type) extends Definition;
case class DefMemory(val name: String, val kind: Type, val size: Int) extends Definition;
case class DefVector(val name: String, val kind: Type, val args: Array[Immediate]) extends Definition;
case class DefAccessor(val name: String, val source: String, val direction: Direction, val index: Immediate) extends Definition;
case class DefInstance(val name: String, val module: String) extends Definition;
case class Conditionally(val pred: Immediate, val conseq: Command, val alt: Command) extends Command;
case class Begin(val body: Array[Command]) extends Command();
case class Connect(val loc: Immediate, val exp: Immediate) extends Command;
case class EmptyCommand() extends Command;

case class Component(val name: String, val ports: Array[Port], val body: Command);
case class Circuit(val components: Array[Component], val main: String);

/// COMPONENTS

case class Direction(val name: String) {
  override def toString = name
}
object Direction {
  val INPUT  = new Direction("input")
  val OUTPUT = new Direction("output")
  val NO_DIR = new Direction("?")
}
import Direction._

class GenSym {
  var counter = -1
  def next(name: String): String = {
    counter += 1
    name + "_" + counter
  }
}

object Builder {
  val components = new ArrayBuffer[Component]()
  var moduleNames = new HashSet[String]
  var moduleGenSym = new GenSym
  def genModuleName(name: String): String = {
    if (moduleNames.contains(name))
      moduleGenSym.next(name)
    else {
      moduleNames(name) = true
      name
    }
  }
  val genSyms = new Stack[GenSym]()
  genSyms.push(new GenSym())
  val scopes = new Stack[HashSet[String]]()
  def genSym = genSyms.top
  def scope = scopes.top
  def genVarName(name: String): String = {
    if (scope.contains(name))
      genSym.next(name)
    else {
      scope(name) = true
      name
    }
  }
  def pushScope = {
    scopes.push(new HashSet[String]())
    genSyms.push(new GenSym)
  }
  def popScope = {
    scopes.pop()
    genSyms.pop()
  }
  val modules = new Stack[Module]()
  def pushModule(module: Module) = modules.push(module)
  def popModule() = modules.pop()
  val datas = new Stack[Data]()
  datas.push(null)
  def pushData(data: Data) = datas.push(data)
  def popData() = datas.pop()
  // val immediates = new Stack[Immediate]()
  // def pushImmediate() = immediates.push()
  // def popImmediate() = immediates.pop()
  val commandz = new Stack[ArrayBuffer[Command]]()
  def commands = commandz.top
  def pushCommand(cmd: Command) = commands += cmd
  def commandify(cmds: ArrayBuffer[Command]): Command = {
    if (cmds.length == 0)
      EmptyCommand()
    else if (cmds.length == 1)
      cmds(0)
    else
      Begin(cmds.toArray)
  }
  def pushCommands = {
    commandz.push(new ArrayBuffer[Command]())
  }
  def popCommands: Command = {
    val newCommands = commands
    commandz.pop()
    commandify(newCommands)
  }
  def collectCommands(f: => Unit): Command = {
    pushCommands
    f
    popCommands
  }
  def build(f: => Unit) = {
    val cmd = collectCommands(f)
    Circuit(components.toArray, components.last.name)
  }
}
import Builder._

abstract class Id {
  val id = genSym.next("id-")
}

abstract class Data(val name: String) extends Id {
  def toPort: Port
  def toType: Type
  def :=(other: Data) = 
    pushCommand(Connect(this.ref, other.ref))
  // def cloneType: this.type
  val module = modules.top;
  val parent = datas.top
  def ref: Immediate = {
    if (parent == null) {
      if (name == "io")
        module.ref
      else
        Field(module.ref, name, toType)
    } else
      Field(parent.ref, name, toType)
  }
  pushData(this)
}
object Data {
  def apply[T <: Data](data: T): T = {
    popData()
    data
  }
}


object Wire {
  def apply[T <: Data](x: T): T = {
    // val prevIsWire = isWire
    // isWire = true
    pushCommand(DefWire(x.name, x.toType))
    val res = x
    // val res = x.cloneType
    // isWire = prevIsWire
    res
  }
}

object Reg {
  def apply[T <: Data](x: T): T = {
    pushCommand(DefRegister(x.name, x.toType))
    x
  }
}

class Bits(name: String, val dir: Direction, val width: Int) extends Data(name) {
  def toPort: Port = 
    Port(name, dir, toType)
  def toType: Type = 
    IntType(if (width == -1) UnknownWidth() else IntWidth(width))
  def cloneType: Bits = {
    val res = new Bits("", dir, width)
    res
  }

  def + (other: Bits) = {
    val name = genSym.next("T")
    pushCommand(DefPrim(name, AddOp, Array(this.ref, other.ref)))
    Data(new Bits(name, dir, width))
  }
  def - (other: Bits) = {
    val name = genSym.next("T")
    pushCommand(DefPrim(name, MinusOp, Array(this.ref, other.ref)))
    Data(new Bits(name, dir, width))
  }
  def * (other: Bits) = {
    val name = genSym.next("T")
    pushCommand(DefPrim(name, TimesOp, Array(this.ref, other.ref)))
    Data(new Bits(name, dir, width))
  }
  def < (other: Bits): Bool = {
    val name = genSym.next("T")
    pushCommand(DefPrim(name, LessOp, Array(this.ref, other.ref)))
    Data(new Bool(name, dir))
  }
  def > (other: Bits): Bool = {
    val name = genSym.next("T")
    pushCommand(DefPrim(name, GreaterOp, Array(this.ref, other.ref)))
    Data(new Bool(name, dir))
  }
  def === (other: Bits): Bool = {
    val name = genSym.next("T")
    pushCommand(DefPrim(name, EqualOp, Array(this.ref, other.ref)))
    Data(new Bool(name, dir))
  }
}

object Bits {
  def apply(name: String, dir: Direction, width: Int) = {
    Data(new Bits(name, dir, width))
  }
  def apply(value: Int, width: Int = -1) = {
    val name = genSym.next("C")
    pushCommand(DefInt(name, value))
    Data(new Bits(name, NO_DIR, width))
  }
}

class Bool(name: String, dir: Direction) extends Bits(name, dir, 1) {
  def || (other: Bool) = {
    val name = genSym.next("T")
    pushCommand(DefPrim(name, OrOp, Array(this.ref, other.ref)))
    Data(new Bool(name, dir))
  }
  def && (other: Bool) = {
    val name = genSym.next("T")
    pushCommand(DefPrim(name, AndOp, Array(this.ref, other.ref)))
    Data(new Bool(name, dir))
  }
  def unary_! (): Bool = {
    val name = genSym.next("T")
    pushCommand(DefPrim(name, NotOp, Array(this.ref)))
    Data(new Bool(name, dir))
  }
}
object Bool {
  def apply(name: String, dir: Direction) = {
    Data(new Bool(name, dir))
  }
  def apply(value: Int) = {
    val name = genSym.next("C")
    pushCommand(DefInt(name, value))
    Data(new Bool(name, NO_DIR))
  }
}

class Bundle(name: String, dir: Direction = NO_DIR) extends Data(name) {
  def toPort: Port = 
    Port(name, dir, toType)
  def toPorts: Array[Port] = 
    elts.map(d => d.toPort).toArray
  def toType: Type = 
    BundleType(this.toPorts)
  val elts = ArrayBuffer[Data]()
}

object Module {
  def apply[T <: Module](c: T): T = {
    val cmd = popCommands
    popScope
    popModule
    val ports = c.io.toPorts
    components += Component(c.name, ports, cmd)
    pushCommand(DefInstance(c.instanceName, c.name))
    c
  }
}

abstract class Module(val instanceName: String, val name: String) extends Id {
  pushScope
  pushCommands
  pushModule(this)
  def io: Bundle
  def ref = if (this == modules.top) Ref("this") else Ref(instanceName)
}

object when {
  def execWhen(cond: Bool)(block: => Unit) {
    pushScope
    pushCommands
    block
    val cmd = popCommands
    popScope
    pushCommand(Conditionally(cond.ref, cmd, EmptyCommand()))
  }
  def apply(cond: Bool)(block: => Unit): when = {
    execWhen(cond){ block }
    new when(cond)
  }
}

class when (prevCond: Bool) {
  def elsewhen (cond: Bool)(block: => Unit): when = {
    when.execWhen(!prevCond && cond){ block }
    new when(prevCond || cond);
  }
  def otherwise (block: => Unit) {
    val cond = !prevCond
    // cond.canBeUsedAsDefault = !Module.current.hasWhenCond
    when.execWhen(cond){ block }
  }
}

object unless {
  def apply(c: Bool)(block: => Unit) {
    when (!c) { block }
  }
}

class Emitter {
  var indenting = 0
  def withIndent(f: => String) = { 
    indenting += 1;
    val res = f
    indenting -= 1;
    res
  }
  def join(parts: Array[String], sep: String) = 
    parts.foldLeft("")((s, p) => if (s == "") p else s + sep + p)
  def join0(parts: Array[String], sep: String) = 
    parts.foldLeft("")((s, p) => s + sep + p)
  def newline = 
    "\n" + join((0 until indenting).map(x => "  ").toArray, "")
  def emit(e: Direction): String = e.name
  def emit(e: PrimOp): String = e.name
  def emit(e: Immediate): String = {
    e match {
      case e: Ref => e.name;
      case e: Field => emit(e.imm) + "." + e.name
    }
  }
  def emit(e: Port): String = emit(e.dir) + " " + e.name + " : " + emit(e.kind)
  def emit(e: Width): String = {
    e match {
      case e: UnknownWidth => "?"
      case e: IntWidth => e.value.toString
    }
  }
  def emit(e: Type): String = {
    e match {
      case e: UnknownType => "?"
      case e: IntType => "Int(" + e.width + ")"
      case e: SIntType => "SInt(" + e.width + ")"
      case e: BundleType => "{" + join(e.ports.map(x => emit(x)), " ") + "}"
    }
  }
  def emit(e: Command): String = {
    e match {
      case e: DefInt => "node " + e.name + " = " + e.value
      case e: DefSInt => "node " + e.name + " = " + e.value
      case e: DefPrim => "node " + e.name + " = " + emit(e.op) + "(" + join(e.args.map(x => emit(x)), " ") + ")"
      case e: DefWire => "wire " + e.name + " : " + emit(e.kind)
      case e: DefRegister => "register " + e.name + " : " + emit(e.kind)
      case e: DefMemory => "mem " + e.name + " : " + emit(e.kind) + "[" + e.size + "]";
      case e: DefVector => "vec " + e.name + " : " + emit(e.kind) + "(" + join(e.args.map(x => emit(x)), " ") + ")"
      case e: DefAccessor => "accessor " + emit(e.direction) + " " + e.name + "[" + e.index + "]"
      case e: DefInstance => "instance " + e.name + " of " + e.module
      case e: Conditionally => "when " + emit(e.pred) + " { " + withIndent{ emit(e.conseq) } + " } else { " + emit(e.alt) + " } "
      case e: Begin => join0(e.body.map(x => emit(x)), newline)
      case e: Connect => emit(e.loc) + " := " + emit(e.exp)
      case e: EmptyCommand => "skip"
    }
  }
  def emit(e: Component): String = 
    withIndent{ "module " + e.name + " : " + join0(e.ports.map(x => emit(x)), newline) + emit(e.body) }
  def emit(e: Circuit): String = 
    withIndent{ "circuit " + e.main + " : " + join0(e.components.map(x => emit(x)), newline) }
}
