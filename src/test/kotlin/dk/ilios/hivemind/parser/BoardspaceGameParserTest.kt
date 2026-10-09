package dk.ilios.hivemind.parser

import dk.ilios.hivemind.model.BugType
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BoardspaceGameParserTest {
    @Rule
    @JvmField
    val temporaryFolder = TemporaryFolder()

    @Test
    fun parsesPlayerMetadataAndReplaysRelativeMoves() {
        val file = temporaryFolder.newFile("opening.sgf")
        file.writeText(
            """
            SU[hive]
            P0[id "White"]
            P1[id "Black"]
            P0[time 0:00:05 ]
            P1[time 0:00:07 ]
            P0[ranking 1700 ]
            P1[ranking 1600 ]
            P0[1 pdropb wq A 1 .]; P0[2 done]
            P1[3 pdropb bq A 1 /wQ]; P1[4 done]
            """.trimIndent()
        )

        val parser = BoardspaceGameParser(file)
        val game = parser.parse()
        val white = game.getWhitePlayer()
        val black = game.getBlackPlayer()

        assertEquals("hive", parser.getGameType())
        assertEquals("White", white.name)
        assertEquals("Black", black.name)
        assertEquals(1700, white.ranking)
        assertEquals(1600, black.ranking)
        assertEquals(5000L, white.playTime)
        assertEquals(7000L, black.playTime)
        assertSame(white.queen, game.getBoard().getHex(0, 0).topToken)
        assertSame(black.queen, game.getBoard().getHex(0, 1).topToken)
        assertEquals(BugType.QUEEN_BEE, game.getBoard().getHex(0, 1).topToken!!.type)
        assertEquals(1, white.turns)
        assertEquals(1, black.turns)

        game.setReplayMode(true)
        assertTrue(game.getBoard().filledHexes.isEmpty())
        game.forward()
        game.forward()
        assertSame(black.queen, game.getBoard().getHex(0, 1).topToken)
    }
}
