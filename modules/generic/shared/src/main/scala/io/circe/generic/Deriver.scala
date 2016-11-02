package io.circe.generic

import io.circe.generic.decoding.{ DerivedDecoder, ReprDecoder }
import io.circe.generic.encoding.{ DerivedObjectEncoder, ReprObjectEncoder }
import io.circe.generic.util.macros.DerivationMacros
import macrocompat.bundle
import scala.reflect.macros.whitebox

@bundle
class Deriver(val c: whitebox.Context)
    extends DerivationMacros[ReprDecoder, ReprObjectEncoder, DerivedDecoder, DerivedObjectEncoder] {
  import c.universe._

  def deriveDecoder[R: c.WeakTypeTag]: c.Expr[ReprDecoder[R]] = c.Expr[ReprDecoder[R]](constructDecoder[R])
  def deriveEncoder[R: c.WeakTypeTag]: c.Expr[ReprObjectEncoder[R]] = c.Expr[ReprObjectEncoder[R]](constructEncoder[R])

  protected[this] val RD: TypeTag[ReprDecoder[_]] = c.typeTag
  protected[this] val RE: TypeTag[ReprObjectEncoder[_]] = c.typeTag
  protected[this] val DD: TypeTag[DerivedDecoder[_]] = c.typeTag
  protected[this] val DE: TypeTag[DerivedObjectEncoder[_]] = c.typeTag

  protected[this] def hnilReprDecoder: Tree = q"_root_.io.circe.generic.extras.decoding.ReprDecoder.hnilReprDecoder"

  protected[this] def decodeMethodName: TermName = TermName("apply")
  protected[this] def decodeAccumulatingMethodName: TermName = TermName("decodeAccumulating")
  protected[this] def encodeMethodName: TermName = TermName("encodeObject")

  protected[this] def decodeField(name: String, decode: TermName): Tree =
    q"$decode.tryDecode(c.downField($name))"

  protected[this] def decodeFieldAccumulating(name: String, decode: TermName): Tree =
    q"$decode.tryDecodeAccumulating(c.downField($name))"

  protected[this] def decodeSubtype(name: String, decode: TermName): Tree = q"""
    {
      val result = c.downField($name)

      if (result.succeeded) Some($decode.tryDecode(result)) else None
    }
  """

  protected[this] def decodeSubtypeAccumulating(name: String, decode: TermName): Tree = q"""
    {
      val result = c.downField($name)

      if (result.succeeded) Some($decode.tryDecodeAccumulating(result)) else None
    }
  """

  protected[this] def encodeField(name: String, encode: TermName, value: TermName): Tree =
    q"($name, $encode($value))"

  protected[this] def encodeSubtype(name: String, encode: TermName, value: TermName): Tree =
    q"_root_.io.circe.JsonObject.singleton($name, $encode($value))"
}