package dk.ilios.hivemind.ai.heuristics

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.model.BugType
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.Player.PlayerType
import dk.ilios.hivemind.model.StandardPositionMode
import dk.ilios.hivemind.model.Token
import dk.ilios.hivemind.model.rules.Rules
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SimpleHeuristicTests {
    private val printer = HiveAsciiPrettyPrinter()
    private lateinit var heuristic: BoardValueHeuristic
    private val game: Game? = null

    @Before
    fun setup() {
        heuristic = SimpleHeuristicV1()
    }

    private fun newGame(): Game {
        val game = Game()
        val p1 = Player("White", PlayerType.WHITE)
        p1.fillBaseSupply()
        val p2 = Player("Black", PlayerType.BLACK)
        p2.fillBaseSupply()

        game.addPlayers(p1, p2)
        game.turnLimit = 10
        game.setStandardPositionMode(StandardPositionMode.DISABLED)
        return game
    }

    @Test
    fun testWinSoonerThanLater_1() {
        val g1 = newGame()
        val g2 = newGame()

        TestSetups.sureWinInTwoTurns(g1)
        TestSetups.sureWinInTwoTurns(g2)

        g1.getBoard().moveToken(-1, 4, 2, 1) // Move A1 to T1
        g2.getBoard().addToken(g2.getBlackPlayer().getFromSupply(BugType.GRASSHOPPER), -1, 2)

        val score1 = heuristic.calculateBoardValue(g1)
        val score2 = heuristic.calculateBoardValue(g2)

        Assert.assertTrue(score1 < score2) // Black is scored negative
    }

    @Test
    fun testWinSoonerThanLater_2() {
        val g1 = newGame()
        val g2 = newGame()

        TestSetups.sureWinInTwoTurns(g1)
        TestSetups.sureWinInTwoTurns(g2)

        g1.getBoard().moveToken(-1, 4, 2, 1) // Move A1 to T1
        g2.getBoard().moveToken(-1, 4, 2, 3) // WTF?

        val score1 = heuristic.calculateBoardValue(g1)
        val score2 = heuristic.calculateBoardValue(g2)

        Assert.assertTrue(score1 < score2) // Black is scored negative
    }

    @Test
    fun testSame() {
        val g1 = newGame()
        val g2 = newGame()

        TestSetups.sureWinInTwoTurns(g1)
        TestSetups.sureWinInTwoTurns(g2)

        g1.getBoard().moveToken(-1, 4, 0, 2) // Ant moved
        g2.getBoard().moveToken(-1, 3, 0, 2) // Beetle moved

        val score1 = heuristic.calculateBoardValue(g1)
        val score2 = heuristic.calculateBoardValue(g2)

        val free: MutableSet<Token>? = Rules.instance.getFreeTokens(g1.getBlackPlayer(), g1.getBoard())
        val free2: MutableSet<Token>? = Rules.instance.getFreeTokens(g2.getBlackPlayer(), g2.getBoard())

        Assert.assertTrue(score1 == score2) // Black is scored negative
    }
}
