package Chipper
import scala.collection.mutable.{ArrayBuffer, Stack, HashSet, HashMap}
import java.lang.reflect.Modifier._

/// TODO:
/// uint, sint -- copy current implementation -- more to expand
/// vecs -- subclass Data
/// init on regs -- need to figure this out
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
  val modules = new HashMap[String,Module]()
  def addModule(mod: Module) {
    modules(mod.id) = mod
  }
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
  def collectCommands[T <: Module](f: => T): Command = {
    pushCommands
    val mod = f
    mod.setRefs
    popCommands
  }

  private val refmap = new HashMap[String,Immediate]()

  def setRefForId(id: String, imm: Immediate) {
    refmap(id) = imm
  }

  def setFieldForId(parentid: String, id: String, name: String) {
    refmap(id) = Field(Alias(parentid), name)
  }

  def setIndexForId(parentid: String, id: String, index: Int) {
    refmap(id) = Index(Alias(parentid), index)
  }

  def getRefForId(id: String): Immediate = {
    if (refmap.contains(id)) {
      refmap(id)
    } else  {
      val ref = Ref(genSym.next("T"))
      refmap(id) = ref
      ref
    }
  }

  def build[T <: Module](f: => T) = {
    val cmd = collectCommands(f)
    Circuit(components.toArray, components.last.name)
  }

  def finalizeData(data: Data) {
    data match {
      case bundle: Bundle => bundle.collectElts
      case vec: Vec[_] => vec.collectElts
      case _ => ()
    }
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
  val BitNotOp = PrimOp("BitNot");
  val ConcatOp = PrimOp("Concat");
  val BitSelectOp = PrimOp("Select");
  val BitsExtractOp = PrimOp("Extract");
  val LessOp = PrimOp("Less");
  val LessEqOp = PrimOp("LessEq");
  val GreaterOp = PrimOp("Greater");
  val GreaterEqOp = PrimOp("GreaterEq");
  val EqualOp = PrimOp("Equal");
  val NotEqualOp = PrimOp("NotEqual");
  val NotOp = PrimOp("Not");
  val NegOp = PrimOp("Neg");
  val MultiplexOp = PrimOp("Multiplex");
  val AndReduceOp = PrimOp("AndReduce");
  val OrReduceOp = PrimOp("OrReduce");
  val XorReduceOp = PrimOp("XorReduce");
}
import PrimOp._

abstract class Immediate {
  def fullname: String
  def name: String
}

case class Alias(val id: String) extends Immediate{
  def fullname = getRefForId(id).fullname
  def name = getRefForId(id).name
}
case class Ref(val name: String) extends Immediate {
  def fullname = name
}
case class Field(val imm: Immediate, val name: String) extends Immediate {
  def fullname = {
    imm.fullname + "." + name
  }
}
case class Index(val imm: Immediate, val value: Int) extends Immediate {
  def name = "@" + value
  def fullname = {
    imm.fullname + "@" + value
  }
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
case class VectorType(val size: Int, val kind: Type) extends Type;

abstract class Command;
abstract class Definition extends Command {
  def id: String
  def name = getRefForId(id).name
}
case class DefUInt(val id: String, val value: Int) extends Definition;
case class DefSInt(val id: String, val value: Int) extends Definition;
case class DefPrim(val id: String, val op: PrimOp, val args: Array[Alias]) extends Definition;
case class DefWire(val id: String, val kind: Type) extends Definition;
case class DefRegister(val id: String, val kind: Type) extends Definition;
case class DefMemory(val id: String, val kind: Type, val size: Int) extends Definition;
// case class DefVector(val id: String, val kind: Type, val args: Iterable[Alias]) extends Definition;
case class DefAccessor(val id: String, val source: Alias, val direction: Direction, val index: Alias) extends Definition;
case class DefInstance(val id: String, val module: String) extends Definition;
case class Conditionally(val pred: Alias, val conseq: Command, val alt: Command) extends Command;
case class Begin(val body: Array[Command]) extends Command();
case class Connect(val loc: Alias, val exp: Alias) extends Command;
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
  def toType: Type
  def dir: Direction
  def :=(other: Data) = 
    pushCommand(Connect(this.ref, other.ref))
  def cloneType: this.type
  def ref: Alias = Alias(id)
  def name = getRefForId(id).name
  def getWidth: Int
  def flatten: Array[Bits]
  def fromBits(n: Bits): this.type = {
    val res = this.cloneType
    var i = 0
    for (x <- res.flatten.reverse) {
      x := n(i + x.getWidth-1, i)
      i += x.getWidth
    }
    res
  }
  def toBits: Bits = {
    val elts = this.flatten
    Cat(elts.head, elts.tail:_*)
  }
  def toPort: Port = Port(id, dir, toType)
}

