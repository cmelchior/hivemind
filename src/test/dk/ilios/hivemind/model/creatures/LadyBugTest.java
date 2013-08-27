package dk.ilios.hivemind.model.creatures;

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.model.*;
import dk.ilios.hivemind.model.rules.Rules;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LadyBugTest {

    HiveAsciiPrettyPrinter printer;
    Player p1;
    Player p2;

    @Before
    public void setup() {
        printer = new HiveAsciiPrettyPrinter();
        p1 = new Player("White", Player.PlayerType.WHITE);
        p1.fillBaseSupply();
        p1.useLadyBugExpansion();
        p2 = new Player("Black", Player.PlayerType.BLACK);
        p2.fillBaseSupply();
        p2.useLadyBugExpansion();
    }


    @Test
    public void testTargetSquares_noMoves() {
        Board board = new Board();
        Token ant = p1.getFromSupply(BugType.LADY_BUG);
        Token bee = p2.getFromSupply(BugType.QUEEN_BEE);
        board.addToken(ant, 0, 0);
        board.addToken(bee, 1, 0);

        List<Hex> targets = Rules.getInstance().getTargetHexes(ant, board);
        assertEquals(0, targets.size());
    }

    /**
     * LadyBug has a complex movement pattern.
     * Following assumptions:
     *  - A LadyBug can either "Slide" or "Crawl" on top of a Hive
     *  - A LadyBug cannot end her move where she started.
     *
     * | = = = = = = = = = = = = |
     * |           _ _           |
     * |         /# # #\         |
     * |    _ _ /# LDY #\        |
     * |  /+ + +\# -W- #/        |
     * | /+ ANT +\#_#_#/         |
     * | \+ -B- +/+ + +\         |
     * |  \+_+_+/+ SPI +\ _ _    |
     * |  /+ + +\+ -B- +/+ + +\  |
     * | /+ BTL +\+_+_+/+ HOP +\ |
     * | \+ -B- +/     \+ -B- +/ |
     * |  \+_+_+/       \+_+_+/  |
     * |  /+ + +\                |
     * | /+ QBE +\               |
     * | \+ -B- +/               |
     * |  \+_+_+/                |
     * |                         |
     * | = = = = = = = = = = = = |
     */
    @Test
    public void testTargetSquares_largeHive() {
        Board board = new Board();
        Token ant = p1.getFromSupply(BugType.LADY_BUG);
        board.addToken(ant, 1, 0);
        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 0, 3);
        board.addToken(p2.getFromSupply(BugType.SPIDER), 1, 1);
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 2, 1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1);
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, 2);

        List<Hex> targets = Rules.getInstance().getTargetHexes(ant, board);
        assertEquals(9, targets.size());
        assertFalse(targets.contains(board.getHex(0, 4)));
        assertFalse(targets.contains(board.getHex(1, 3)));
    }

    /**
     * A LadyBug can either crawl or slide when on top of the hive.
     * When faced with a 2 stack, she just climbs up instead of sliding.
     *
     * | = = = = = = = = = = = = |
     * |           _ _           |
     * |         /# # #\         |
     * |    _ _ /# LDY #\        |
     * |  /+ + +\# -W- #/        |
     * | /+ ANT +\#_#_#/         |
     * | \+ -B- +/+ + +\         |
     * |  \+_+_+/+ SPI +\ _ _    |
     * |  /+ + +\+ -B- +/+ + +\  |
     * | /+ 2xB +\+_+_+/+ HOP +\ |
     * | \+ -B- +/     \+ -B- +/ |
     * |  \+_+_+/       \+_+_+/  |
     * |  /+ + +\                |
     * | /+ QBE +\               |
     * | \+ -B- +/               |
     * |  \+_+_+/                |
     * |                         |
     * | = = = = = = = = = = = = |
     */
    @Test
    public void testTargetSquares_movingOnTop() {
        Board board = new Board();
        Token ant = p1.getFromSupply(BugType.LADY_BUG);
        board.addToken(ant, 1, 0);
        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 0, 3);
        board.addToken(p2.getFromSupply(BugType.SPIDER), 1, 1);
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 2, 1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1);
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, 2);
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, 2);

        List<Hex> targets = Rules.getInstance().getTargetHexes(ant, board);
        assertEquals(9, targets.size());
        assertFalse(targets.contains(board.getHex(0, 4)));
        assertFalse(targets.contains(board.getHex(1, 3)));

    }
}
