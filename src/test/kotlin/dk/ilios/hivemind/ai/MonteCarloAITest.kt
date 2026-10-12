package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.heuristics.TestSetups
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.Player.PlayerType
import org.junit.Assert
import org.junit.Test

class MonteCarloAITest {
    @Test
    fun testCanDetectWinTurnOne() {
        val ai: HiveAI = UCTMonteCarloTreeSearchAI("MCTS", 1, 10000)

        var game = Game()
        val p1 = Player("White", PlayerType.WHITE)
        p1.fillBaseSupply()
        val p2 = Player("Black", PlayerType.BLACK)
        p2.fillBaseSupply()

        game.addPlayers(p1, p2)
        game.turnLimit = 10
        game = TestSetups.sureWinInOneTurn(game)

        val command = checkNotNull(ai.nextMove(game, game.board))

        Assert.assertEquals(1, command.toQ.toLong())
        Assert.assertEquals(1, command.toR.toLong())
    }
}
