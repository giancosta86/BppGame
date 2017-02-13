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

import javafx.scene.layout.BorderPane

import info.gianlucacosta.helios.apps.AppInfo
import info.gianlucacosta.helios.fx.Includes._
import info.gianlucacosta.helios.fx.dialogs.InputDialogs
import info.gianlucacosta.helios.fx.scene.fxml.FxmlScene
import info.gianlucacosta.helios.fx.stage.StackedStage
import info.gianlucacosta.twobinpack.core.Problem

import scalafx.Includes._
import scalafx.stage.WindowEvent


/**
  * Stage where the game actually takes place
  */
private class GameStage(
                         appInfo: AppInfo,
                         val previousStage: javafx.stage.Stage
                       ) extends StackedStage {

  private var gameController: GameController = _


  title =
    appInfo.name

  this.setMainIcon(appInfo)

  /**
    * Starts a new game
    *
    */
  def startGame(problem: Problem): Unit = {
    val fxmlScene =
      new FxmlScene[GameController, BorderPane](classOf[GameController]) {
        override protected def preInitialize(): Unit = {
          gameController =
            controller

          controller.stage =
            GameStage.this

          controller.problemOption() =
            Some(problem)
        }
      }


    scene =
      fxmlScene

    maximized =
      true

    show()
  }


  handleEvent(WindowEvent.WindowCloseRequest) {
    (event: WindowEvent) => {
      if (!canClose) {
        event.consume()
      }
    }
  }


  private def canClose: Boolean =
    InputDialogs.askYesNoCancel(
      "Return to the main screen?"
    ).contains(true)
}
