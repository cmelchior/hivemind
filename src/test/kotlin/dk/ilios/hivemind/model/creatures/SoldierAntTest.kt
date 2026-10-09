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

class SoldierAntTest {
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
        val ant = p1.getFromSupply(BugType.SOLDIER_ANT)
        val bee = p2.getFromSupply(BugType.QUEEN_BEE)
        board.addToken(ant, 0, 0)
        board.addToken(bee, 1, 0)

        val targets = checkNotNull(Rules.instance.getTargetHexes(ant, board))
        Assert.assertEquals(5, targets.size.toLong())
        Assert.assertEquals(board.getHex(1, -1), targets.get(0))
        Assert.assertEquals(board.getHex(0, 1), targets.get(4))
    }

    /**
     * White Ant cannot enter the gate and go to Hex A.
     *
     * | = = = = = = = = = = = = |
     * |    _ _                  |
     * |  /# # #\                |
     * | /# ANT #\ _ _           |
     * | \# -W- #/+ + +\         |
     * |  \#_#_#/+ QBE +\ _ _    |
     * |  /+ + +\+ -B- +/+ + +\  |
     * | /+ ANT +\+_+_+/+ SPI +\ |
     * | \+ -B- +/     \+ -B- +/ |
     * |  \+_+_+/   A   \+_+_+/  |
     * |  /+ + +\       /+ + +\  |
     * | /+ BTL +\ _ _ /+ HOP +\ |
     * | \+ -B- +/     \+ -B- +/ |
     * |  \+_+_+/       \+_+_+/  |
     * |                         |
     * | = = = = = = = = = = = = |
     */
    @Test
    fun testTargetSquares_cannotEnterGate() {
        val board = Board(p1, p2)
        val ant = p1.getFromSupply(BugType.SOLDIER_ANT)
        board.addToken(ant, 0, 0)
        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 1, 0)
        board.addToken(p2.getFromSupply(BugType.SPIDER), 2, 0)
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 2, 1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1)
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, 2)

        val targets = checkNotNull(Rules.instance.getTargetHexes(ant, board))
        Assert.assertEquals(11, targets.size.toLong())
        Assert.assertFalse(targets.contains(board.getHex(1, 1)))
    }

    @Test
    fun freeToMove_surrounded() {
        val board = Board(p1, p2)
        val ant = p1.getFromSupply(BugType.SOLDIER_ANT)
        board.addToken(ant, 0, 0)

        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 0, -1)
        board.addToken(p2.getFromSupply(BugType.SPIDER), 1, 0)
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), -1, 1)

        Assert.assertFalse(Rules.instance.isFreeToMove(ant, board))
    }

    /**
     * A1 is almost surrounded, but still free to move.
     *
     * | = = = = = = = = = = = = |
     * |                  _ _    |
     * |                /# # #\  |
     * |           _ _ /# S2  #\ |
     * |         /+ + +\# -W- #/ |
     * |    _ _ /+ A1  +\#_#_#/  |
     * |  /+ + +\+ -B- +/# # #\  |
     * | /+ B2  +\+_+_+/#  Q  #\ |
     * | \+ -B- +/+ + +\# -W- #/ |
     * |  \+_+_+/+  Q  +\#_#_#/  |
     * |        \+ -B- +/        |
     * |         \+_+_+/         |
     * |         /+ + +\         |
     * |        /+ A2  +\        |
     * |        \+ -B- +/        |
     * |         \+_+_+/         |
     * |                         |
     * | = = = = = = = = = = = = |
     */
    @Test
    fun freeToMove_almostSurrounded() {
        val board = Board(p1, p2)

        // Black setup
        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 0, 3)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 4)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 2)
        board.addToken(p2.getFromSupply(BugType.BEETLE), -1, 3)

        // White setup
        board.addToken(p1.getFromSupply(BugType.SPIDER), 1, 1)
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 1, 2)

        Assert.assertTrue(Rules.instance.isFreeToMove(checkNotNull(board.getHex(0, 2).topToken), board))
    }

    /**
     *
     * | = = = = = = = = = = = = = = = = = = = |
     * |                         _ _           |
     * |                       /# # #\         |
     * |                  _ _ /# ANT #\        |
     * |                /# # #\# -W- #/        |
     * |               /# ANT #\#_#_#/         |
     * |               \# -W- #/               |
     * |                \#_#_#/                |
     * |                /# # #\                |
     * |           _ _ /# SPI #\               |
     * |         /     \# -W- #/               |
     * |    _ _ /  WIN  \#_#_#/         _ _    |
     * |  /+ + +\       /# # #\       /# # #\  |
     * | /+ B1  +\ _ _ /# QBE #\ _ _ /# ANT #\ |
     * | \+ -B- +/+ + +\# -W- #/# # #\# -W- #/ |
     * |  \+_+_+/+ QBE +\#_#_#/# SPI #\#_#_#/  |
     * |  /+ + +\+ -B- +/+ + +\# -W- #/+ + +\  |
     * | /+ A1  +\+_+_+/+ ANT +\#_#_#/+ HOP +\ |
     * | \+ -B- +/+ + +\+ -B- +/     \+ -B- +/ |
     * |  \+_+_+/+ A2  +\+_+_+/       \+_+_+/  |
     * |        \+ -B- +/                      |
     * |         \+_+_+/                       |
     * |                                       |
     * | = = = = = = = = = = = = = = = = = = = |
     *
     */
    @Test
    fun canMoveToEndGame() {
        val board = Board(p1, p2)

        // Black setup
        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 0, 3)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 4)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), -1, 4)
        board.addToken(p2.getFromSupply(BugType.BEETLE), -1, 3)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 3)
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 3, 2)

        // White setup
        board.addToken(p1.getFromSupply(BugType.SOLDIER_ANT), 1, 0)
        board.addToken(p1.getFromSupply(BugType.SPIDER), 1, 1)
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 1, 2)
        board.addToken(p1.getFromSupply(BugType.SOLDIER_ANT), 2, -1)
        board.addToken(p1.getFromSupply(BugType.SPIDER), 2, 2)
        board.addToken(p1.getFromSupply(BugType.SOLDIER_ANT), 3, 1)

        val ant = checkNotNull(board.getHex(2, -1).topToken)
        val targets = checkNotNull(Rules.instance.getBugSpecificRules(ant).getTargetHexes(ant, board))
        Assert.assertTrue(targets.contains(board.getHex(0, 2)))
    }
}
