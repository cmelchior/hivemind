package dk.ilios.hivemind.ai.moves

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.BugType
import dk.ilios.hivemind.model.HexagonUtils
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.Player.PlayerType
import org.junit.Assert.*
import org.junit.Test

class MoveGeneratorTest {
    @Test
    fun standardGeneratorAcceptsReadOnlyInputAndPreservesGameState() {
        val game = startedGame()
        val initialMoves = listOf(GameCommand.PASS)

        val moves = StandardMoveGenerator().generateMoves(initialMoves, game)

        assertEquals(listOf(GameCommand.PASS), initialMoves)
        assertSame(GameCommand.PASS, moves.first())
        assertTrue(moves.size > initialMoves.size)
        assertEquals(2, game.board.filledHexes.size)
        assertEquals(10, game.whitePlayer.supply.size)
        assertEquals(1, game.whitePlayer.turns)
        assertSame(game.whitePlayer, game.activePlayer)
    }

    @Test
    fun aggressiveGeneratorKeepsMovesNearTheOpposingQueenFirst() {
        val game = startedGame()
        val initialMoves = listOf(GameCommand.PASS)
        val queen = checkNotNull(game.blackPlayer.queen!!.hex)

        val moves = AggressiveMovesFirstGenerator().generateMoves(initialMoves, game)
        val distances = moves.map { HexagonUtils.distance(queen.q, queen.r, it.toQ, it.toR) }
        val firstDistantMove = distances.indexOfFirst { it > 1 }

        assertEquals(listOf(GameCommand.PASS), initialMoves)
        assertTrue(firstDistantMove > 0)
        assertTrue(distances.take(firstDistantMove).all { it <= 1 })
        assertTrue(distances.drop(firstDistantMove).all { it > 1 })
        assertEquals(2, game.board.filledHexes.size)
    }

    private fun startedGame(): Game {
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
