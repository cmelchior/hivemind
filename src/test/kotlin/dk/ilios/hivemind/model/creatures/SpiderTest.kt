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

class SpiderTest {
    lateinit var printer: HiveAsciiPrettyPrinter
    lateinit var p1: Player
    lateinit var p2: Player

    @Before
    fun setup() {
        printer = HiveAsciiPrettyPrinter()
        p1 = Player("White", PlayerType.WHITE)
        p2 = Player("Black", PlayerType.BLACK)
        p1.fillBaseSupply()
        p2.fillBaseSupply()
    }


    @Test
    fun testTargetSquares_aroundHive() {
        val board = Board(p1, p2)
        val ant = p1.getFromSupply(BugType.SPIDER)
        val bee = p2.getFromSupply(BugType.QUEEN_BEE)
        board.addToken(ant, 0, 0)
        board.addToken(bee, 1, 0)

        val targets = checkNotNull(Rules.instance.getTargetHexes(ant, board))
        Assert.assertEquals(1, targets.size.toLong())
        Assert.assertEquals(board.getHex(2, 0), targets.get(0))
    }

    /**
     * Spider cannot enter the gate and go to Hex A, so doesn't count it when finding target hexes.
     *
     * | = = = = = = = = = = = = |
     * |           _ _           |
     * |         /+ + +\         |
     * |    _ _ /+ QBE +\ _ _    |
     * |  /+ + +\+ -B- +/+ + +\  |
     * | /+ ANT +\+_+_+/+ SPI +\ |
     * | \+ -B- +/     \+ -B- +/ |
     * |  \+_+_+/       \+_+_+/  |
     * |  /+ + +\       /+ + +\  |
     * | /+ BTL +\     /+ HOP +\ |
     * | \+ -B- +/     \+ -B- +/ |
     * |  \+_+_+/       \+_+_+/  |
     * |  /# # #\                |
     * | /# SPI #\               |
     * | \# -W- #/               |
     * |  \#_#_#/                |
     * |                         |
     * | = = = = = = = = = = = = |
     */
    @Test
    fun testTargetSquares_cannotEnterGate() {
        val board = Board(p1, p2)
        val ant = p1.getFromSupply(BugType.SPIDER)
        board.addToken(ant, 0, 3)
        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 1, 0)
        board.addToken(p2.getFromSupply(BugType.SPIDER), 2, 0)
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 2, 1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1)
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, 2)

        val targets = checkNotNull(Rules.instance.getTargetHexes(ant, board))
        Assert.assertEquals(2, targets.size.toLong())
        Assert.assertTrue(targets.contains(board.getHex(-1, 1)))
        Assert.assertTrue(targets.contains(board.getHex(3, 1)))
    }
}
