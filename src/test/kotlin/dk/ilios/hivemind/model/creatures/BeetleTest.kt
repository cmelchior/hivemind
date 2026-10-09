package dk.ilios.hivemind.model.creatures

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter
import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.BugType
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.Player.PlayerType
import dk.ilios.hivemind.model.rules.Rules
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class BeetleTest {
    lateinit var printer: HiveAsciiPrettyPrinter
    lateinit var p1: Player
    lateinit var p2: Player

    @Before
    fun setup() {
        printer = HiveAsciiPrettyPrinter()
        p1 = Player("White", PlayerType.WHITE)
        p1.fillBaseSupply()
        p2 = Player("Black", PlayerType.BLACK)
        p2.fillBaseSupply()
    }

    @Test
    fun testTargetSquares_startingPosition() {
        val board = Board(p1, p2)
        val bee = p1.getFromSupply(BugType.BEETLE)
        val ant = p2.getFromSupply(BugType.SOLDIER_ANT)
        board.addToken(bee, 0, 0)
        board.addToken(ant, 1, -1)

        val targets = checkNotNull(Rules.instance.getTargetHexes(bee, board))
        Assert.assertEquals(3, targets.size.toLong())
        Assert.assertEquals(board.getHex(0, -1), targets.get(0))
        Assert.assertEquals(board.getHex(1, -1), targets.get(1))
        Assert.assertEquals(board.getHex(1, 0), targets.get(2))
    }

    /**
     * Testing scenario described here:
     * http://boardgamegeek.com/wiki/page/Hive_FAQ
     */
    @Test
    fun testTargetSquares_freedom_to_move_between_gates_blocked() {
        val board = Board(p1, p2)
        val beetle = p1.getFromSupply(BugType.BEETLE)
        board.addToken(beetle, 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1)
        board.addToken(p2.getFromSupply(BugType.SPIDER), 1, 0)
        board.addToken(p2.getFromSupply(BugType.SPIDER), 1, 0)

        val targets = checkNotNull(Rules.instance.getTargetHexes(beetle, board))
        Assert.assertEquals(4, targets.size.toLong())
        Assert.assertFalse(targets.contains(board.getHex(1, -1)))
    }

    /**
     * Testing scenario described here:
     * http://boardgamegeek.com/wiki/page/Hive_FAQ
     */
    @Test
    fun testTargetSquares_freedom_to_move_between_gates() {
        val board = Board(p1, p2)
        val bee = p1.getFromSupply(BugType.BEETLE)
        board.addToken(bee, 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1)
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 1, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0)

        val targets = checkNotNull(Rules.instance.getTargetHexes(bee, board))
        Assert.assertEquals(5, targets.size.toLong())
        Assert.assertTrue(targets.contains(board.getHex(1, -1)))
    }
}
