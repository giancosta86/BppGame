/*^
  ===========================================================================
  BppGame
  ===========================================================================
  Copyright (C) 2017 Gianluca Costa
  ===========================================================================
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/gpl-3.0.html>.
  ===========================================================================
*/

package info.gianlucacosta.bppgame

import java.io.{FileOutputStream, FileWriter}
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.ScrollPane.ScrollBarPolicy
import javafx.stage.Stage

import info.gianlucacosta.helios.fx.Includes._
import info.gianlucacosta.helios.fx.dialogs.Alerts
import info.gianlucacosta.twobinpack.core.{FrameMode, Problem, Solution}
import info.gianlucacosta.twobinpack.io.FileExtensions
import info.gianlucacosta.twobinpack.io.bpp.{BppProblemWriter, BppSolutionWriter}
import info.gianlucacosta.twobinpack.rendering.frame.Frame
import info.gianlucacosta.twobinpack.rendering.frame.axes.AxesPane
import info.gianlucacosta.twobinpack.rendering.gallery.BlockGalleryPane

import scalafx.Includes._
import scalafx.beans.binding.Bindings
import scalafx.scene.control.Alert.AlertType
import scalafx.stage.{FileChooser, WindowEvent}

/**
  * The controller handling the game
  */
private class GameController {
  var stage: Stage = _


  private val problemFileChooser = new FileChooser {
    extensionFilters.setAll(
      new FileChooser.ExtensionFilter("BPP problem", s"*${FileExtensions.BppProblem}")
    )

    title =
      "Save problem..."
  }


  private val solutionFileChooser = new FileChooser {
    extensionFilters.setAll(
      new FileChooser.ExtensionFilter("BPP solution file", s"*${FileExtensions.BppSolution}")
    )

    title =
      "Save solution..."
  }


  private val solutionImageFileChooser = new FileChooser {
    extensionFilters.setAll(
      new FileChooser.ExtensionFilter("Solution image (.png)", s"*.png")
    )

    title =
      "Save solution as image..."
  }


  private var axesPane: AxesPane = _


  val problemOption =
    new SimpleObjectProperty[Option[Problem]](None)


  private val usedBinsOption =
    new SimpleObjectProperty[Option[Int]]


  private val remainingBlocksOption =
    new SimpleObjectProperty[Option[Int]]


  private val frameOption =
    new SimpleObjectProperty[Option[Frame]](None)


  frameOption.onChange {
    frameOption().foreach(frame => {
      val problem =
        problemOption().get

      usedBinsOption <==
        Bindings.createObjectBinding(
          () => {
            val usedBins =
              frame.quantizedWidth() - 1

            Some(
              usedBins
            )
          },

          frame.quantizedWidth
        )

      remainingBlocksOption <==
        Bindings.createObjectBinding(
          () => {
            Some(
              problem.frameTemplate.blockPool.totalBlockCount -
                frame.blocks().size
            )
          },

          frame.blocks
        )


      galleryScrollPane.content <==
        Bindings.createObjectBinding[Node](
          () => {
            val blockGalleryPane =
              new BlockGalleryPane(
                frame.blockGallery(),
                problem.frameTemplate.colorPalette,
                resolutionSlider.value().toInt,
                sortAscending = false,
                labelTextFormat = "w=%H (%Q)"
              )

            blockGalleryPane.delegate
          },

          frame.blockGallery,
          resolutionSlider.value
        )
    })
  }


