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

class QueenBeeTest {
    lateinit var printer: HiveAsciiPrettyPrinter
    lateinit var p1: Player
    lateinit var p2: Player

    @Before
    fun setup() {
        printer = HiveAsciiPrettyPrinter()
        p1 = Player("John", PlayerType.WHITE)
        p1.fillBaseSupply()
        p2 = Player("Susan", PlayerType.BLACK)
        p2.fillBaseSupply()
    }


    @Test
    fun testTargetSquares_startingPosition() {
        val board = Board(p1, p2)
        val bee = p1.getFromSupply(BugType.QUEEN_BEE)
        val ant = p2.getFromSupply(BugType.SOLDIER_ANT)
        board.addToken(bee, 0, 0)
        board.addToken(ant, 1, -1)

        val targets = Rules.instance.getTargetHexes(bee, board)
        Assert.assertEquals(2, targets.size.toLong())
        Assert.assertEquals(board.getHex(0, -1), targets[0])
        Assert.assertEquals(board.getHex(1, 0), targets[1])
    }
}