object Wire {
  def apply[T <: Data](t: T): T = {
    val x = t.cloneType
    finalizeData(x)
    pushCommand(DefWire(x.id, x.toType))
    x
  }
}

object Reg {
  def apply[T <: Data](t: T): T = {
    val x = t.cloneType
    finalizeData(x)
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
    val x = t.cloneType
    finalizeData(x)
    pushCommand(DefAccessor(x.id, Alias(t.id), NO_DIR, idx.ref))
    x
  }
}

object Vec {
  def apply[T <: Data](elts: Iterable[T]): Vec[T] = {
    val vec = new Vec[T](i => elts.head.cloneType)
    vec.self ++= elts
    // pushCommand(DefVector(t.id, t.toType, elts.map(_.ref)))
    vec
  }
  def tabulate[T <: Data](n: Int)(gen: (Int) => T): Vec[T] =
    apply((0 until n).map(i => gen(i)))
  def fill[T <: Data](n: Int)(gen: => T): Vec[T] = 
    Vec.tabulate(n){ i => gen }
}

abstract class Aggregate extends Data {
  def collectElts: Unit;
}

class Vec[T <: Data](val gen: (Int) => T) extends Aggregate with VecLike[T] {
  val self = new ArrayBuffer[T]
  def apply(idx: UInt): T = {
    val x = self(0).cloneType
    pushCommand(DefAccessor(x.id, Alias(id), NO_DIR, idx.ref))
    x
  }
  def apply(idx: Int): T = 
    self(idx)
  def toPorts: Array[Port] = 
    self.map(d => d.toPort).toArray
  def toType: Type = 
    VectorType(self.size, self(0).toType)
  override def cloneType: this.type =
    Vec.tabulate(size)(i => self(i)).asInstanceOf[this.type]

  override def flatten: Array[Bits] = 
    self.map(_.flatten).reduce(_ ++ _)
  override def getWidth: Int = 
    flatten.map(_.getWidth).reduce(_ + _)

  def dir = self(0).dir

  def collectElts: Unit = {
    for (i <- 0 until self.size) {
      val elt = self(i)
      elt match {
        case bundle: Bundle =>
          setIndexForId(id, bundle.id, i)
          bundle.collectElts
        case vec: Vec[_] =>
          setIndexForId(id, vec.id, i)
          vec.collectElts
        case data: Data =>
          setIndexForId(id, data.id, i)
      }
    }
  }

  def length: Int = self.size
}

trait VecLike[T <: Data] extends collection.IndexedSeq[T] {
  // def read(idx: UInt): T
  // def write(idx: UInt, data: T): Unit
  def apply(idx: UInt): T

  def forall(p: T => Bool): Bool = (this map p).fold(Bool(true))(_&&_)
  def exists(p: T => Bool): Bool = (this map p).fold(Bool(false))(_||_)
  // def contains[T <: Bits](x: T): Bool = this.exists(_ === x)
  def count(p: T => Bool): UInt = PopCount((this map p).toSeq)

  private def indexWhereHelper(p: T => Bool) = this map p zip (0 until length).map(i => UInt(i))
  def indexWhere(p: T => Bool): UInt = PriorityMux(indexWhereHelper(p))
  def lastIndexWhere(p: T => Bool): UInt = PriorityMux(indexWhereHelper(p).reverse)
  def onlyIndexWhere(p: T => Bool): UInt = Mux1H(indexWhereHelper(p))
}

