package dk.ilios.hivemind.model;

import dk.ilios.hivemind.ai.heuristics.TestSetups;
import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.game.Game;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BoardTest {

    HiveAsciiPrettyPrinter printer;
    Player p1;
    Player p2;

    @Before
    public void setup() {
        printer = new HiveAsciiPrettyPrinter();
        p1 = new Player("John", Player.PlayerType.WHITE);
        p1.fillBaseSupply();
        p2 = new Player("Susan", Player.PlayerType.BLACK);
        p2.fillBaseSupply();
    }


    @Test
    public void testClockwiseNeighbor() {
        Board board = new Board();
        Hex hex = board.getClockwiseHex(new Hex(0,0), new Hex(1, -1), board);

        assertEquals(1, hex.getQ());
        assertEquals(0, hex.getR());
    }

    @Test
    public void testCounterClockwiseNeighbor() {
        Board board = new Board();
        Hex hex = board.getCounterClockwiseHex(new Hex(0,0), new Hex(1, -1), board);

        assertEquals(0, hex.getQ());
        assertEquals(-1, hex.getR());
    }

    @Test
    public void testHexEquals() {
        Board board = new Board();
        Token bee = p1.getFromSupply(BugType.QUEEN_BEE);
        board.addToken(bee, 1, -1);
        assertEquals(bee.getHex(), board.getHex(1, -1));
    }

    @Test
    public void testNeighborTokens() {
        Game game = new Game();
        Player p1 = new Player("White", Player.PlayerType.WHITE); p1.fillBaseSupply();
        Player p2 = new Player("Black", Player.PlayerType.BLACK); p2.fillBaseSupply();

        game.addPlayers(p1, p2);
        game.setTurnLimit(10);
        game = TestSetups.sureWinInOneTurn(game);
        assertEquals(6, game.getBoard().getNeighborTokens(game.getBoard().getHex(0,3)).size());
    }

    @Test
    public void testStandardPosition_rotate2ndToken() {
        Game game = new Game();
        game.addPlayers(p1, p2);

        Board board = game.getBoard();
        board.setStandardPositionMode(true);

        Token grasshopper = p1.getFromSupply(BugType.GRASSHOPPER);
        Token bee = p2.getFromSupply(BugType.QUEEN_BEE);

        board.addToken(grasshopper, 1, 1);
        board.addToken(bee, 1, 2);

        int[] grasshopperCoords = board.getSPCoordinatesFor(grasshopper.getHex());
        int[] beeCoords = board.getSPCoordinatesFor(bee.getHex());

        assertArrayEquals(new int[] {0, 0}, grasshopperCoords);
        assertArrayEquals(new int[] {1, 0}, beeCoords);
    }


    @Test
    public void testStandardPosition_flip3rdToken() {
        Game game = new Game();
        game.addPlayers(p1, p2);

        Board board = game.getBoard();
        board.setStandardPositionMode(true);

        Token grasshopper = p1.getFromSupply(BugType.GRASSHOPPER);
        Token blackBee = p2.getFromSupply(BugType.QUEEN_BEE);
        Token spider = p1.getFromSupply(BugType.SPIDER);

        board.addToken(grasshopper, 1, 1);
        board.addToken(blackBee, 1, 2);
        board.addToken(spider, 0, 1);
        printer.print(board);

        int[] spiderCoords = board.getSPCoordinatesFor(spider.getHex());
        assertArrayEquals(new int[] {0, -1}, spiderCoords);

    }



//    @Test
//    public void testZobristKey_empty() {
//        long time = System.currentTimeMillis();
//        Board board = new Board();
//        System.out.println("" + (System.currentTimeMillis() - time));
//
//        assertEquals(0, board.getZobristKey());
//    }
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
//    @Test
//    public void testZobristKey_boardState() {
//        Board board = new Board();
//        Token whiteBee = p1.getFromSupply(BugType.QUEEN_BEE);
//        Token whiteSoldier = p1.getFromSupply(BugType.SOLDIER_ANT);
//        Token blackBee = p2.getFromSupply(BugType.QUEEN_BEE);
//        Token blackSoldier = p2.getFromSupply(BugType.SOLDIER_ANT);
//
//        board.addToken(whiteBee, 0, 0);
//        board.addToken(blackBee, 0, 1);
//        board.addToken(whiteSoldier, 1, 0);
//        board.addToken(blackSoldier, 1, 1);
//
//        long firstKey = board.getZobristKey();
//
//        board.removeToken(1, 1);
//        board.removeToken(1, 0);
//        board.removeToken(0, 1);
//        board.removeToken(0, 0);
//
//        board.addToken(whiteBee, 0, 0);
//        board.addToken(blackSoldier, 1, 1);
//        board.addToken(whiteSoldier, 1, 0);
//        board.addToken(blackBee, 0, 1);
//
//        // If board state is the same and next player also. It doesn't matter how that state was reached.
//        assertEquals(firstKey, board.getZobristKey());
//    }




}
