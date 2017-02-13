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

import java.io.{BufferedReader, InputStreamReader}
import java.util.UUID

import info.gianlucacosta.twobinpack.core.Problem
import info.gianlucacosta.twobinpack.io.bpp.BppProblemReader

import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.MenuItem


/**
  * Menu item related to a sample problem: when clicked, it starts such problem
  */
private class SampleProblemMenuItem(
                                     itemText: String,
                                     gameStage: GameStage
                                   ) extends MenuItem {
  text =
    itemText


  lazy val problem: Problem = {
    val problemResourceName =
      itemText
        .toLowerCase
        .replaceAll(" ", "_")

    val problemStream =
      getClass.getResourceAsStream(s"problems/${problemResourceName}.bpp")


    val problemReader =
      new BppProblemReader(
        new BufferedReader(
          new InputStreamReader(
            problemStream
          )
        )
      )


    try {
      problemReader.readBppProblem(
        initialFrameWidth = 1,
        timeLimitOption = None,
        "BPP problem",
        UUID.randomUUID()
      )
    } finally {
      problemReader.close()
    }
  }


  handleEvent(ActionEvent.Action) {
    (event: ActionEvent) => {
      gameStage.startGame(problem)
    }
  }
}