/** Returns the number of bits set (i.e value is 1) in the input signal.
  */
object PopCount
{
  def apply(in: Iterable[Bool]): UInt = {
    if (in.size == 0) {
      UInt(0)
    } else if (in.size == 1) {
      in.head
    } else {
      apply(in.slice(0, in.size/2)) + Cat(UInt(0), apply(in.slice(in.size/2, in.size)))
    }
  }
  def apply(in: Bits): UInt = apply((0 until in.getWidth).map(in(_)))
}

/** Builds a Mux tree out of the input signal vector using a one hot encoded
  select signal. Returns the output of the Mux tree.
  */
object Mux1H
{
  def apply[T <: Data](sel: Iterable[Bool], in: Iterable[T]): T = {
    if (in.tail.isEmpty) in.head
    else {
      val masked = (sel, in).zipped map ((s, i) => Mux(s, i.toBits, Bits(0)))
      in.head.fromBits(masked.reduceLeft(_|_))
    }
  }
  def apply[T <: Data](in: Iterable[(Bool, T)]): T = {
    val (sel, data) = in.unzip
    apply(sel, data)
  }
  def apply[T <: Data](sel: Bits, in: Iterable[T]): T =
    apply((0 until in.size).map(sel(_)), in)
  def apply(sel: Bits, in: Bits): Bool = (sel & in).orR
}

/** Builds a Mux tree under the assumption that multiple select signals
  can be enabled. Priority is given to the first select signal.

  Returns the output of the Mux tree.
  */
object PriorityMux
{
  def apply[T <: Bits](in: Iterable[(Bool, T)]): T = {
    if (in.size == 1) {
      in.head._2
    } else {
      Mux(in.head._1, in.head._2, apply(in.tail))
    }
  }
  def apply[T <: Bits](sel: Iterable[Bool], in: Iterable[T]): T = apply(sel zip in)
  def apply[T <: Bits](sel: Bits, in: Iterable[T]): T = apply((0 until in.size).map(sel(_)), in)
}

class Bits(val dir: Direction, val width: Int) extends Data {
  private def binop(op: PrimOp, other: Bits): Bits = {
    val d = new Bits(dir, width)
    pushCommand(DefPrim(d.id, op, Array(this.ref, other.ref)))
    d
  }
  protected def compop(op: PrimOp, other: Bits): Bool = {
    val d = new Bool(dir)
    pushCommand(DefPrim(d.id, op, Array(this.ref, other.ref)))
    d
  }

  def toType: Type = 
    UIntType(if (width == -1) UnknownWidth() else IntWidth(width))
  override def cloneType: this.type = {
    val res = new Bits(dir, width).asInstanceOf[this.type]
    res
  }
  override def getWidth: Int = width
  override def flatten: Array[Bits] = Array[Bits](this)

  def apply(x: UInt): Bool = {
    val d = new Bool(dir)
    pushCommand(DefPrim(d.id, BitSelectOp, Array(this.ref, x.ref)))
    d
  }
  def apply(x: Int): Bool = 
    apply(UInt(x))
  def apply(x: UInt, y: UInt): Bits = {
    val d = new Bits(dir, width)
    pushCommand(DefPrim(d.id, BitsExtractOp, Array(this.ref, x.ref, y.ref)))
    d
  }
  def apply(x: Int, y: Int): Bits = 
    apply(UInt(x), UInt(y))

  def + (other: Bits) = binop(AddOp, other)
  def +% (other: Bits) = binop(AddModOp, other)
  def - (other: Bits) = binop(MinusOp, other)
  def * (other: Bits) = binop(TimesOp, other)
  def / (other: Bits) = binop(DivideOp, other)
  def % (other: Bits) = binop(ModOp, other)
  def << (other: Bits) = binop(ShiftLeftOp, other)
  def >> (other: Bits) = binop(ShiftRightOp, other)

