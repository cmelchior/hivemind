package dk.ilios.hivemind.model;

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.model.rules.Rules;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class RulesTest {

    HiveAsciiPrettyPrinter printer;
    Player p1;
    Player p2;

    @Before
    public void setup() {
        printer = new HiveAsciiPrettyPrinter();
        p1 = new Player("White", Player.PlayerType.WHITE);
        p2 = new Player("Black", Player.PlayerType.BLACK);

        p1.fillBaseSupply();
        p1.useAllExpansions();
        p2.fillBaseSupply();
        p2.useAllExpansions();
    }

    @Test
    public void canSlideTo_noSpace() {
        Board board = new Board(p1, p2);
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1);

        assertFalse(Rules.getInstance().canSlideTo(board.getHex(0,0), board.getHex(1,-1), board));
    }

    @Test
    public void canSlideTo_success() {
        Board board = new Board(p1, p2);
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1);

        assertTrue(Rules.getInstance().canSlideTo(board.getHex(0,0), board.getHex(1,-1), board));
    }

    @Test
    public void canSlideTo_blocked() {
        Board board = new Board(p1, p2);
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0);

        assertFalse(Rules.getInstance().canSlideTo(board.getHex(0,0), board.getHex(1,-1), board));
    }

    @Test
    public void canCrawlDown_success() {
        Board board = new Board(p1, p2);
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, -1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1);

        assertTrue(Rules.getInstance().canCrawlDown(board.getHex(0, -1), board.getHex(0, 0), board));
    }


    /**
     * Queen Bee cannot move to Hex A, as the One Hive rule is broken during the move
     *
     * | = = = = = = = = = = = = |
     * |           _ _           |
     * |         /+ + +\         |
     * |    _ _ /+ ANT +\ _ _    |
     * |  /# # #\+ -B- +/# # #\  |
     * | /# QBE #\+_+_+/# BTL #\ |
     * | \# -W- #/     \# -W- #/ |
     * |  \#_#_#/       \#_#_#/  |
     * |  /     \       /+ + +\  |
     * | /   A   \ _ _ /+ ANT +\ |
     * | \       /+ + +\+ -B- +/ |
     * |  \ _ _ /+ HOP +\+_+_+/  |
     * |        \+ -B- +/        |
     * |         \+_+_+/         |
     * |                         |
     * | = = = = = = = = = = = = |
     */
    @Test
    public void canSlideTo_oneHiveBrokenDuringMove() {
        Board board = new Board(p1, p2);
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1);
        board.addToken(p2.getFromSupply(BugType.BEETLE), 2, -1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 2, 0);
        board.addToken(p2.getFromSupply(BugType.GRASSHOPPER), 1, 1);

        assertFalse(Rules.getInstance().canSlideTo(board.getHex(0, 0), board.getHex(0, 1), board));
    }

    /**
     * Trying to slide a empty hex to a occupied hex
     */
    @Test
    public void canSlideTo_illegalArguments() {
        Board board = new Board(p1, p2);
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0);

        assertFalse(Rules.getInstance().canSlideTo(board.getHex(0, 1), board.getHex(0, 0), board));
    }

    @Test
    public void canSlideOnTopOfHive() {
        Board board = new Board(p1, p2);
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0);
        board.addToken(p1.getFromSupply(BugType.LADY_BUG), 0, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0);

        assertTrue(Rules.getInstance().canSlideTo(board.getHex(0, 0), board.getHex(1, 0), board));
    }

    /**
     * Cannot slide to a filled hex (can crawl up though)
     */
    @Test
    public void canSlideOnTopOfHive_blockedSinglePiece() {
        Board board = new Board(p1, p2);
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0);
        board.addToken(p1.getFromSupply(BugType.LADY_BUG), 0, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0);
        board.addToken(p2.getFromSupply(BugType.BEETLE), 1, 0);

        assertFalse(Rules.getInstance().canSlideTo(board.getHex(0, 0), board.getHex(1, 0), board));
    }

    @Test
    public void canSlideOnTopOfHive_blockedGuards() {
        Board board = new Board(p1, p2);

        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 0, 0);
        board.addToken(p1.getFromSupply(BugType.LADY_BUG), 0, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1);
        board.addToken(p1.getFromSupply(BugType.SOLDIER_ANT), 1, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1);
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, 1);

        assertFalse(Rules.getInstance().canCrawlDown(board.getHex(0, 0), board.getHex(1, 0), board));
    }

    @Test
    public void canCrawlDown_blocked() {
        Board board = new Board(p1, p2);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1);
        board.addToken(p1.getFromSupply(BugType.SOLDIER_ANT), 1, 0);
        board.addToken(p1.getFromSupply(BugType.QUEEN_BEE), 1, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, 1);
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, 1);

        assertFalse(Rules.getInstance().canCrawlDown(board.getHex(1, 0), board.getHex(0, 0), board));
    }

    /**
     * Crawl from ground to top of hive
     */
    @Test
    public void canCrawlTo_success() {
        Board board = new Board(p1, p2);
        board.addToken(p1.getFromSupply(BugType.BEETLE), 0, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1);

        assertTrue(Rules.getInstance().canCrawlUp(board.getHex(0, 0), board.getHex(1, -1), board));
    }

    /**
     * Crawl from ground to top of hive, but blocked by guarding towers.
     */
    @Test
    public void canCrawlTo_blockedHigh() {
        Board board = new Board(p1, p2);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1);
        board.addToken(p2.getFromSupply(BugType.BEETLE), 0, -1);
        board.addToken(p1.getFromSupply(BugType.BEETLE), 0, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, -1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0);
        board.addToken(p2.getFromSupply(BugType.BEETLE), 1, 0);

        assertFalse(Rules.getInstance().canSlideTo(board.getHex(0, 0), board.getHex(1, -1), board));
    }

    /**
     * Crawl between two guards (effectively sliding).
     */
    @Test
    public void canCrawlTo_blockedLow() {
        Board board = new Board(p1, p2);
        board.addToken(p1.getFromSupply(BugType.BEETLE), 0, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0);

        assertFalse(Rules.getInstance().canCrawlUp(board.getHex(0, 0), board.getHex(1, -1), board));
    }

    @Test
    public void getMimicList_onGround() {
        Board board = new Board(p1, p2);
        Token mos1 = p1.getFromSupply(BugType.MOSQUITO);
        Token bee = p2.getFromSupply(BugType.QUEEN_BEE);
        mos1.mimic(bee);

        board.addToken(mos1, 0, 0);
        board.addToken(bee, 1, 0);

        List<Token> result = Rules.getInstance().getMimicList(mos1, board);
        assertEquals(1, result.size());
        assertEquals(bee, result.get(0));
    }

    @Test
    public void getMimicList_onHive() {
        Board board = new Board(p1, p2);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 0, -1);
        board.addToken(p1.getFromSupply(BugType.BEETLE), 0, 0);
        board.addToken(p2.getFromSupply(BugType.SOLDIER_ANT), 1, 0);

        Token mos1 = p1.getFromSupply(BugType.MOSQUITO);
        board.addToken(mos1, 0, 0);

        List<Token> result = Rules.getInstance().getMimicList(mos1, board);
        assertEquals(0, result.size());
    }


}