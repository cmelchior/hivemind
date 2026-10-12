package dk.ilios.hivemind.model

import dk.ilios.hivemind.ai.heuristics.TestSetups
import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.model.Player.PlayerType
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class BoardTest {
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
    fun testCreateZobristMap() {
        val board = Board(p1, p2)
        val start = System.currentTimeMillis()
        board.setStandardPositionMode(StandardPositionMode.ENABLED)
        println("Creating board in: " + (System.currentTimeMillis() - start))
    }

    @Test
    fun testClockwiseNeighbor() {
        val board = Board(p1, p2)
        val hex = board.getClockwiseHex(Hex(0, 0), Hex(1, -1), board)

        Assert.assertEquals(1, hex.q.toLong())
        Assert.assertEquals(0, hex.r.toLong())
    }

    @Test
    fun testCounterClockwiseNeighbor() {
        val board = Board(p1, p2)
        val hex = board.getCounterClockwiseHex(Hex(0, 0), Hex(1, -1), board)

        Assert.assertEquals(0, hex.q.toLong())
        Assert.assertEquals(-1, hex.r.toLong())
    }

    @Test
    fun testHexEquals() {
        val board = Board(p1, p2)
        val bee = p1.getFromSupply(BugType.QUEEN_BEE)
        board.addToken(bee, 1, -1)
        Assert.assertEquals(bee.hex, board.getHex(1, -1))
    }

    @Test
    fun testNeighborTokens() {
        var game = Game()
        val p1 = Player("White", PlayerType.WHITE)
        p1.fillBaseSupply()
        val p2 = Player("Black", PlayerType.BLACK)
        p2.fillBaseSupply()

        game.addPlayers(p1, p2)
        game.turnLimit = 10
        game = TestSetups.sureWinInOneTurn(game)
        Assert.assertEquals(6, game.board.getNeighborTokens(game.board.getHex(0, 3)).size.toLong())
    }

    @Test
    fun testStandardPosition_rotate2ndToken() {
        val game = Game()
        game.addPlayers(p1, p2)

        val board = game.board
        board.setStandardPositionMode(StandardPositionMode.ENABLED)

        val grasshopper = p1.getFromSupply(BugType.GRASSHOPPER)
        val bee = p2.getFromSupply(BugType.QUEEN_BEE)

        board.addToken(grasshopper, 1, 1)
        board.addToken(bee, 1, 2)

        val grasshopperCoords = board.getSPCoordinatesFor(checkNotNull(grasshopper.hex))
        val beeCoords = board.getSPCoordinatesFor(checkNotNull(bee.hex))

        Assert.assertArrayEquals(intArrayOf(0, 0), grasshopperCoords)
        Assert.assertArrayEquals(intArrayOf(1, 0), beeCoords)
    }

    @Test
    fun testStandardPosition_rotate2ndToken_fromOrigin() {
        val game = Game()
        game.addPlayers(p1, p2)

        val board = game.board
        board.setStandardPositionMode(StandardPositionMode.LIMITED)

        val grasshopper = p1.getFromSupply(BugType.GRASSHOPPER)
        val spider = p2.getFromSupply(BugType.SPIDER)

        board.addToken(grasshopper, 0, 0)
        board.addToken(spider, 1, -1)

        val grasshopperCoords = board.getSPCoordinatesFor(checkNotNull(grasshopper.hex))
        val spiderCoords = board.getSPCoordinatesFor(checkNotNull(spider.hex))

        Assert.assertArrayEquals(intArrayOf(0, 0), grasshopperCoords)
        Assert.assertArrayEquals(intArrayOf(1, 0), spiderCoords)
    }


    @Test
    fun testStandardPosition_flip3rdToken() {
        val game = Game()
        game.addPlayers(p1, p2)

        val board = game.board
        board.setStandardPositionMode(StandardPositionMode.ENABLED)

        val grasshopper = p1.getFromSupply(BugType.GRASSHOPPER)
        val blackBee = p2.getFromSupply(BugType.QUEEN_BEE)
        val spider = p1.getFromSupply(BugType.SPIDER)

        board.addToken(grasshopper, 1, 1)
        board.addToken(blackBee, 1, 2)
        board.addToken(spider, 0, 1)

        val spiderCoords = board.getSPCoordinatesFor(checkNotNull(spider.hex))
        Assert.assertArrayEquals(intArrayOf(0, -1), spiderCoords)
    }

    @Test
    fun testStandardPosition_flip4thToken() {
        val game = Game()
        game.addPlayers(p1, p2)

        val board = game.board
        board.setStandardPositionMode(StandardPositionMode.ENABLED)

        val grasshopper = p1.getFromSupply(BugType.GRASSHOPPER)
        val beetle = p2.getFromSupply(BugType.BEETLE)
        val whiteQueen = p1.getFromSupply(BugType.QUEEN_BEE)
        val blackQueen = p2.getFromSupply(BugType.QUEEN_BEE)

        board.addToken(grasshopper, 0, 0)
        p1.movedToken()
        board.addToken(beetle, 1, 0)
        p2.movedToken()
        board.addToken(whiteQueen, -1, 0)
        p1.movedToken()
        board.addToken(blackQueen, 1, 1)
        p2.movedToken()

        val blackQueenCoords = board.getSPCoordinatesFor(checkNotNull(blackQueen.hex))
        Assert.assertArrayEquals(intArrayOf(3, -2), blackQueenCoords)
    }


    @Test
    fun testStandardPosition_zOpening() {
        val game = Game()
        game.addPlayers(p1, p2)

        val board = game.board
        board.setStandardPositionMode(StandardPositionMode.ENABLED)

        val gh1 = p1.getFromSupply(BugType.GRASSHOPPER)
        val gh2 = p2.getFromSupply(BugType.GRASSHOPPER)
        val whiteQueen = p1.getFromSupply(BugType.QUEEN_BEE)
        val blackQueen = p2.getFromSupply(BugType.QUEEN_BEE)

        board.addToken(gh1, 0, 0)
        board.addToken(gh2, 1, 0)
        board.addToken(whiteQueen, -1, 1)
        board.addToken(blackQueen, 2, -1)

        val blackQueenCoords = board.getSPCoordinatesFor(checkNotNull(blackQueen.hex))
        Assert.assertArrayEquals(intArrayOf(3, -2), blackQueenCoords)
    }

    @Test
    fun testStandardPosition_reverse() {
        val game = Game()
        game.addPlayers(p1, p2)

        val board = game.board
        board.setStandardPositionMode(StandardPositionMode.ENABLED)

        val gh1 = p1.getFromSupply(BugType.GRASSHOPPER)
        val gh2 = p2.getFromSupply(BugType.GRASSHOPPER)
        val s1 = p1.getFromSupply(BugType.SPIDER)
        val s2 = p2.getFromSupply(BugType.SPIDER)
        val whiteQueen = p1.getFromSupply(BugType.QUEEN_BEE)
        val blackQueen = p2.getFromSupply(BugType.QUEEN_BEE)

        board.addToken(gh1, 0, 0)
        board.addToken(gh2, 1, 0)
        board.addToken(s1, -1, 0)
        board.addToken(s2, 2, 0)
        board.addToken(whiteQueen, 0, -1)
        board.addToken(blackQueen, 1, 1)

        val blackQueenCoords = board.getSPCoordinatesFor(checkNotNull(blackQueen.hex))
        val originalHex = board.getHexForStandardPosition(blackQueenCoords[0], blackQueenCoords[1])
        Assert.assertEquals(blackQueen, originalHex.topToken)
    }

    @Test
    fun testZobristKey_empty() {
        val board = Board(p1, p2)
        board.setStandardPositionMode(StandardPositionMode.ENABLED)
        Assert.assertEquals(0, board.zobristKey)
    }

    //
    //    @Test
    //    public void testZobristKey_playerState() {
    //        Board board = new Board();
    //        Token whiteBee = p1.getFromSupply(BugType.QUEEN_BEE);
    //        Token blackBee = p2.getFromSupply(BugType.QUEEN_BEE);
    //
    //        board.addToken(whiteBee, 0, 0);
    //        board.addToken(blackBee, 0, 1);
    //        long firstKey = board.getZobristKey();
    //
    //        board.removeToken(0, 1);
    //        board.removeToken(0, 0);
    //        board.addToken(blackBee, 0, 1);
    //        board.addToken(whiteBee, 0, 0);
    //
    //        // Last player move is encoded into the key. Similar
    //        // board layouts are not the same if next player differs.
    //        assertNotEquals(firstKey, board.getZobristKey());
    //    }
    //
    @Test
    fun testZobristKey_boardState() {
        val board = Board(p1, p2)
        board.setStandardPositionMode(StandardPositionMode.ENABLED)
        val whiteBee = p1.getFromSupply(BugType.QUEEN_BEE)
        val whiteSoldier = p1.getFromSupply(BugType.SOLDIER_ANT)
        val blackBee = p2.getFromSupply(BugType.QUEEN_BEE)
        val blackSoldier = p2.getFromSupply(BugType.SOLDIER_ANT)

        board.addToken(whiteBee, 0, 0)
        board.addToken(blackBee, 0, 1)
        board.addToken(whiteSoldier, 1, 0)
        board.addToken(blackSoldier, 1, 1)

        val firstKey = board.zobristKey

        board.removeToken(1, 1)
        board.removeToken(1, 0)
        board.removeToken(0, 1)
        board.removeToken(0, 0)

        board.addToken(whiteBee, 0, 0)
        board.addToken(blackSoldier, 1, 1)
        board.addToken(whiteSoldier, 1, 0)
        board.addToken(blackBee, 0, 1)

        // If board state is the same and next player also. It doesn't matter how that state was reached.
        Assert.assertEquals(firstKey, board.zobristKey)
    }

    @Test
    fun testZobristKey_sameForStandardPositionForMultiplePositions() {
        val board = Board(p1, p2)
        board.setStandardPositionMode(StandardPositionMode.ENABLED)
        val whiteBee = p1.getFromSupply(BugType.QUEEN_BEE)
        val whiteSoldier = p1.getFromSupply(BugType.SOLDIER_ANT)
        val blackBee = p2.getFromSupply(BugType.QUEEN_BEE)
        val blackSoldier = p2.getFromSupply(BugType.SOLDIER_ANT)

        board.addToken(whiteBee, 0, 0)
        board.addToken(blackBee, 1, 0)
        board.addToken(whiteSoldier, 2, 0)
        board.addToken(blackSoldier, 3, -1)

        val firstKey = board.zobristKey

        board.removeToken(0, 0)
        board.removeToken(1, 0)
        board.removeToken(2, 0)
        board.removeToken(3, -1)

        board.addToken(whiteBee, 0, 0)
        board.addToken(blackBee, -1, 0)
        board.addToken(whiteSoldier, -2, 0)
        board.addToken(blackSoldier, -3, 1)

        // If the board maintain Standard Position the Zobrist keys should be equal.
        Assert.assertEquals(firstKey, board.zobristKey)
    }

    @Test
    fun testGetHexForStandardPosition() {
        val game = Game()
        game.addPlayers(p1, p2)

        val board = game.board
        board.setStandardPositionMode(StandardPositionMode.ENABLED)

        val grasshopper = p1.getFromSupply(BugType.GRASSHOPPER)
        val bee = p2.getFromSupply(BugType.QUEEN_BEE)

        board.addToken(grasshopper, 0, 0)
        board.addToken(bee, 1, -1)

        val grasshopperCoords = board.getSPCoordinatesFor(checkNotNull(grasshopper.hex))
        val beeCoords = board.getSPCoordinatesFor(checkNotNull(bee.hex))

        Assert.assertArrayEquals(intArrayOf(0, 0), grasshopperCoords)
        Assert.assertArrayEquals(intArrayOf(1, 0), beeCoords)
    }

    @Test
    fun testOpeningStandardPosition() {
        val game = Game()
        p1.useAllExpansions()
        p2.useAllExpansions()
        game.addPlayers(p1, p2)

        val board = game.board
        board.setStandardPositionMode(StandardPositionMode.ENABLED)

        val ladybug = p1.getFromSupply(BugType.LADY_BUG)
        val mosquito = p2.getFromSupply(BugType.MOSQUITO)
        val spider = p1.getFromSupply(BugType.SPIDER)
        val grasshopper = p2.getFromSupply(BugType.GRASSHOPPER)

        board.addToken(ladybug, 0, 0)
        board.addToken(mosquito, -1, 1)
        board.addToken(spider, 0, -1)
        board.addToken(grasshopper, -2, 2)

        Assert.assertArrayEquals(intArrayOf(1, 0), board.getSPCoordinatesFor(checkNotNull(mosquito.hex)))
        Assert.assertArrayEquals(intArrayOf(0, -1), board.getSPCoordinatesFor(checkNotNull(spider.hex)))
    }
}
