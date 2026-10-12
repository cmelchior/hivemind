package dk.ilios.hivemind.model

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Player.PlayerType
import org.junit.Assert.*
import org.junit.Test

class GameTest {
    @Test
    fun testReplayModeForward() {
        val game = recordedGame()
        val white = game.whitePlayer
        val black = game.blackPlayer

        game.setReplayMode(true)
        assertTrue(game.board.filledHexes.isEmpty())
        assertEquals(11, white.supply.size)
        assertEquals(0, white.turns)
        assertSame(white, game.activePlayer)

        val whiteMove = game.forward()
        assertSame(white.queen, whiteMove.token)
        assertSame(white.queen, game.board.getHex(0, 0).topToken)
        assertEquals(10, white.supply.size)
        assertEquals(1, white.turns)
        assertSame(black, game.activePlayer)

        val blackMove = game.forward()
        assertSame(black.queen, blackMove.token)
        assertSame(black.queen, game.board.getHex(1, 0).topToken)
        assertEquals(10, black.supply.size)
        assertEquals(1, black.turns)
        assertSame(white, game.activePlayer)
        assertThrows(IllegalStateException::class.java) { game.forward() }
    }

    @Test
    fun testReplayModeBackwards() {
        val game = recordedGame()
        val white = game.whitePlayer
        val black = game.blackPlayer

        game.setReplayMode(true)
        val whiteMove = game.forward()
        val blackMove = game.forward()

        assertSame(blackMove, game.backwards())
        assertNull(black.queen!!.hex)
        assertEquals(11, black.supply.size)
        assertEquals(0, black.turns)
        assertSame(black, game.activePlayer)
        assertEquals(1, game.board.filledHexes.size)

        assertSame(whiteMove, game.backwards())
        assertNull(white.queen!!.hex)
        assertEquals(11, white.supply.size)
        assertEquals(0, white.turns)
        assertSame(white, game.activePlayer)
        assertTrue(game.board.filledHexes.isEmpty())
        assertThrows(IllegalStateException::class.java) { game.backwards() }

        // Leaving replay restores the recorded position and permits further moves.
        game.setReplayMode(false)
        assertEquals(2, game.board.filledHexes.size)
        assertEquals(1, white.turns)
        assertEquals(1, black.turns)
        game.continueGame(GameCommand.addFromSupply(white.getFromSupply(BugType.SPIDER), -1, 0))
        assertEquals(3, game.board.filledHexes.size)
        assertEquals(2, white.turns)
    }

    private fun recordedGame(): Game {
        val white = Player("White", PlayerType.WHITE).apply { fillBaseSupply() }
        val black = Player("Black", PlayerType.BLACK).apply { fillBaseSupply() }
        return Game().apply {
            addPlayers(white, black)
            setManualStepping(true)
            start()
            continueGame(GameCommand.addFromSupply(white.getFromSupply(BugType.QUEEN_BEE), 0, 0))
            continueGame(GameCommand.addFromSupply(black.getFromSupply(BugType.QUEEN_BEE), 1, 0))
        }
    }
}
