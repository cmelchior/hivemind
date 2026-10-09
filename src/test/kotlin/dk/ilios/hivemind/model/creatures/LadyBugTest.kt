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

class LadyBugTest {
    lateinit var printer: HiveAsciiPrettyPrinter
    lateinit var p1: Player
    lateinit var p2: Player

    @Before
    fun setup() {
        printer = HiveAsciiPrettyPrinter()
        p1 = Player("White", PlayerType.WHITE)
        p1.fillBaseSupply()
        p1.useLadyBugExpansion()
        p2 = Player("Black", PlayerType.BLACK)
        p2.fillBaseSupply()
        p2.useLadyBugExpansion()
    }


    @Test
    fun testTargetSquares_noMoves() {
        val board = Board(p1, p2)
        val ant = p1.getFromSupply(BugType.LADY_BUG)
        val bee = p2.getFromSupply(BugType.QUEEN_BEE)
        board.addToken(ant, 0, 0)
        board.addToken(bee, 1, 0)

        val targets = checkNotNull(Rules.instance.getTargetHexes(ant, board))
        Assert.assertEquals(0, targets.size.toLong())
    }

    /**
     * LadyBug has a complex movement pattern.
     * Following assumptions:
     * - A LadyBug can either "Slide" or "Crawl" on top of a Hive
     * - A LadyBug cannot end her move where she started.
     *
     * | = = = = = = = = = = = = |
     * |           _ _           |
     * |         /# # #\         |
     * |    _ _ /# LDY #\        |
     * |  /+ + +\# -W- #/        |
     * | /+ ANT +\#_#_#/         |
     * | \+ -B- +/+ + +\         |
     * |  \+_+_+/+ SPI +\ _ _    |
     * |  /+ + +\+ -B- +/+ + +\  |
     * | /+ BTL +\+_+_+/+ HOP +\ |
     * | \+ -B- +/     \+ -B- +/ |
     * |  \+_+_+/       \+_+_+/  |
     * |  /+ + +\                |
     * | /+ QBE +\               |
     * | \+ -B- +/               |
     * |  \+_+_+/                |
     * |                         |
     * | = = = = = = = = = = = = |
     */
    @Test
    fun testTargetSquares_largeHive() {
        val board = Board(p1, p2)
        val ant = p1.getFromSupply(BugType.LADY_BUG)
        board.addToken(ant, 1, 0)
        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 0, 3)
        board.addToken(p2.getFromSupply(BugType.SPIDER), 1, 1)
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 2, 1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1)
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, 2)

        val targets = checkNotNull(Rules.instance.getTargetHexes(ant, board))
        Assert.assertEquals(9, targets.size.toLong())
        Assert.assertFalse(targets.contains(board.getHex(0, 4)))
        Assert.assertFalse(targets.contains(board.getHex(1, 3)))
    }

    /**
     * A LadyBug can either crawl or slide when on top of the hive.
     * When faced with a 2 stack, she just climbs up instead of sliding.
     *
     * | = = = = = = = = = = = = |
     * |           _ _           |
     * |         /# # #\         |
     * |    _ _ /# LDY #\        |
     * |  /+ + +\# -W- #/        |
     * | /+ ANT +\#_#_#/         |
     * | \+ -B- +/+ + +\         |
     * |  \+_+_+/+ SPI +\ _ _    |
     * |  /+ + +\+ -B- +/+ + +\  |
     * | /+ 2xB +\+_+_+/+ HOP +\ |
     * | \+ -B- +/     \+ -B- +/ |
     * |  \+_+_+/       \+_+_+/  |
     * |  /+ + +\                |
     * | /+ QBE +\               |
     * | \+ -B- +/               |
     * |  \+_+_+/                |
     * |                         |
     * | = = = = = = = = = = = = |
     */
    @Test
    fun testTargetSquares_movingOnTop() {
        val board = Board(p1, p2)
        val ant = p1.getFromSupply(BugType.LADY_BUG)
        board.addToken(ant, 1, 0)
        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 0, 3)
        board.addToken(p2.getFromSupply(BugType.SPIDER), 1, 1)
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 2, 1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1)
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, 2)
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, 2)

        val targets = checkNotNull(Rules.instance.getTargetHexes(ant, board))
        Assert.assertEquals(9, targets.size.toLong())
        Assert.assertFalse(targets.contains(board.getHex(0, 4)))
        Assert.assertFalse(targets.contains(board.getHex(1, 3)))
    }
}
