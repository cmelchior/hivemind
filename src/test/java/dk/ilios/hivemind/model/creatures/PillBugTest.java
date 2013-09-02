package dk.ilios.hivemind.model.creatures;

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.model.Player;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

public class PillBugTest {

    HiveAsciiPrettyPrinter printer;
    Player p1;
    Player p2;

    @Before
    public void setup() {
        printer = new HiveAsciiPrettyPrinter();
        p1 = new Player("White", Player.PlayerType.WHITE);
        p1.fillBaseSupply();
        p1.usePillBugExpansion();
        p2 = new Player("Black", Player.PlayerType.BLACK);
        p2.fillBaseSupply();
        p2.usePillBugExpansion();
    }

    @Test
    public void notImplemented() {
        fail("Not implemented");
    }
}
