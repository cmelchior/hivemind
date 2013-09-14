package dk.ilios.hivemind.model.creatures;

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.model.*;
import dk.ilios.hivemind.model.rules.Rules;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BeetleTest {

    HiveAsciiPrettyPrinter printer;
    Player p1;
    Player p2;

    @Before
    public void setup() {
        printer = new HiveAsciiPrettyPrinter();
        p1 = new Player("White", Player.PlayerType.WHITE);
        p1.fillBaseSupply();
        p2 = new Player("Black", Player.PlayerType.BLACK);
        p2.fillBaseSupply();
    }

    @Test
    public void testTargetSquares_startingPosition() {
        Board board = new Board(p1, p2);
        Token bee = p1.getFromSupply(BugType.BEETLE);
        Token ant = p2.getFromSupply(BugType.SOLDIER_ANT);
        board.addToken(bee, 0, 0);
        board.addToken(ant, 1, -1);

        List<Hex> targets = Rules.getInstance().getTargetHexes(bee, board);
        assertEquals(3, targets.size());
        assertEquals(board.getHex(0,-1), targets.get(0));
        assertEquals(board.getHex(1,-1), targets.get(1));
        assertEquals(board.getHex(1,0), targets.get(2));
    }

    /**
     * Testing scenario described here:
     * http://boardgamegeek.com/wiki/page/Hive_FAQ
     */
    @Test
    public void testTargetSquares_freedom_to_move_between_gates_blocked() {
        Board board = new Board(p1, p2);
        Token beetle = p1.getFromSupply(BugType.BEETLE);
        board.addToken(beetle, 0, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1);
        board.addToken(p2.getFromSupply(BugType.SPIDER), 1, 0);
        board.addToken(p2.getFromSupply(BugType.SPIDER), 1, 0);

        List<Hex> targets = Rules.getInstance().getTargetHexes(beetle, board);
        assertEquals(4, targets.size());
        assertFalse(targets.contains(board.getHex(1, -1)));
    }

    /**
     * Testing scenario described here:
     * http://boardgamegeek.com/wiki/page/Hive_FAQ
     */
    @Test
    public void testTargetSquares_freedom_to_move_between_gates() {
        Board board = new Board(p1, p2);
        Token bee = p1.getFromSupply(BugType.BEETLE);
        board.addToken(bee, 0, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1);
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 1, -1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0);

        List<Hex> targets = Rules.getInstance().getTargetHexes(bee, board);
        assertEquals(5, targets.size());
        assertTrue(targets.contains(board.getHex(1, -1)));
    }
}
