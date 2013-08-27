package dk.ilios.hivemind.model.creatures;

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.model.*;
import dk.ilios.hivemind.model.rules.Rules;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SoldierAntTest {

    HiveAsciiPrettyPrinter printer;
    Player p1;
    Player p2;

    @Before
    public void setup() {
        printer = new HiveAsciiPrettyPrinter();
        p1 = new Player("White", Player.PlayerType.WHITE);
        p2 = new Player("Black", Player.PlayerType.BLACK);
        p1.fillBaseSupply();
        p2.fillBaseSupply();
    }


    @Test
    public void testTargetSquares_aroundHive() {
        Board board = new Board();
        Token ant = p1.getFromSupply(BugType.SOLDIER_ANT);
        Token bee = p2.getFromSupply(BugType.QUEEN_BEE);
        board.addToken(ant, 0, 0);
        board.addToken(bee, 1, 0);

        List<Hex> targets = Rules.getInstance().getTargetHexes(ant, board);
        assertEquals(5, targets.size());
        assertEquals(board.getHex(1,-1), targets.get(0));
        assertEquals(board.getHex(0,1), targets.get(4));
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
    public void testTargetSquares_cannotEnterGate() {
        Board board = new Board();
        Token ant = p1.getFromSupply(BugType.SOLDIER_ANT);
        board.addToken(ant, 0, 0);
        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 1, 0);
        board.addToken(p2.getFromSupply(BugType.SPIDER), 2, 0);
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 2, 1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1);
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, 2);

        List<Hex> targets = Rules.getInstance().getTargetHexes(ant, board);
        assertEquals(11, targets.size());
        assertFalse(targets.contains(board.getHex(1, 1)));
    }

    @Test
    public void freeToMove_surrounded() {
        Board board = new Board();
        Token ant = p1.getFromSupply(BugType.SOLDIER_ANT);
        board.addToken(ant, 0, 0);

        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 0, -1);
        board.addToken(p2.getFromSupply(BugType.SPIDER), 1, 0);
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), -1, 1);

        assertFalse(Rules.getInstance().isFreeToMove(ant, board));
    }
}
