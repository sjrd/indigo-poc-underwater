import scala.sys.process._
import scala.language.postfixOps

import sbtwelcome._
import indigoplugin._

Global / onChangedBuildSource := ReloadOnSourceChanges

Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }

lazy val gameOptions: IndigoOptions =
  IndigoOptions.defaults
    .withTitle("PoCUnderwater")
    .withWindowSize(550, 400)
    .withBackgroundColor("black")
    .withAssetDirectory("assets")
    .excludeAssets {
      case p if p.endsWith(os.RelPath.rel / ".gitkeep") => true
      case _                                            => false
    }

lazy val pocunderwater =
  (project in file("pocunderwater"))
    .enablePlugins(ScalaJSPlugin, SbtIndigo)
    .settings( // Normal SBT settings
      name         := "pocunderwater",
      version      := "0.0.1",
      scalaVersion := "3.7.2",
      organization := "be.doeraene",
      libraryDependencies ++= Seq(
        "org.scalameta" %%% "munit" % "1.1.1" % Test
      ),
      testFrameworks += new TestFramework("munit.Framework"),
      scalafixOnCompile  := true,
      semanticdbEnabled  := true,
      semanticdbVersion  := scalafixSemanticdb.revision,
    )
    .settings( // Indigo specific settings
      indigoOptions := {
        val electronPath =
          ((LocalRootProject / baseDirectory).value / "node_modules/electron/dist/electron").getAbsolutePath()
        gameOptions
          .withElectronInstallType(ElectronInstall.PathToExecutable(
            s"$electronPath --no-sandbox"))
      },
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "indigo-json-circe" % "0.22.0",
        "io.indigoengine" %%% "indigo"            % "0.22.0",
        "io.indigoengine" %%% "indigo-extras"     % "0.22.0"
      ),
      Compile / sourceGenerators += Def.task {
        IndigoGenerators("be.doeraene.generated")
          .generateConfig("Config", gameOptions)
          .listAssets("Assets", gameOptions.assets)
          .toSourceFiles((Compile / sourceManaged).value)
      }
    )

lazy val indigo =
  (project in file("."))
    .settings(
      logo := "PoCUnderwater (v" + version.value.toString + ")",
      usefulTasks := Seq(
        UsefulTask("runGame", "Run the game").noAlias,
        UsefulTask("buildGame", "Build web version").noAlias,
        UsefulTask("runGameFull", "Run the fully optimised game").noAlias,
        UsefulTask("buildGameFull", "Build the fully optimised web version").noAlias
      ),
      logoColor        := scala.Console.MAGENTA,
      aliasColor       := scala.Console.YELLOW,
      commandColor     := scala.Console.CYAN,
      descriptionColor := scala.Console.WHITE
    )
    .aggregate(pocunderwater)

addCommandAlias(
  "buildGame",
  List(
    "pocunderwater/compile",
    "pocunderwater/fastLinkJS",
    "pocunderwater/indigoBuild"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "buildGameFull",
  List(
    "pocunderwater/compile",
    "pocunderwater/fullLinkJS",
    "pocunderwater/indigoBuildFull"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "runGame",
  List(
    "pocunderwater/compile",
    "pocunderwater/fastLinkJS",
    "pocunderwater/indigoRun"
  ).mkString(";", ";", "")
)
addCommandAlias(
  "runGameFull",
  List(
    "pocunderwater/compile",
    "pocunderwater/fullLinkJS",
    "pocunderwater/indigoRunFull"
  ).mkString(";", ";", "")
)
