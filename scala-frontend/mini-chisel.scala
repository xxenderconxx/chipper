import scala.collection.mutable.{ArrayBuffer, Stack, HashSet, HashMap}

/// TODO:
/// uint, sint -- copy current implementation
/// unique id -> named using introspection
/// memories -- need to gen accessors and connects
/// vecs -- need to figure this out
/// init on regs -- need to figure this out
/// get rid of this
/// tester -- need internal access
/// multiple clock domains -- 
/// 

class GenSym {
  var counter = -1
  def next(name: String): String = {
    counter += 1
    name + "_" + counter
  }
}

object Builder {
  val components = new ArrayBuffer[Component]()
  val genSym = new GenSym()
  val scopes = new Stack[HashSet[String]]()
  def scope = scopes.top
  def pushScope = {
    scopes.push(new HashSet[String]())
  }
  def popScope = {
    scopes.pop()
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

  private val idmap = new HashMap[String,String]()
  def setNameForId(id: String, name: String) {
    // println("SETTING ID " + id + " TO " + name)
    idmap(id) = name
  }
  def getNameForId(id: String) = {
    val res = 
    if (id == "this") {
      "this"
    } else if (idmap.contains(id)) {
      idmap(id)
    } else  {
      val name = genSym.next("T")
      idmap(id) = name
      name
    }
    // println("ID = " + id + " => " + res)
    res
  }

  def build[T <: Module](f: => T) = {
    val cmd = collectCommands(f)
    Circuit(components.toArray, components.last.name)
  }
}

import Builder._

/// CHISEL IR

case class PrimOp(val name: String) {
  override def toString = name
}

object PrimOp {
  val AddOp = PrimOp("Add");
  val AddModOp = PrimOp("AddMod");
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

abstract class Immediate {
  def name: String
}

case class Ref(val name: String, val kind: Type = UnknownType()) extends Immediate;
case class Field(val imm: Immediate, val id: String, val kind: Type = UnknownType())
    extends Immediate {
  def name = getNameForId(id)
}

case class Port(val id: String, val dir: Direction, val kind: Type);

abstract class Width;
case class UnknownWidth() extends Width;
case class IntWidth(val value: Int) extends Width;

abstract class Type;
case class UnknownType extends Type;
case class UIntType(val width: Width) extends Type;
case class SIntType(val width: Width) extends Type;
case class BundleType(val ports: Array[Port]) extends Type;

abstract class Command;
abstract class Definition extends Command {
  def id: String
  def name = getNameForId(id)
}
case class DefUInt(val id: String, val value: Int) extends Definition;
case class DefSInt(val id: String, val value: Int) extends Definition;
case class DefPrim(val id: String, val op: PrimOp, val args: Array[Immediate]) extends Definition;
case class DefWire(val id: String, val kind: Type) extends Definition;
case class DefRegister(val id: String, val kind: Type) extends Definition;
case class DefMemory(val id: String, val kind: Type, val size: Int) extends Definition;
case class DefVector(val id: String, val kind: Type, val args: Array[Immediate]) extends Definition;
case class DefAccessor(val id: String, val source: String, val direction: Direction, val index: Immediate) extends Definition;
case class DefInstance(val id: String, val module: String) extends Definition;
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

/// CHISEL FRONT-END

abstract class Id {
  val id = genSym.next("id")
}

abstract class Data extends Id {
  def toPort: Port
  def toType: Type
  def :=(other: Data) = 
    pushCommand(Connect(this.ref, other.ref))
  def cloneType: this.type
  val module = modules.top;
  val parent = datas.top
  def ref: Immediate = {
    if (parent == null) {
      //module.findNames
      Field(module.ref, id, toType)
    } else
      Field(parent.ref, id, toType)
  }

  def name = getNameForId(id)

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
    pushCommand(DefWire(x.id, x.toType))
    val res = x
    // val res = x.cloneType
    // isWire = prevIsWire
    res
  }
}

object Reg {
  def apply[T <: Data](x: T): T = {
    pushCommand(DefRegister(x.id, x.toType))
    x
  }
}

object Mem {
  def apply[T <: Data](t: T, size: Int): Mem[T] = {
    val mem = new Mem(t, size)
    pushCommand(DefMemory(t.id, t.toType, size))
    mem
  }
}

class Mem[T <: Data](val t: T, val size: Int) {
  def apply(idx: Bits): T = {
    val y = t.cloneType
    pushCommand(DefAccessor(y.id, t.id, NO_DIR, idx.ref))
    y
  }
}

object Vec {
  def apply[T <: Data](t: T, elts: Array[T]): Vec[T] = {
    val vec = new Vec(t, elts)
    pushCommand(DefVector(t.id, t.toType, elts.map(_.ref)))
    vec
  }
}

class Vec[T <: Data](val t: T, val elts: Array[T]) {
  def apply(idx: Bits): T = {
    val y = elts(0).cloneType
    pushCommand(DefAccessor(y.id, t.id, NO_DIR, idx.ref))
    y
  }
  def apply(idx: Int): T = 
    elts(idx)
}

class Bits(val dir: Direction, val width: Int) extends Data {
  def toPort: Port = 
    Port(id, dir, toType)
  def toType: Type = 
    UIntType(if (width == -1) UnknownWidth() else IntWidth(width))
  override def cloneType: this.type = {
    val res = new Bits(dir, width).asInstanceOf[this.type]
    Data(res)
  }