  def & (other: Bits) = binop(BitAndOp, other)
  def | (other: Bits) = binop(BitOrOp, other)
  def ^ (other: Bits) = binop(BitXorOp, other)
  def unary_~ = {
    val d = new Bits(dir, width)
    pushCommand(DefPrim(d.id, BitNotOp, Array(this.ref)))
    d
  }

  def < (other: Bits) = compop(LessOp, other)
  def > (other: Bits) = compop(GreaterOp, other)
  def === (other: Bits) = compop(EqualOp, other)
  def != (other: Bits) = compop(NotEqualOp, other)
  def <= (other: Bits) = compop(LessEqOp, other)
  def >= (other: Bits) = compop(GreaterEqOp, other)

  def orR: Bool = {
    val d = new Bool(dir)
    pushCommand(DefPrim(d.id, OrReduceOp, Array(this.ref)))
    d
  }
  def andR: Bool = {
    val d = new Bool(dir)
    pushCommand(DefPrim(d.id, AndReduceOp, Array(this.ref)))
    d
  }
  def xorR: Bool = {
    val d = new Bool(dir)
    pushCommand(DefPrim(d.id, XorReduceOp, Array(this.ref)))
    d
  }
}

object Bits {
  def apply(dir: Direction, width: Int) = {
    new Bits(dir, width)
  }
  def apply(value: Int, width: Int = -1) = {
    val b = new Bits(NO_DIR, width)
    pushCommand(DefUInt(b.id, value))
    b
  }
}

abstract trait Num[T <: Data] {
  def << (b: T): T;
  def >> (b: T): T;
  //def unary_-(): T;
  def +  (b: T): T;
  def *  (b: T): T;
  def /  (b: T): T;
  def %  (b: T): T;
  def -  (b: T): T;
  def <  (b: T): Bool;
  def <= (b: T): Bool;
  def >  (b: T): Bool;
  def >= (b: T): Bool;

  def min(b: T): T = Mux(this < b, this.asInstanceOf[T], b)
  def max(b: T): T = Mux(this < b, b, this.asInstanceOf[T])
}

class UInt(dir: Direction, width: Int) extends Bits(dir, width) with Num[UInt] {
  override def cloneType: this.type = {
    val res = new UInt(dir, width).asInstanceOf[this.type]
    res
  }

  private def binop(op: PrimOp, other: UInt): UInt = {
    val d = new UInt(dir, width)
    pushCommand(DefPrim(d.id, op, Array(this.ref, other.ref)))
    d
  }

  def + (other: UInt) = binop(AddOp, other)
  def +% (other: UInt) = binop(AddModOp, other)
  def - (other: UInt) = binop(MinusOp, other)
  def * (other: UInt) = binop(TimesOp, other)
  def / (other: UInt) = binop(DivideOp, other)
  def % (other: UInt) = binop(ModOp, other)
  def << (other: UInt) = binop(ShiftLeftOp, other)
  def >> (other: UInt) = binop(ShiftRightOp, other)

  def < (other: UInt) = compop(LessOp, other)
  def > (other: UInt) = compop(GreaterOp, other)
  def === (other: UInt) = compop(EqualOp, other)
  def <= (other: UInt) = compop(LessEqOp, other)
  def >= (other: UInt) = compop(GreaterEqOp, other)
}

object UInt {
  def apply(dir: Direction, width: Int) = {
    new UInt(dir, width)
  }
  def apply(value: Int, width: Int = -1) = {
    val b = new UInt(NO_DIR, width)
    pushCommand(DefUInt(b.id, value))
    b
  }
}

class SInt(dir: Direction, width: Int) extends Bits(dir, width) with Num[SInt] {
  override def cloneType: this.type = {
    val res = new SInt(dir, width).asInstanceOf[this.type]
    res
  }

  private def binop(op: PrimOp, other: SInt): SInt = {
    val d = new SInt(dir, width)
    pushCommand(DefPrim(d.id, op, Array(this.ref, other.ref)))
    d
  }

