package dk.ilios.hivemind.model.creatures;

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.model.*;
import dk.ilios.hivemind.model.rules.Rules;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MosquitoTest {

    HiveAsciiPrettyPrinter printer;
    Player p1;
    Player p2;

    @Before
    public void setup() {
        printer = new HiveAsciiPrettyPrinter();
        p1 = new Player("White", Player.PlayerType.WHITE);
        p1.fillBaseSupply();
        p1.useMosquitoExpansion();
        p2 = new Player("Black", Player.PlayerType.BLACK);
        p2.fillBaseSupply();
        p2.useMosquitoExpansion();
    }


    /**
     * A mosquito has no move, so copying a mosquito does nothing.
     */
    @Test
    public void testTargetSquares_noMove() {
        Board board = new Board();
        Token mos1 = p1.getFromSupply(BugType.MOSQUITO);
        Token mos2 = p2.getFromSupply(BugType.MOSQUITO);
        mos1.mimic(mos2);

        board.addToken(mos1, 0, 0);
        board.addToken(mos2, 1, 0);

        List<Hex> targets = Rules.getInstance().getTargetHexes(mos1, board);
        assertEquals(0, targets.size());
    }

    /**
     * Copying another bug grants movement.
     */
    @Test
    public void testTargetSquares_copyMovement() {
        Board board = new Board();
        Token mos1 = p1.getFromSupply(BugType.MOSQUITO);
        Token bee = p2.getFromSupply(BugType.QUEEN_BEE);
        mos1.mimic(bee);

        board.addToken(mos1, 0, 0);
        board.addToken(bee, 1, 0);

        List<Hex> targets = Rules.getInstance().getTargetHexes(mos1, board);
        assertEquals(2, targets.size());
        assertTrue(targets.contains(board.getHex(1, -1)));
        assertTrue(targets.contains(board.getHex(0,1)));
    }
}
