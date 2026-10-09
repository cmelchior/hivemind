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
        val white = game.getWhitePlayer()
        val black = game.getBlackPlayer()

        game.setReplayMode(true)
        assertTrue(game.getBoard().filledHexes.isEmpty())
        assertEquals(11, white.supply.size)
        assertEquals(0, white.turns)
        assertSame(white, game.getActivePlayer())

        val whiteMove = game.forward()
        assertSame(white.queen, whiteMove.token)
        assertSame(white.queen, game.getBoard().getHex(0, 0).topToken)
        assertEquals(10, white.supply.size)
        assertEquals(1, white.turns)
        assertSame(black, game.getActivePlayer())

        val blackMove = game.forward()
        assertSame(black.queen, blackMove.token)
        assertSame(black.queen, game.getBoard().getHex(1, 0).topToken)
        assertEquals(10, black.supply.size)
        assertEquals(1, black.turns)
        assertSame(white, game.getActivePlayer())
        assertThrows(IllegalStateException::class.java) { game.forward() }
    }

    @Test
    fun testReplayModeBackwards() {
        val game = recordedGame()
        val white = game.getWhitePlayer()
        val black = game.getBlackPlayer()

        game.setReplayMode(true)
        val whiteMove = game.forward()
        val blackMove = game.forward()

        assertSame(blackMove, game.backwards())
        assertNull(black.queen!!.hex)
        assertEquals(11, black.supply.size)
        assertEquals(0, black.turns)
        assertSame(black, game.getActivePlayer())
        assertEquals(1, game.getBoard().filledHexes.size)

        assertSame(whiteMove, game.backwards())
        assertNull(white.queen!!.hex)
        assertEquals(11, white.supply.size)
        assertEquals(0, white.turns)
        assertSame(white, game.getActivePlayer())
        assertTrue(game.getBoard().filledHexes.isEmpty())
        assertThrows(IllegalStateException::class.java) { game.backwards() }

        // Leaving replay restores the recorded position and permits further moves.
        game.setReplayMode(false)
        assertEquals(2, game.getBoard().filledHexes.size)
        assertEquals(1, white.turns)
        assertEquals(1, black.turns)
        game.continueGame(GameCommand.addFromSupply(white.getFromSupply(BugType.SPIDER), -1, 0))
        assertEquals(3, game.getBoard().filledHexes.size)
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
