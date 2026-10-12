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

class MosquitoTest {
    lateinit var printer: HiveAsciiPrettyPrinter
    lateinit var p1: Player
    lateinit var p2: Player

    @Before
    fun setup() {
        printer = HiveAsciiPrettyPrinter()
        p1 = Player("White", PlayerType.WHITE)
        p1.fillBaseSupply()
        p1.useMosquitoExpansion()
        p2 = Player("Black", PlayerType.BLACK)
        p2.fillBaseSupply()
        p2.useMosquitoExpansion()
    }


    /**
     * A mosquito has no move, so copying a mosquito does nothing.
     */
    @Test
    fun testTargetSquares_noMove() {
        val board = Board(p1, p2)
        val mos1 = p1.getFromSupply(BugType.MOSQUITO)
        val mos2 = p2.getFromSupply(BugType.MOSQUITO)
        mos1.mimic(mos2)

        board.addToken(mos1, 0, 0)
        board.addToken(mos2, 1, 0)

        val targets = Rules.instance.getTargetHexes(mos1, board)
        Assert.assertEquals(0, targets.size.toLong())
    }

    /**
     * Copying another bug grants movement.
     */
    @Test
    fun testTargetSquares_copyMovement() {
        val board = Board(p1, p2)
        val mos1 = p1.getFromSupply(BugType.MOSQUITO)
        val bee = p2.getFromSupply(BugType.QUEEN_BEE)
        mos1.mimic(bee)

        board.addToken(mos1, 0, 0)
        board.addToken(bee, 1, 0)

        val targets = Rules.instance.getTargetHexes(mos1, board)
        Assert.assertEquals(2, targets.size.toLong())
        Assert.assertTrue(targets.contains(board.getHex(1, -1)))
        Assert.assertTrue(targets.contains(board.getHex(0, 1)))
    }
}