  @FXML
  def initialize(): Unit = {
    resolutionSlider.min =
      Problem.MinResolution

    resolutionSlider.max =
      Problem.MaxResolution


    frameOption <==
      Bindings.createObjectBinding[Option[Frame]](
        () => {
          problemOption().map(problem => {
            resolutionSlider.value() =
              problem.frameTemplate.resolution

            new Frame(problem.frameTemplate) {
              resolution <==
                resolutionSlider.value
            }
          })
        },

        problemOption
      )


    usedBinsLabel.text <==
      Bindings.createStringBinding(
        () => {
          usedBinsOption()
            .map(_.toString)
            .getOrElse("")
        },

        usedBinsOption
      )


    blocksLabel.text <==
      Bindings.createStringBinding(
        () => {
          remainingBlocksOption()
              .map(_.toString)
              .getOrElse("")
        },

        remainingBlocksOption
      )


    resolutionLabel.text <==
      Bindings.createStringBinding(
        () =>
          s"${resolutionSlider.value().toInt}",

        resolutionSlider.value
      )


    frameScrollPane.visible <==
      frameOption =!= None


    galleryScrollPane.visible <==
      frameScrollPane.visible


    frameScrollPane.content <==
      Bindings.createObjectBinding[Node](
        () => {
          frameOption().map(frame => {
            axesPane =
              new AxesPane(
                frame,
                showVerticalAxis = false
              )

            axesPane.delegate
          }).orNull
        },

        frameOption
      )


    frameScrollPane.hbarPolicy =
      ScrollBarPolicy.ALWAYS


    saveSolutionButton.disable <==
      Bindings.createBooleanBinding(
        () => {
          remainingBlocksOption().forall(_ > 0)
        },

        remainingBlocksOption
      )


    saveSolutionAsImageButton.disable <==
      saveSolutionButton.disable
  }


  @FXML
  def saveProblem(): Unit = {
    problemFileChooser.title =
      "Save problem..."

    problemFileChooser.initialFileName =
      "Problem"

    val problemFile =
      problemFileChooser.smartSave(stage)

    if (problemFile != null) {
      frameOption().foreach(frame => {
        try {
          val problemWriter =
            new BppProblemWriter(
              new FileWriter(
                problemFile
              )
            )

          try {
            problemWriter.writeBppProblem(problemOption().get)
          } finally {
            problemWriter.close()
          }

          Alerts.showInfo("Problem saved successfully.")
        } catch {
          case ex: Exception =>
            Alerts.showException(ex, alertType = AlertType.Warning)
        }
      })
    }
  }


  @FXML
  def saveSolution(): Unit = {
    solutionFileChooser.initialFileName =
      "Solution"

    val solutionFile =
      solutionFileChooser.smartSave(stage)


    if (solutionFile != null) {
      val solution =
        Solution(
          problemOption().get,
          None,
          None,
          frameOption().get.blocks()
        )

      val solutionWriter =
        new BppSolutionWriter(
          new FileWriter(
            solutionFile
          )
        )

      try {
        try {
          solutionWriter.writeSolution(solution)
        } finally {
          solutionWriter.close()
        }

        Alerts.showInfo("Solution saved successfully.")
      } catch {
        case ex: Exception =>
          Alerts.showException(ex)
      }
    }
  }

  @FXML
  def saveSolutionAsImage(): Unit = {
    solutionImageFileChooser.initialFileName =
      "Solution"


    val solutionImageFile =
      solutionImageFileChooser.smartSave(stage)

    if (solutionImageFile != null) {
      frameOption().foreach(frame => {
        try {
          val imageOutputStream =
            new FileOutputStream(solutionImageFile)

          try {
            axesPane.exportAsImage(imageOutputStream)
          } finally {
            imageOutputStream.close()
          }

          Alerts.showInfo("Image saved successfully.")
        } catch {
          case ex: Exception =>
            Alerts.showException(ex, alertType = AlertType.Warning)
        }
      })
    }
  }


  @FXML
  def closeProblem(): Unit = {
    val closeEvent = new WindowEvent(
      stage,
      WindowEvent.WindowCloseRequest
    )

    stage.fireEvent(closeEvent)
  }


  @FXML
  var usedBinsLabel: javafx.scene.control.Label = _

  @FXML
  var blocksPromptLabel: javafx.scene.control.Label = _

  @FXML
  var blocksLabel: javafx.scene.control.Label = _


  @FXML
  var resolutionSlider: javafx.scene.control.Slider = _

  @FXML
  var resolutionLabel: javafx.scene.control.Label = _


  @FXML
  var gameSplitPane: javafx.scene.control.SplitPane = _

  @FXML
  var frameScrollPane: javafx.scene.control.ScrollPane = _

  @FXML
  var galleryScrollPane: javafx.scene.control.ScrollPane = _

  @FXML
  var saveSolutionButton: javafx.scene.control.Button = _

  @FXML
  var saveSolutionAsImageButton: javafx.scene.control.Button = _
}
