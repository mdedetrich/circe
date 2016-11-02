package io.circe.generic.extras

import io.circe.generic.extras.decoding.{ ConfiguredDecoder, ReprDecoder }
import io.circe.generic.extras.encoding.{ ConfiguredObjectEncoder, ReprObjectEncoder }
import io.circe.generic.util.macros.DerivationMacros
import macrocompat.bundle
import scala.reflect.macros.whitebox

@bundle
class ConfigurableDeriver(val c: whitebox.Context)
    extends DerivationMacros[ReprDecoder, ReprObjectEncoder, ConfiguredDecoder, ConfiguredObjectEncoder] {
  import c.universe._

  def deriveDecoder[R: c.WeakTypeTag]: c.Expr[ReprDecoder[R]] = c.Expr[ReprDecoder[R]](constructDecoder[R])
  def deriveEncoder[R: c.WeakTypeTag]: c.Expr[ReprObjectEncoder[R]] = c.Expr[ReprObjectEncoder[R]](constructEncoder[R])

  protected[this] val RD: TypeTag[ReprDecoder[_]] = c.typeTag
  protected[this] val RE: TypeTag[ReprObjectEncoder[_]] = c.typeTag
  protected[this] val DD: TypeTag[ConfiguredDecoder[_]] = c.typeTag
  protected[this] val DE: TypeTag[ConfiguredObjectEncoder[_]] = c.typeTag

  protected[this] def hnilReprDecoder: Tree = q"_root_.io.circe.generic.extras.decoding.ReprDecoder.hnilReprDecoder"

  protected[this] def decodeMethodName: TermName = TermName("configuredDecode")
  protected[this] def decodeAccumulatingMethodName: TermName = TermName("configuredDecodeAccumulating")

  protected[this] override def decodeMethodArgs: List[Tree] = List(
    q"transformKeys: (_root_.java.lang.String => _root_.java.lang.String)",
    q"defaults: _root_.scala.collection.immutable.Map[_root_.java.lang.String, _root_.scala.Any]",
    q"discriminator: _root_.scala.Option[_root_.java.lang.String]"
  )


  protected[this] def encodeMethodName: TermName = TermName("configuredEncodeObject")
  protected[this] override def encodeMethodArgs: List[Tree] = List(
    q"transformKeys: (_root_.java.lang.String => _root_.java.lang.String)",
    q"discriminator: _root_.scala.Option[_root_.java.lang.String]"
  )

  protected[this] def decodeField(name: String, decode: TermName): Tree = q"""
    orDefault(
      $decode.tryDecode(c.downField(transformKeys($name))),
      $name,
      defaults
    )
  """

  protected[this] def decodeFieldAccumulating(name: String, decode: TermName): Tree = q"""
    orDefaultAccumulating(
      $decode.tryDecodeAccumulating(c.downField(transformKeys($name))),
      $name,
      defaults
    )
  """

  protected[this] def decodeSubtype(name: String, decode: TermName): Tree = q"""
    withDiscriminator(
      $decode,
      c,
      $name,
      discriminator
    )
  """

  protected[this] def decodeSubtypeAccumulating(name: String, decode: TermName): Tree = q"""
    withDiscriminatorAccumulating(
      $decode,
      c,
      $name,
      discriminator
    )
  """

  protected[this] def encodeField(name: String, encode: TermName, value: TermName): Tree =
    q"(transformKeys($name), $encode($value))"

  protected[this] def encodeSubtype(name: String, encode: TermName, value: TermName): Tree =
    q"addDiscriminator($encode, $value, $name, discriminator)"
}