  def unary_- = {
    val d = new SInt(dir, width)
    pushCommand(DefPrim(d.id, NegOp, Array(this.ref)))
    d
  }

  def + (other: SInt) = binop(AddOp, other)
  def +% (other: SInt) = binop(AddModOp, other)
  def - (other: SInt) = binop(MinusOp, other)
  def * (other: SInt) = binop(TimesOp, other)
  def / (other: SInt) = binop(DivideOp, other)
  def % (other: SInt) = binop(ModOp, other)
  def << (other: SInt) = binop(ShiftLeftOp, other)
  def >> (other: SInt) = binop(ShiftRightOp, other)

  def < (other: SInt) = compop(LessOp, other)
  def > (other: SInt) = compop(GreaterOp, other)
  def === (other: SInt) = compop(EqualOp, other)
  def <= (other: SInt) = compop(LessEqOp, other)
  def >= (other: SInt) = compop(GreaterEqOp, other)
}

object SInt {
  def apply(dir: Direction, width: Int) = {
    new SInt(dir, width)
  }
  def apply(value: Int, width: Int = -1) = {
    val b = new SInt(NO_DIR, width)
    pushCommand(DefSInt(b.id, value))
    b
  }
}

class Bool(dir: Direction) extends UInt(dir, 1) {
  def || (other: Bool) = {
    val d = new Bool(dir)
    pushCommand(DefPrim(d.id, OrOp, Array(this.ref, other.ref)))
    d
  }
  def && (other: Bool) = {
    val d = new Bool(dir)
    pushCommand(DefPrim(d.id, AndOp, Array(this.ref, other.ref)))
    d
  }
  def unary_! (): Bool = {
    val d = new Bool(dir)
    pushCommand(DefPrim(d.id, NotOp, Array(this.ref)))
    d
  }
}
object Bool {
  def apply(dir: Direction) : Bool = {
    new Bool(dir)
  }
  def apply(value: Int) : Bool = {
    val b = new Bool(NO_DIR)
    pushCommand(DefUInt(b.id, value))
    b
  }
  def apply(value: Boolean) : Bool = 
    apply(if (value) 1 else 0)
}

object Mux {
  def apply[T <: Data](cond: Bool, con: T, alt: T): T = {
    val r = con.cloneType
    pushCommand(DefPrim(r.id, MultiplexOp, Array(cond.ref, con.ref, alt.ref)))
    r
  }
}

object Cat {
  def apply[T <: Bits](a: T, r: T*): T = apply(a :: r.toList)
  def apply[T <: Bits](r: Seq[T]): T = doCat(r)
  private def doCat[T <: Data](r: Seq[T]): T = {
    if (r.tail.isEmpty)
      r.head
    else {
      val l = doCat(r.slice(0, r.length/2))
      val h = doCat(r.slice(r.length/2, r.length))
      val d = l.cloneType
      pushCommand(DefPrim(d.id, ConcatOp, Array(l.ref, h.ref)))
      d
    }
  }
}

object Bundle {
  val keywords = HashSet[String]("elements", "flip", "toString",
    "flatten", "binding", "asInput", "asOutput", "unary_$tilde",
    "unary_$bang", "unary_$minus", "cloneType", "toUInt", "toBits",
    "toBool", "toSInt", "asDirectionless")
}

class Bundle(val dir: Direction = OUTPUT) extends Aggregate {
  def toPorts: Array[Port] = 
    elts.map(d => d.toPort).toArray
  def toType: BundleType = 
    BundleType(this.toPorts)

  override def flatten: Array[Bits] = 
    elts.map(_.flatten).reduce(_ ++ _)
  override def getWidth: Int = 
    flatten.map(_.getWidth).reduce(_ + _)

