package dk.ilios.hivemind.model.creatures;

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.model.*;
import dk.ilios.hivemind.model.rules.Rules;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GrasshopperTest {

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
    public void testTargetSquares_overToken() {
        Board board = new Board(p1, p2);
        Token grasshopper = p1.getFromSupply(BugType.GRASSHOPPER);
        Token bee = p2.getFromSupply(BugType.QUEEN_BEE);
        board.addToken(grasshopper, 0, 0);
        board.addToken(bee, 1, 0);

        List<Hex> targets = Rules.getInstance().getTargetHexes(grasshopper, board);
        assertEquals(1, targets.size());
        assertEquals(board.getHex(2,0), targets.get(0));
    }

    /**
     * Grasshopper(W) cannot jump over empty spaces.
     *
     * | = = = = = = = = = = = = |
     * |           _ _           |
     * |         /+ + +\         |
     * |    _ _ /+ QBE +\ _ _    |
     * |  /+ + +\+ -B- +/+ + +\  |
     * | /+ ANT +\+_+_+/+ SPI +\ |
     * | \+ -B- +/     \+ -B- +/ |
     * |  \+_+_+/       \+_+_+/  |
     * |  /# # #\       /+ + +\  |
     * | /# HOP #\     /+ HOP +\ |
     * | \# -W- #/     \+ -B- +/ |
     * |  \#_#_#/       \+_+_+/  |
     * |                         |
     * | = = = = = = = = = = = = |
     */
    @Test
    public void testTargetSquares_cannotJumpOverEmptySpaces() {
        Board board = new Board(p1, p2);
        Token ant = p1.getFromSupply(BugType.GRASSHOPPER);
        board.addToken(ant, 0, 2);
        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 1, 0);
        board.addToken(p2.getFromSupply(BugType.SPIDER), 2, 0);
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 2, 1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1);

        List<Hex> targets = Rules.getInstance().getTargetHexes(ant, board);
        assertEquals(1, targets.size());
        assertTrue(targets.contains(board.getHex(0, 0)));
    }
}