  def + (other: Bits) = {
    val d = Data(new Bits(dir, width))
    pushCommand(DefPrim(d.id, AddOp, Array(this.ref, other.ref)))
    d
  }
  def +% (other: Bits) = {
    val d = Data(new Bits(dir, width))
    pushCommand(DefPrim(d.id, AddModOp, Array(this.ref, other.ref)))
    d
  }
  def - (other: Bits) = {
    val d = Data(new Bits(dir, width))
    pushCommand(DefPrim(d.id, MinusOp, Array(this.ref, other.ref)))
    d
  }
  def * (other: Bits) = {
    val d = Data(new Bits(dir, width))
    pushCommand(DefPrim(d.id, TimesOp, Array(this.ref, other.ref)))
    d
  }
  def < (other: Bits): Bool = {
    val d = Data(new Bool(dir))
    pushCommand(DefPrim(d.id, LessOp, Array(this.ref, other.ref)))
    d
  }
  def > (other: Bits): Bool = {
    val d = Data(new Bool(dir))
    pushCommand(DefPrim(d.id, GreaterOp, Array(this.ref, other.ref)))
    d
  }
  def === (other: Bits): Bool = {
    val d = Data(new Bool(dir))
    pushCommand(DefPrim(d.id, EqualOp, Array(this.ref, other.ref)))
    d
  }
}

object Bits {
  def apply(dir: Direction, width: Int) = {
    Data(new Bits(dir, width))
  }
  def apply(value: Int, width: Int = -1) = {
    val b = new Bits(NO_DIR, width)
    pushCommand(DefUInt(b.id, value))
    Data(b)
  }
}

class Bool(dir: Direction) extends Bits(dir, 1) {
  def || (other: Bool) = {
    val d = Data(new Bool(dir))
    pushCommand(DefPrim(d.id, OrOp, Array(this.ref, other.ref)))
    d
  }
  def && (other: Bool) = {
    val d = Data(new Bool(dir))
    pushCommand(DefPrim(d.id, AndOp, Array(this.ref, other.ref)))
    d
  }
  def unary_! (): Bool = {
    val d = Data(new Bool(dir))
    pushCommand(DefPrim(d.id, NotOp, Array(this.ref)))
    d
  }
}
object Bool {
  def apply(dir: Direction) = {
    Data(new Bool(dir))
  }
  def apply(value: Int) = {
    val b = new Bool(NO_DIR)
    pushCommand(DefUInt(b.id, value))
    Data(b)
  }
}

class Bundle(dir: Direction = NO_DIR) extends Data {
  def toPort: Port = 
    Port(id, dir, toType)
  def toPorts: Array[Port] = 
    elts.map(d => d.toPort).toArray
  def toType: Type = 
    BundleType(this.toPorts)
  def collectElts {
    for (m <- getClass.getDeclaredMethods) {
      val name = m.getName
      val types = m.getParameterTypes()
      if (types.length == 0) {
        val obj = m.invoke(this)
        obj match {
          case bundle: Bundle =>
            bundle.collectElts
            elts += bundle
            setNameForId(bundle.id, name)
          case data: Data =>
            elts += data
            setNameForId(data.id, name)
        }
      }
    }
  }
  val elts = ArrayBuffer[Data]()
  override def cloneType: this.type = {
    try {
      val constructor = this.getClass.getConstructors.head
      val res = constructor.newInstance(Array.fill(constructor.getParameterTypes.size)(null):_*)
      Data(res.asInstanceOf[this.type])
    } catch {
      case npe: java.lang.reflect.InvocationTargetException if npe.getCause.isInstanceOf[java.lang.NullPointerException] =>
      //   throwException("Parameterized Bundle " + this.getClass + " needs clone method. You are probably using an anonymous Bundle object that captures external state and hence is un-cloneable", npe)
        error("BAD")
      case e: java.lang.Exception =>
        error("BAD")
      //   throwException("Parameterized Bundle " + this.getClass + " needs clone method", e)
    }
  }
}

object Module {
  def apply[T <: Module](c: T): T = {
    val cmd = popCommands
    popScope
    val mod = popModule
    mod.findNames
    val ports = c.io.toPorts
    components += Component(c.name, ports, cmd)
    pushCommand(DefInstance(c.id, c.name))
    c
  }
}

abstract class Module extends Id {
  pushScope
  pushCommands
  pushModule(this)
  def io: Bundle
  def ref = if (this == modules.top) Ref("this") else Ref(id)

