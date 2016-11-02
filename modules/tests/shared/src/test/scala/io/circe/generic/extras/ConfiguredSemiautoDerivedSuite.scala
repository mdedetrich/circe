package io.circe.generic.extras

import cats.Eq
import io.circe.{ Decoder, Encoder, ObjectEncoder }
import io.circe.generic.extras.semiauto._
import io.circe.literal._
import io.circe.tests.CirceSuite

class ConfiguredSemiautoDerivedSuite extends CirceSuite {
  implicit val customConfig: Configuration =
    Configuration.default.withSnakeCaseKeys.withDefaults.withDiscriminator("type")

  sealed trait ConfigExampleBase
  case class ConfigExampleFoo(thisIsAField: String, a: Int = 0, b: Double) extends ConfigExampleBase
  case object ConfigExampleBar extends ConfigExampleBase

  object ConfigExampleFoo {
    implicit val eqConfigExampleFoo: Eq[ConfigExampleFoo] = Eq.fromUniversalEquals
  }

  object ConfigExampleBase {
    implicit val eqConfigExampleBase: Eq[ConfigExampleBase] = Eq.fromUniversalEquals
  }

  implicit val decodeConfigExampleBase: Decoder[ConfigExampleBase] = deriveDecoder
  implicit val encodeConfigExampleBase: ObjectEncoder[ConfigExampleBase] = deriveEncoder

  "Semi-automatic derivation" should "support configuration" in forAll { (f: String, b: Double) =>
    val foo: ConfigExampleBase = ConfigExampleFoo(f, 0, b)
    val json = json"""{ "type": "ConfigExampleFoo", "this_is_a_field": $f, "b": $b}"""
    val expected = json"""{ "type": "ConfigExampleFoo", "this_is_a_field": $f, "a": 0, "b": $b}"""

    assert(Encoder[ConfigExampleBase].apply(foo) === expected)
    assert(Decoder[ConfigExampleBase].decodeJson(json) === Right(foo))
  }
}
