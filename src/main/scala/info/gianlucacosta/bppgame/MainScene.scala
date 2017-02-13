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

import java.io.{BufferedReader, FileReader}
import java.util.UUID
import javafx.stage.Stage

import info.gianlucacosta.helios.apps.AppInfo
import info.gianlucacosta.helios.fx.Includes._
import info.gianlucacosta.helios.fx.dialogs.about.AboutBox
import info.gianlucacosta.helios.fx.dialogs.{Alerts, InputDialogs}
import info.gianlucacosta.twobinpack.core._
import info.gianlucacosta.twobinpack.io.FileExtensions
import info.gianlucacosta.twobinpack.io.bpp.BppProblemReader

import scala.annotation.tailrec
import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Button, Label, MenuButton, MenuItem}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.scene.text.{Font, FontWeight, TextAlignment}
import scalafx.stage.FileChooser

private object MainScene {
  private val DefaultButtonWidth =
    250
}


/**
  * The scene shown in the primary stage, when the app starts
  */
private class MainScene(appInfo: AppInfo, primaryStage: Stage) extends Scene {
  private val gameStage: GameStage =
    new GameStage(appInfo, primaryStage)


  private lazy val aboutBox: AboutBox =
    new AboutBox(appInfo)


  private val problemFileChooser = new FileChooser {
    extensionFilters.setAll(
      new FileChooser.ExtensionFilter("BPP problem", s"*${FileExtensions.BppProblem}")
    )

    title =
      "Open problem..."
  }


  root =
    new BorderPane {
      style =
        "-fx-background-color: linear-gradient(to bottom right, #1945b3, #7d9be7);"

      padding =
        Insets(30)


      top =
        new Label {
          text =
            appInfo.name

          style =
            "-fx-text-fill: linear-gradient(to bottom, #f9e85b, #fff2c4);"

          textAlignment =
            TextAlignment.Center

          alignment =
            Pos.Center

          font =
            Font.font("Arial", FontWeight.Bold, 56)

          margin =
            Insets(20, 0, 0, 0)

          maxWidth =
            Double.MaxValue
        }


      center =
        new VBox {
          padding =
            Insets(40, 20, 20, 20)

          prefWidth =
            700

          prefHeight =
            450


          spacing =
            50

          alignment =
            Pos.Center


          children = List(
            new Button {
              text =
                "New problem..."

              prefWidth =
                MainScene.DefaultButtonWidth + 150

              prefHeight =
                70


              handleEvent(MouseEvent.MouseClicked) {
                (event: MouseEvent) => {
                  createProblem()
                }
              }
            },


            new Button {
              text =
                "Open problem..."

              prefWidth =
                MainScene.DefaultButtonWidth

              prefHeight =
                45


              handleEvent(MouseEvent.MouseClicked) {
                (event: MouseEvent) => {
                  openProblem()
                }
              }
            },


            new MenuButton {
              text =
                "Sample problems"

              alignment =
                Pos.Center


              prefWidth =
                MainScene.DefaultButtonWidth

              prefHeight =
                45


              items = List(
                new SampleProblemMenuItem(
                  "Very easy",
                  gameStage
                ),

                new SampleProblemMenuItem(
                  "Easy",
                  gameStage
                ),


                new SampleProblemMenuItem(
                  "Medium",
                  gameStage
                ),


                new SampleProblemMenuItem(
                  "Difficult",
                  gameStage
                ),

                new SampleProblemMenuItem(
                  "Very difficult",
                  gameStage
                )
              )
            },


            new Button {
              text =
                "About..."

              prefWidth =
                MainScene.DefaultButtonWidth

              prefHeight =
                45


              handleEvent(MouseEvent.MouseClicked) {
                (event: MouseEvent) => {
                  aboutBox.showAndWait()
                  ()
                }
              }
            }
          )
        }
    }


  private def createProblem(): Unit = {
    val createProblemHeader =
      "Create problem..."


    val binSizeOption =
      InputDialogs.askForLong(
        "Bin size:",
        initialValue = 1,
        minValue = 1,
        maxValue = Constraints.MaxBinCapacity,
        header = createProblemHeader
      )
        .map(_.toInt)


    binSizeOption.foreach(binSize => {
      val itemsCountOption =
        InputDialogs.askForLong(
          "Number of items:",
          initialValue = 1,
          minValue = 1,
          maxValue = Constraints.MaxBlockCountPerProblem,
          header = createProblemHeader
        )
          .map(_.toInt)


      itemsCountOption
        .foreach(itemsCount => {
          val blockPoolOption =
            askForItems(
              binSize,
              itemsCount
            )

          blockPoolOption.foreach(blockPool => {
            val frameTemplate =
              FrameTemplate(
                FrameDimension(
                  1,
                  binSize
                ),

                FrameMode.Strip,

                blockPool,

                FrameTemplate.SuggestedBlockColorsPool,

                Problem.SuggestedResolution
              )


            val problem =
              Problem(
                frameTemplate,
                None,
                "BPP Problem"
              )


            gameStage.startGame(
              problem
            )
          })
        })
    })
  }


  private def askForItems(binSize: Int, itemsCount: Int): Option[BlockPool] = {
    askForItems(
      binSize,
      Map(),
      1,
      itemsCount
    )
  }


  @tailrec
  private def askForItems(
                            binSize: Int,
                            cumulatedBlocks: Map[BlockDimension, Int],
                            currentItemOrdinal: Int,
                            itemsToAskCount: Int
                          ): Option[BlockPool] = {
    itemsToAskCount match {
      case 0 =>
        Some(
          BlockPool.create(
            canRotateBlocks = false,
            draftBlocks = cumulatedBlocks
          )
        )


      case _ =>
        val currentItemSizeOption =
          InputDialogs.askForLong(
            s"Size of item #${currentItemOrdinal}:",
            initialValue = 1,
            minValue = 1,
            maxValue = binSize
          )
            .map(_.toInt)


        currentItemSizeOption match {
          case Some(currentItemSize) =>

            val currentBlockDimension =
              BlockDimension(
                1,
                currentItemSize
              )


            val newBlockQuantity =
              cumulatedBlocks.getOrElse(
                currentBlockDimension,
                0
              ) + 1


            val newCumulatedBlocks =
              cumulatedBlocks.updated(
                currentBlockDimension,
                newBlockQuantity
              )

            askForItems(
              binSize,
              newCumulatedBlocks,
              currentItemOrdinal + 1,
              itemsToAskCount - 1
            )


          case None =>
            None
        }
    }
  }


  private def openProblem(): Unit = {
    val problemFile =
      problemFileChooser.smartOpen(primaryStage)

    if (problemFile != null) {
      try {
        val problemReader =
          new BppProblemReader(
            new BufferedReader(
              new FileReader(
                problemFile
              )
            )
          )

        try {
          val problem =
            problemReader.readBppProblem(
              1,
              None,
              "BPP problem",
              UUID.randomUUID()
            )

          gameStage.startGame(
            problem
          )
        } finally {
          problemReader.close()
        }
      } catch {
        case ex: Exception =>
          ex.printStackTrace(System.out)

          Alerts.showException(ex, alertType = AlertType.Warning)
      }
    }
  }
}