  def name = {
    getClass.getName
  }

  def findNames {
    for (m <- getClass.getDeclaredMethods) {
      val name = m.getName()
      val types = m.getParameterTypes()
      if (types.length == 0) {
        val obj = m.invoke(this)
        obj match {
          case module: Module =>
            println("SETTING MODULE NAME " + name)
            setNameForId(module.id, name)
          case bundle: Bundle =>
            bundle.collectElts
            setNameForId(bundle.id, name)
          case mem: Mem[_] =>
            setNameForId(mem.t.id, name)
          case vec: Vec[_] =>
            setNameForId(vec.t.id, name)
          case data: Data =>
            setNameForId(data.id, name)
        }
      }
    }
  }
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
    this.otherwise {
      when.execWhen(cond) { block }
    }
    new when(cond)
  }

  private def replaceCondition(
      cond: Conditionally, elsecmd: Command): Conditionally = {
    cond.alt match {
      // this is an elsewhen clause
      // we have to go deeper
      case newcond: Conditionally =>
        Conditionally(cond.pred, cond.conseq,
          replaceCondition(newcond, elsecmd))
      // if the alt is empty, we've found the end
      case empty: EmptyCommand =>
        Conditionally(cond.pred, cond.conseq, elsecmd)
      // this shouldn't happen
      case _ =>
        throw new Exception("Cannot replace non-empty else clause")
    }
  }

  def otherwise (block: => Unit) {
    // first generate the body
    pushScope
    pushCommands
    block
    val elsecmd = popCommands
    popScope
    // now we look back and find the last Conditionally
    val isConditionally = (x: Command) => {
      x match {
        case Conditionally(_, _, _) => true
        case _ => false
      }
    }
    // replace the last Conditionally with a new one with the
    // same predicate and consequent but with the last alt replaced
    // by the commands for the otherwise body
    val i = commands.lastIndexWhere(isConditionally)
    commands(i) = commands(i) match {
      case cond: Conditionally =>
        replaceCondition(cond, elsecmd)
      // this should never happen
      case _ => throw new Exception("That's not a conditionally")
    }
  }
}

object unless {
  def apply(c: Bool)(block: => Unit) {
    when (!c) { block }
  }
}

/// CHISEL IR EMITTER

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
      case e: Ref => getNameForId(e.name);
      case e: Field => emit(e.imm) + "." + e.name
    }
  }
  def emit(e: Port): String =
    emit(e.dir) + " " + getNameForId(e.id) + " : " + emit(e.kind)
  def emit(e: Width): String = {
    e match {
      case e: UnknownWidth => "?"
      case e: IntWidth => e.value.toString
    }
  }
  def emit(e: Type): String = {
    e match {
      case e: UnknownType => "?"
      case e: UIntType => "UInt(" + emit(e.width) + ")"
      case e: SIntType => "SInt(" + emit(e.width) + ")"
      case e: BundleType => "{" + join(e.ports.map(x => emit(x)), " ") + "}"
    }
  }
  def emit(e: Command): String = {
    e match {
      case e: DefUInt => "ulit " + e.name + " = " + e.value
      case e: DefSInt => "slit " + e.name + " = " + e.value
      case e: DefPrim => "prim " + e.name + " = " + emit(e.op) + "(" + join(e.args.map(x => emit(x)), ", ") + ")"
      case e: DefWire => "wire " + e.name + " : " + emit(e.kind)
      case e: DefRegister => "reg " + e.name + " : " + emit(e.kind)
      case e: DefMemory => "mem " + e.name + " : " + emit(e.kind) + "[" + e.size + "]";
      case e: DefVector => "vec " + e.name + " : " + emit(e.kind) + "(" + join(e.args.map(x => emit(x)), " ") + ")"
      case e: DefAccessor => "accessor " + e.name + "[" + emit(e.index) + "]"
      case e: DefInstance => "instance " + e.name + " of " + e.module
      case e: Conditionally => "when " + emit(e.pred) + " { " + withIndent{ emit(e.conseq) } + newline + "}" + (if (e.alt.isInstanceOf[EmptyCommand]) "" else " else { " + withIndent{ emit(e.alt) } + newline + "}")
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
