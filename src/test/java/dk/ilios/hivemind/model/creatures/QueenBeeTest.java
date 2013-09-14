package dk.ilios.hivemind.model.creatures;

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.model.*;
import dk.ilios.hivemind.model.rules.Rules;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class QueenBeeTest {

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
    public void testTargetSquares_startingPosition() {
        Board board = new Board(p1, p2);
        Token bee = p1.getFromSupply(BugType.QUEEN_BEE);
        Token ant = p2.getFromSupply(BugType.SOLDIER_ANT);
        board.addToken(bee, 0, 0);
        board.addToken(ant, 1, -1);

        List<Hex> targets = Rules.getInstance().getTargetHexes(bee, board);
        assertEquals(2, targets.size());
        assertEquals(board.getHex(0,-1), targets.get(0));
        assertEquals(board.getHex(1,0), targets.get(1));
    }
}
