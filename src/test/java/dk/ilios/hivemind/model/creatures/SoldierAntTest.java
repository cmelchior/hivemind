package dk.ilios.hivemind.model.creatures;

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.model.*;
import dk.ilios.hivemind.model.rules.Rules;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

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
        Board board = new Board(p1, p2);
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
        Board board = new Board(p1, p2);
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
        Board board = new Board(p1, p2);
        Token ant = p1.getFromSupply(BugType.SOLDIER_ANT);
        board.addToken(ant, 0, 0);

        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 0, -1);
        board.addToken(p2.getFromSupply(BugType.SPIDER), 1, 0);
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), -1, 1);

        assertFalse(Rules.getInstance().isFreeToMove(ant, board));
    }

    /**
     * A1 is almost surrounded, but still free to move.
     *
     * | = = = = = = = = = = = = | 
     * |                  _ _    |
     * |                /# # #\  |
     * |           _ _ /# S2  #\ |
     * |         /+ + +\# -W- #/ |
     * |    _ _ /+ A1  +\#_#_#/  |
     * |  /+ + +\+ -B- +/# # #\  |
     * | /+ B2  +\+_+_+/#  Q  #\ |
     * | \+ -B- +/+ + +\# -W- #/ |
     * |  \+_+_+/+  Q  +\#_#_#/  |
     * |        \+ -B- +/        |
     * |         \+_+_+/         |
     * |         /+ + +\         |
     * |        /+ A2  +\        |
     * |        \+ -B- +/        |
     * |         \+_+_+/         |
     * |                         |
     * | = = = = = = = = = = = = |
     */
    @Test
    public void freeToMove_almostSurrounded() {
        Board board = new Board(p1, p2);

        // Black setup
        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 0, 3);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 4);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 2);
        board.addToken(p2.getFromSupply(BugType.BEETLE), -1, 3);

        // White setup
        board.addToken(p1.getFromSupply(BugType.SPIDER), 1, 1);
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 1, 2);

        assertTrue(Rules.getInstance().isFreeToMove(board.getHex(0, 2).getTopToken(), board));
    }

    /**
     *
     * | = = = = = = = = = = = = = = = = = = = |
     * |                         _ _           |
     * |                       /# # #\         |
     * |                  _ _ /# ANT #\        |
     * |                /# # #\# -W- #/        |
     * |               /# ANT #\#_#_#/         |
     * |               \# -W- #/               |
     * |                \#_#_#/                |
     * |                /# # #\                |
     * |           _ _ /# SPI #\               |
     * |         /     \# -W- #/               |
     * |    _ _ /  WIN  \#_#_#/         _ _    |
     * |  /+ + +\       /# # #\       /# # #\  |
     * | /+ B1  +\ _ _ /# QBE #\ _ _ /# ANT #\ |
     * | \+ -B- +/+ + +\# -W- #/# # #\# -W- #/ |
     * |  \+_+_+/+ QBE +\#_#_#/# SPI #\#_#_#/  |
     * |  /+ + +\+ -B- +/+ + +\# -W- #/+ + +\  |
     * | /+ A1  +\+_+_+/+ ANT +\#_#_#/+ HOP +\ |
     * | \+ -B- +/+ + +\+ -B- +/     \+ -B- +/ |
     * |  \+_+_+/+ A2  +\+_+_+/       \+_+_+/  |
     * |        \+ -B- +/                      |
     * |         \+_+_+/                       |
     * |                                       |
     * | = = = = = = = = = = = = = = = = = = = |
     *
     */
    @Test
    public void canMoveToEndGame() {
        Board board = new Board(p1, p2);

        // Black setup
        board.addToken(p2.getFromSupply(BugType.QUEEN_BEE), 0, 3);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 4);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), -1, 4);
        board.addToken(p2.getFromSupply(BugType.BEETLE), -1, 3);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 3);
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 3, 2);

        // White setup
        board.addToken(p1.getFromSupply(BugType.SOLDIER_ANT), 1, 0);
        board.addToken(p1.getFromSupply(BugType.SPIDER), 1, 1);
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 1, 2);
        board.addToken(p1.getFromSupply(BugType.SOLDIER_ANT), 2, -1);
        board.addToken(p1.getFromSupply(BugType.SPIDER), 2, 2);
        board.addToken(p1.getFromSupply(BugType.SOLDIER_ANT), 3, 1);

        Token ant = board.getHex(2, -1).getTopToken();
        List<Hex> targets = Rules.getInstance().getBugSpecificRules(ant).getTargetHexes(ant, board);
        assertTrue(targets.contains(board.getHex(0, 2)));
    }



}
