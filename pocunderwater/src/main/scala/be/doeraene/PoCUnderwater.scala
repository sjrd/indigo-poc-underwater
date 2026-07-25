package be.doeraene

import be.doeraene.generated.Assets
import be.doeraene.generated.Config
import indigo.*
import indigo.scenes.*
import ultraviolet.syntax.*

import scala.annotation.nowarn
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object PoCUnderwater extends IndigoSandbox[Unit, Unit] {
  val config: GameConfig =
    Config.config.noResize
      .withMagnification(1)

  val assets: Set[AssetType] =
    Assets.assets.assetSet

  val fonts: Set[FontInfo]       = Set()
  val animations: Set[Animation] = Set()

  val shaders: Set[ShaderProgram] =
    //println(UnderwaterBlendShader.fragment.toGLSL[WebGL2].toOutput.code)
    Set(
      //CustomEntityShader.shader,
      //CustomBlendShader.shader,
      UnderwaterBlendShader.shader,
      StoreAlphaMaskBlendShader.shader,
      ApplyAlphaMaskBlendShader.shader,
    )

  def setup(assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def updateModel(context: Context[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  /** In this scene we render 3 boxes, using 4 layers (mostly for clarity) and 1 blend material.
    *
    * Left to right:
    *
    *   - Box 1 is a simple graphic with a bitmap texture
    *   - Box 2 is a custom entity running the same custom shader we used in the basic entity shader
    *     example.
    *   - Box 3 is made by rendering Box 1 on one layer, then rendering Box 2 on a layer above it,
    *     and giving this layer our custom blend material explaining how we'd like it to be merge to
    *     the layer below.
    *
    * In this instance the example shows all the arguments you can supply to a `Blending` instance,
    * but you can also construct them with only the `BlendMaterial`, or even skip it and replace
    * `withBlending` with `withBlendMaterial`.
    */
  def present(context: Context[Unit], model: Unit): Outcome[SceneUpdateFragment] = {
    val gap = 2

    val grass = Graphic(0, 0, 30, 30, Material.Bitmap(Assets.assets.grass))
    val wall = Graphic(0, 0, 30, 30, Material.Bitmap(Assets.assets.wall))
    val water = Graphic(0, 0, 30, 30, Material.Bitmap(Assets.assets.water))
    val smallice = Graphic(0, 0, 30, 30, Material.Bitmap(Assets.assets.smallice))
    val sand = Graphic(0, 0, 30, 30, Material.Bitmap(Assets.assets.sand))

    val grid =
      for
        i <- 0 until 10
        j <- 0 until 10
      yield
        val g = if (i + j) % 2 == 0 then grass else wall
        g.moveTo(i * 30, j * 30)

    val storeAlphaMaskBlending = Blending(
      entity = Blend.Normal,
      layer = Blend.Add(BlendFactor.One, BlendFactor.Zero),
      blendMaterial = StoreAlphaMaskBlendMaterial(),
      clearColor = None,
    )

    val applyAlphaMaskBlending = Blending(
      entity = Blend.Normal,
      layer = Blend.Normal,// Blend.Add(BlendFactor.One, BlendFactor.Zero),
      blendMaterial = ApplyAlphaMaskBlendMaterial(),
      clearColor = None,
    )

    Outcome(
      SceneUpdateFragment(
        Layer(grid*),
        Layer(
          //Shape.Box(Rectangle(300, 150), Fill.Color(RGBA.fromHexString("0050d0ff")))
        )
          .withBlending(
            Blending(
              entity = Blend.Normal,
              layer = Blend.Normal,
              blendMaterial = UnderwaterBlendMaterial(),
              clearColor = None
            )
          ),
        Layer(
          water.moveTo(30, 30),
          water.moveTo(60, 30),
          water.moveTo(30, 60),
          water.moveTo(60, 60),
          grass.moveTo(0, 0),
          smallice.moveTo(30, 0),
          smallice.moveTo(60, 0),
          grass.moveTo(0, 30),
          grass.moveTo(0, 60),
          wall.moveTo(90, 0),
          wall.moveTo(90, 30),
          wall.moveTo(90, 60),
          wall.moveTo(90, 90),
          wall.moveTo(0, 90),
          wall.moveTo(30, 90),
          wall.moveTo(60, 90),
        ),
        Layer(
          Shape.Box(Rectangle(30, 30, 30, 30), Fill.LinearGradient(Point(0, 0), RGBA.Red, Point(10, 0), RGBA.Red.withAlpha(0.0))),
        ).withBlending(storeAlphaMaskBlending),
        Layer(
          grass.moveTo(30, 30),
        ).withBlending(applyAlphaMaskBlending),
        Layer(
          Shape.Box(Rectangle(30, 60, 30, 30), Fill.LinearGradient(Point(0, 0), RGBA.Red, Point(10, 0), RGBA.Red.withAlpha(0.0))),
        ).withBlending(storeAlphaMaskBlending),
        Layer(
          grass.moveTo(30, 60),
        ).withBlending(applyAlphaMaskBlending),
        Layer(
          Shape.Box(Rectangle(30, 30, 30, 30), Fill.LinearGradient(Point(0, 0), RGBA.Red, Point(0, 10), RGBA.Red.withAlpha(0.0))),
        ).withBlending(storeAlphaMaskBlending),
        Layer(
          smallice.moveTo(30, 30),
        ).withBlending(applyAlphaMaskBlending),
        Layer(
          Shape.Box(Rectangle(60, 30, 30, 30), Fill.LinearGradient(Point(0, 0), RGBA.Red, Point(0, 10), RGBA.Red.withAlpha(0.0))),
        ).withBlending(storeAlphaMaskBlending),
        Layer(
          smallice.moveTo(60, 30),
        ).withBlending(applyAlphaMaskBlending),
        /*Layer(
          Graphic(0, 0, 64, 64, Material.Bitmap(Assets.assets.nineslice))
            .moveTo(10, 10)
        ),
        Layer(
          BlankEntity(0, 0, 64, 64, ShaderData(CustomEntityShader.shader.id))
            .moveTo(10 + 64 + gap, 10)
        ),
        Layer(
          Graphic(0, 0, 64, 64, Material.Bitmap(Assets.assets.nineslice))
            .moveTo(10 + 64 + gap + 64 + gap, 10)
        ),
        Layer(
          BlankEntity(0, 0, 64, 64, ShaderData(CustomEntityShader.shader.id))
            .moveTo(10 + 64 + gap + 64 + gap, 10)
        ).withBlending(
          Blending(
            entity = Blend.Normal,
            layer = Blend.Normal,
            blendMaterial = CustomBlendMaterial(),
            clearColor = None
          )
        )*/
      )
    )
  }
}

object CustomEntityShader {
  val shader: ShaderProgram =
    UltravioletShader.entityFragment(
      ShaderId("custom-entity-shader"),
      EntityShader.fragment[FragmentEnv](fragment, FragmentEnv.reference)
    )

  @nowarn("msg=unused")
  inline def fragment: Shader[FragmentEnv, Unit] =
    Shader[FragmentEnv] { env =>
      def fragment(color: vec4): vec4 =
        val col: vec3 = 0.5f + 0.5f * cos(env.TIME + env.UV.xyx + vec3(0.0f, 2.0f, 4.0f))
        vec4(col, 1.0f)
    }
}
