package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.heuristics.SimpleHeuristicV1
import dk.ilios.hivemind.ai.heuristics.TestSetups
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.Player.PlayerType
import org.junit.Assert
import org.junit.Test

class NegamaxAITest {
    @Test
    fun testCanDetectWinTurnOne() {
        val ai: HiveAI = NegamaxAI("Negamax", SimpleHeuristicV1(), 1, 30000)

        var game = Game()
        val p1 = Player("White", PlayerType.WHITE)
        p1.fillBaseSupply()
        val p2 = Player("Black", PlayerType.BLACK)
        p2.fillBaseSupply()

        game.addPlayers(p1, p2)
        game.turnLimit = 10
        game = TestSetups.sureWinInOneTurn(game)

        val command = checkNotNull(ai.nextMove(game, game.getBoard()))

        Assert.assertEquals(1, command.toQ.toLong())
        Assert.assertEquals(1, command.toR.toLong())
    }


    @Test
    fun testCanDetectWinTurnTwo() {
        val ai: HiveAI = NegamaxAI("Negamax", SimpleHeuristicV1(), 3, 30000)

        var game = Game()
        val p1 = Player("White", PlayerType.WHITE)
        p1.fillBaseSupply()
        val p2 = Player("Black", PlayerType.BLACK)
        p2.fillBaseSupply()

        game.addPlayers(p1, p2)
        game.turnLimit = 10
        game = TestSetups.sureWinInTwoTurns(game)

        val command = checkNotNull(ai.nextMove(game, game.getBoard()))

        Assert.assertEquals(2, command.toQ.toLong())
        Assert.assertEquals(1, command.toR.toLong())
    }
}
