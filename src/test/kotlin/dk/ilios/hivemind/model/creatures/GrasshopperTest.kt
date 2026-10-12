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

class GrasshopperTest {
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
    fun testTargetSquares_overToken() {
        val board = Board(p1, p2)
        val grasshopper = p1.getFromSupply(BugType.GRASSHOPPER)
        val bee = p2.getFromSupply(BugType.QUEEN_BEE)
        board.addToken(grasshopper, 0, 0)
        board.addToken(bee, 1, 0)

        val targets = Rules.instance.getTargetHexes(grasshopper, board)
        Assert.assertEquals(1, targets.size.toLong())
        Assert.assertEquals(board.getHex(2, 0), targets[0])
    }

    /**
     * Grasshopper(W) cannot jump over empty spaces.
     *
     * | = = = = = = = = = = = = |
     * |           _ _           |
     * |         /+ + +\         |
     * |    _ _ /+ QBE +\ _ _    |
     * |  /+ + +\+ -B- +/+ + +\  |
     * | /+ ANT +\+_+_+/+ SPI +\ |
     * | \+ -B- +/     \+ -B- +/ |
     * |  \+_+_+/       \+_+_+/  |
     * |  /# # #\       /+ + +\  |
     * | /# HOP #\     /+ HOP +\ |
     * | \# -W- #/     \+ -B- +/ |
     * |  \#_#_#/       \+_+_+/  |
     * |                         |
     * | = = = = = = = = = = = = |
     */
    @Test
    fun testTargetSquares_cannotJumpOverEmptySpaces() {
        val board = Board(p1, p2)
        val ant = p1.getFromSupply(BugType.GRASSHOPPER)
        board.addToken(ant, 0, 2)
        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 1, 0)
        board.addToken(p2.getFromSupply(BugType.SPIDER), 2, 0)
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 2, 1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1)

        val targets = Rules.instance.getTargetHexes(ant, board)
        Assert.assertEquals(1, targets.size.toLong())
        Assert.assertTrue(targets.contains(board.getHex(0, 0)))
    }
}
