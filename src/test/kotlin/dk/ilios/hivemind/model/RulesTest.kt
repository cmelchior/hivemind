package dk.ilios.hivemind.model

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter
import dk.ilios.hivemind.model.Player.PlayerType
import dk.ilios.hivemind.model.rules.Rules
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RulesTest {
    lateinit var printer: HiveAsciiPrettyPrinter
    lateinit var p1: Player
    lateinit var p2: Player

    @Before
    fun setup() {
        printer = HiveAsciiPrettyPrinter()
        p1 = Player("White", PlayerType.WHITE)
        p2 = Player("Black", PlayerType.BLACK)

        p1.fillBaseSupply()
        p1.useAllExpansions()
        p2.fillBaseSupply()
        p2.useAllExpansions()
    }

    @Test
    fun canSlideTo_noSpace() {
        val board = Board(p1, p2)
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1)

        Assert.assertFalse(Rules.instance.canSlideTo(board.getHex(0, 0), board.getHex(1, -1), board))
    }

    @Test
    fun canSlideTo_success() {
        val board = Board(p1, p2)
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1)

        Assert.assertTrue(Rules.instance.canSlideTo(board.getHex(0, 0), board.getHex(1, -1), board))
    }

    @Test
    fun canSlideTo_blocked() {
        val board = Board(p1, p2)
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0)

        Assert.assertFalse(Rules.instance.canSlideTo(board.getHex(0, 0), board.getHex(1, -1), board))
    }

    @Test
    fun canCrawlDown_success() {
        val board = Board(p1, p2)
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1)

        Assert.assertTrue(Rules.instance.canCrawlDown(board.getHex(0, -1), board.getHex(0, 0), board))
    }


    /**
     * Queen Bee cannot move to Hex A, as the One Hive rule is broken during the move
     *
     * | = = = = = = = = = = = = |
     * |           _ _           |
     * |         /+ + +\         |
     * |    _ _ /+ ANT +\ _ _    |
     * |  /# # #\+ -B- +/# # #\  |
     * | /# QBE #\+_+_+/# BTL #\ |
     * | \# -W- #/     \# -W- #/ |
     * |  \#_#_#/       \#_#_#/  |
     * |  /     \       /+ + +\  |
     * | /   A   \ _ _ /+ ANT +\ |
     * | \       /+ + +\+ -B- +/ |
     * |  \ _ _ /+ HOP +\+_+_+/  |
     * |        \+ -B- +/        |
     * |         \+_+_+/         |
     * |                         |
     * | = = = = = = = = = = = = |
     */
    @Test
    fun canSlideTo_oneHiveBrokenDuringMove() {
        val board = Board(p1, p2)
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1)
        board.addToken(p2.getFromSupply(BugType.BEETLE), 2, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 2, 0)
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 1, 1)

        Assert.assertFalse(Rules.instance.canSlideTo(board.getHex(0, 0), board.getHex(0, 1), board))
    }

    /**
     * Trying to slide a empty hex to a occupied hex
     */
    @Test
    fun canSlideTo_illegalArguments() {
        val board = Board(p1, p2)
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0)

        Assert.assertFalse(Rules.instance.canSlideTo(board.getHex(0, 1), board.getHex(0, 0), board))
    }

    @Test
    fun canSlideOnTopOfHive() {
        val board = Board(p1, p2)
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0)
        board.addToken(p1.getFromSupply(BugType.LADY_BUG), 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0)

        Assert.assertTrue(Rules.instance.canSlideTo(board.getHex(0, 0), board.getHex(1, 0), board))
    }

    /**
     * Cannot slide to a filled hex (can crawl up though)
     */
    @Test
    fun canSlideOnTopOfHive_blockedSinglePiece() {
        val board = Board(p1, p2)
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0)
        board.addToken(p1.getFromSupply(BugType.LADY_BUG), 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0)
        board.addToken(p2.getFromSupply(BugType.BEETLE), 1, 0)

        Assert.assertFalse(Rules.instance.canSlideTo(board.getHex(0, 0), board.getHex(1, 0), board))
    }

    @Test
    fun canSlideOnTopOfHive_blockedGuards() {
        val board = Board(p1, p2)

        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0)
        board.addToken(p1.getFromSupply(BugType.LADY_BUG), 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1)
        board.addToken(p1.getFromSupply(BugType.SOLDIER_ANT), 1, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1)
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, 1)

        Assert.assertFalse(Rules.instance.canCrawlDown(board.getHex(0, 0), board.getHex(1, 0), board))
    }

    @Test
    fun canCrawlDown_blocked() {
        val board = Board(p1, p2)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1)
        board.addToken(p1.getFromSupply(BugType.SOLDIER_ANT), 1, 0)
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 1, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1)
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, 1)

        Assert.assertFalse(Rules.instance.canCrawlDown(board.getHex(1, 0), board.getHex(0, 0), board))
    }

    /**
     * Crawl from ground to top of hive
     */
    @Test
    fun canCrawlTo_success() {
        val board = Board(p1, p2)
        board.addToken(p1.getFromSupply(BugType.BEETLE), 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1)

        Assert.assertTrue(Rules.instance.canCrawlUp(board.getHex(0, 0), board.getHex(1, -1), board))
    }

    /**
     * Crawl from ground to top of hive, but blocked by guarding towers.
     */
    @Test
    fun canCrawlTo_blockedHigh() {
        val board = Board(p1, p2)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1)
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, -1)
        board.addToken(p1.getFromSupply(BugType.BEETLE), 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0)
        board.addToken(p2.getFromSupply(BugType.BEETLE), 1, 0)

        Assert.assertFalse(Rules.instance.canSlideTo(board.getHex(0, 0), board.getHex(1, -1), board))
    }

    /**
     * Crawl between two guards (effectively sliding).
     */
    @Test
    fun canCrawlTo_blockedLow() {
        val board = Board(p1, p2)
        board.addToken(p1.getFromSupply(BugType.BEETLE), 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0)

        Assert.assertFalse(Rules.instance.canCrawlUp(board.getHex(0, 0), board.getHex(1, -1), board))
    }

    @Test
    fun getMimicList_onGround() {
        val board = Board(p1, p2)
        val mos1 = p1.getFromSupply(BugType.MOSQUITO)
        val bee = p2.getFromSupply(BugType.QUEEN_BEE)
        mos1.mimic(bee)

        board.addToken(mos1, 0, 0)
        board.addToken(bee, 1, 0)

        val result: MutableList<Token> = Rules.instance.getMimicList(mos1, board)
        Assert.assertEquals(1, result.size.toLong())
        Assert.assertEquals(bee, result.get(0))
    }

    @Test
    fun getMimicList_onHive() {
        val board = Board(p1, p2)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1)
        board.addToken(p1.getFromSupply(BugType.BEETLE), 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0)

        val mos1 = p1.getFromSupply(BugType.MOSQUITO)
        board.addToken(mos1, 0, 0)

        val result: MutableList<Token> = Rules.instance.getMimicList(mos1, board)
        Assert.assertEquals(0, result.size.toLong())
    }

    @Test
    fun isQueenSurronded_false() {
        val board = Board(p1, p2)
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1)
        board.addToken(p2.getFromSupply(BugType.BEETLE), 1, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0)
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 0, 1)
        board.addToken(p2.getFromSupply(BugType.SPIDER), -1, 1)

        Assert.assertFalse(Rules.instance.isQueenSurrounded(p1, board))
    }

    /**
     * | = = = = = = = = = = = = |
     * |           _ _           |
     * |         /+ + +\         |
     * |    _ _ /+ A2  +\ _ _    |
     * |  /+ + +\+ -B- +/+ + +\  |
     * | /+ A1  +\+_+_+/+ B1  +\ |
     * | \+ -B- +/# # #\+ -B- +/ |
     * |  \+_+_+/#  Q  #\+_+_+/  |
     * |  /+ + +\# -W- #/+ + +\  |
     * | /+ S1  +\#_#_#/+ A3  +\ |
     * | \+ -B- +/+ + +\+ -B- +/ |
     * |  \+_+_+/+ G2  +\+_+_+/  |
     * |        \+ -B- +/        |
     * |         \+_+_+/         |
     * |                         |
     * | = = = = = = = = = = = = |
     */
    @Test
    fun isQueenSurronded_true() {
        val board = Board(p1, p2)
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1)
        board.addToken(p2.getFromSupply(BugType.BEETLE), 1, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0)
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 0, 1)
        board.addToken(p2.getFromSupply(BugType.SPIDER), -1, 1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), -1, 0)

        Assert.assertTrue(Rules.instance.isQueenSurrounded(p1, board))
    }


    @Test
    fun isQueenSurronded_falseAfterMove() {
        val board = Board(p1, p2)
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1)
        board.addToken(p2.getFromSupply(BugType.BEETLE), 1, -1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0)
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 0, 1)
        board.addToken(p2.getFromSupply(BugType.SPIDER), -1, 1)
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), -1, 0)

        board.moveToken(-1, 0, -1, -1)
        Assert.assertFalse(Rules.instance.isQueenSurrounded(p1, board))
    }
}