  val elts = ArrayBuffer[Data]()
  def collectElts: Unit = {
    elts.clear()
    for (m <- getClass.getDeclaredMethods) {
      val name = m.getName

      val modifiers = m.getModifiers();
      val types = m.getParameterTypes()
      var isInterface = false;
      var isFound = false;
      val rtype = m.getReturnType();
      var c = rtype;
      val sc = Class.forName("Chipper.Data");
      do {
        if (c == sc) {
          isFound = true; isInterface = true;
        } else if (c == null || c == Class.forName("java.lang.Object")) {
          isFound = true; isInterface = false;
        } else {
          c = c.getSuperclass();
        }
      } while (!isFound);
      if (types.length == 0 && !isStatic(modifiers) && isInterface
          && !(Bundle.keywords contains name)) {
        val obj = m.invoke(this)
        obj match {
          case bundle: Bundle =>
            setFieldForId(id, bundle.id, name)
            bundle.collectElts
            elts += bundle
          case vec: Vec[_] =>
            setFieldForId(id, vec.id, name)
            vec.collectElts
            elts += vec
          case data: Data =>
            setFieldForId(id, data.id, name)
            elts += data
          case _ => ()
        }
      }
    }
  }

  override def cloneType: this.type = {
    try {
      val constructor = this.getClass.getConstructors.head
      val res = constructor.newInstance(Array.fill(constructor.getParameterTypes.size)(null):_*)
      res.asInstanceOf[this.type]
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
    c.io.collectElts
    val ports = c.io.toPorts
    components += Component(c.name, ports, cmd)
    pushCommand(DefInstance(c.id, c.name))
    c
  }
}

abstract class Module extends Id {
  pushScope
  pushCommands
  addModule(this)

  def io: Bundle
  def ref = getRefForId(id)

  def name = {
    getClass.getName
  }

  def setRefs {
    setRefForId(io.id, Ref("this"))

    for (m <- getClass.getDeclaredMethods) {
      val name = m.getName()
      val types = m.getParameterTypes()
      if (types.length == 0) {
        val obj = m.invoke(this)
        obj match {
          case module: Module =>
            setRefForId(module.id, Ref(name))
            module.setRefs
          case bundle: Bundle =>
            if (name != "io") {
              setRefForId(bundle.id, Ref(name))
            }
          case mem: Mem[_] =>
            setRefForId(mem.t.id, Ref(name))
          case vec: Vec[_] =>
            setRefForId(vec.id, Ref(name))
          case data: Data =>
            setRefForId(data.id, Ref(name))
          // ignore anything not of those types
          case _ => null
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
  def emit(e: Alias): String = e.fullname
  def emit(e: Port): String =
    emit(e.dir) + " " + getRefForId(e.id).name + " : " + emit(e.kind)
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
      case e: VectorType => "Vec(" + emit(e.kind) + ", " + e.size + ")"
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
      // case e: DefVector => "vec " + e.name + " : " + emit(e.kind) + "(" + join(e.args.map(x => emit(x)).toArray[String], " ") + ")"
      case e: DefAccessor => "accessor " + e.name + " " + emit(e.source) + "[" + emit(e.index) + "]"
      case e: DefInstance => {
        val mod = modules(e.id)
        // update all references to the modules ports
        setRefForId(mod.io.id, Ref(e.name))
        "instance " + e.name + " of " + e.module
      }
      case e: Conditionally => "when " + emit(e.pred) + " { " + withIndent{ emit(e.conseq) } + newline + "}" + (if (e.alt.isInstanceOf[EmptyCommand]) "" else " else { " + withIndent{ emit(e.alt) } + newline + "}")
      case e: Begin => join0(e.body.map(x => emit(x)), newline)
      case e: Connect => emit(e.loc) + " := " + emit(e.exp)
      case e: EmptyCommand => "skip"
    }
  }
  def emit(e: Component): String =  {
    withIndent{ "module " + e.name + " : " +
      join0(e.ports.map(x => emit(x)), newline) +
      newline + emit(e.body) }
  }
  def emit(e: Circuit): String = 
    withIndent{ "circuit " + e.main + " : " + join0(e.components.map(x => emit(x)), newline) }
}
