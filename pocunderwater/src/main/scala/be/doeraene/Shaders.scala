package be.doeraene

import indigo.*
import indigo.scenes.*
import ultraviolet.syntax.*

import scala.annotation.nowarn

/** A `BlendMaterial` is nothing more than a class that provides the `ShaderData` used to tell
  * Indigo how to merge one layer down onto the layer below it.
  *
  * In terms of the process, each layer is first rendered, and then the blending is performed. In
  * effect, you are combining to images of the same size in the same location.
  */
final case class CustomBlendMaterial() extends BlendMaterial:
  def toShaderData: ShaderData =
    ShaderData(CustomBlendShader.shader.id)

final case class UnderwaterBlendMaterial() extends BlendMaterial:
  def toShaderData: ShaderData =
    ShaderData(UnderwaterBlendShader.shader.id)

/** The structure of a blend shader is _very_ similar that seen in the basic entity shader example.
  * There are a number of differences though that all boil down to telling Indigo to expect a blend
  * shader instead of an entity shader.
  *
  * In terms of the actual code, the main thing to note is that the `BlendFragmentEnv` has replaced
  * our usual `FragmentEnv`, and this gives us access to `env.SRC` and `env.DST`, which are the
  * color values of the current pixel on each layer, where src is merging down to dst.
  *
  * `env.SRC` and `env.DST` are convenience variables that you get for free, but you can always
  * change which pixel you'd like to reference using
  * `texture2D(SRC_CHANNEL, <some custom UV coordinate)`.
  *
  * This shader mixes the two layers together 50-50.
  */
object CustomBlendShader {
  val shader: ShaderProgram =
    UltravioletShader.blendFragment(
      ShaderId("custom-blend-shader"),
      BlendShader.fragment[BlendFragmentEnv](fragment, BlendFragmentEnv.reference)
    )

  @nowarn("msg=unused")
  inline def fragment: Shader[BlendFragmentEnv, Unit] =
    Shader[BlendFragmentEnv] { env =>
      def fragment(color: vec4): vec4 =
        mix(env.DST, env.SRC, 0.5f)
    }
}

object UnderwaterBlendShader {
  val shader: ShaderProgram =
    UltravioletShader.blendFragment(
      ShaderId("custom-blend-shader"),
      BlendShader.fragment[BlendFragmentEnv](fragment, BlendFragmentEnv.reference)
    )

  final val DefaultMaxAmplitude = 2.5f
  final val DefaultPeriod = 3000f
  final val DefaultWaveHeight = 23f

  @nowarn("msg=unused")
  inline def fragment: Shader[BlendFragmentEnv, Unit] = {
    Shader[BlendFragmentEnv] { env =>
      def fragment(color: vec4): vec4 =
        val period = 3f
        val theta = env.TIME % period * env.TAU / period
        val amplitude = sin(theta) * 2.5f
        val offset: Float = sin(env.UV.y * env.SIZE.y / 8.0f) * amplitude
        val offsetPos = vec2(env.UV.x + offset / env.SIZE.x, env.UV.y)

        val orig = texture2D(env.DST_CHANNEL, offsetPos)
        mix(orig, vec4(0x00/255.0f, 0x50/255.0f, 0xd0/255.0f, 0xff/255.0f), 0.5f)
    }
  }
}
