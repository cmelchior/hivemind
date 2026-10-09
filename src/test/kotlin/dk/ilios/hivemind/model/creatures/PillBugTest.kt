package dk.ilios.hivemind.model.creatures

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.Player.PlayerType
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class PillBugTest {
    lateinit var printer: HiveAsciiPrettyPrinter
    lateinit var p1: Player
    lateinit var p2: Player

    @Before
    fun setup() {
        printer = HiveAsciiPrettyPrinter()
        p1 = Player("White", PlayerType.WHITE)
        p1.fillBaseSupply()
        p1.usePillBugExpansion()
        p2 = Player("Black", PlayerType.BLACK)
        p2.fillBaseSupply()
        p2.usePillBugExpansion()
    }

    @Ignore
    @Test
    fun notImplemented() {
        TODO("Not Implemented")
    }
}
