package dk.ilios.hivemind.ai.heuristics;

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.model.BugType;
import dk.ilios.hivemind.model.Player;
import dk.ilios.hivemind.model.StandardPositionMode;
import dk.ilios.hivemind.model.Token;
import dk.ilios.hivemind.model.rules.Rules;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;

public class SimpleHeuristicTests {

    private HiveAsciiPrettyPrinter printer = new HiveAsciiPrettyPrinter();
    private BoardValueHeuristic heuristic;
    private Game game;

    @Before
    public void setup() {
        heuristic = new SimpleHeuristicV1();
    }

    private Game newGame() {
        Game game = new Game();
        Player p1 = new Player("White", Player.PlayerType.WHITE); p1.fillBaseSupply();
        Player p2 = new Player("Black", Player.PlayerType.BLACK); p2.fillBaseSupply();

        game.addPlayers(p1, p2);
        game.setTurnLimit(10);
        game.setStandardPositionMode(StandardPositionMode.DISABLED);
        return game;
    }

    @Test
    public void testWinSoonerThanLater_1() {
        Game g1 = newGame();
        Game g2 = newGame();

        TestSetups.sureWinInTwoTurns(g1);
        TestSetups.sureWinInTwoTurns(g2);

        g1.getBoard().moveToken(-1, 4, 2, 1); // Move A1 to T1
        g2.getBoard().addToken(g2.getBlackPlayer().getFromSupply(BugType.GRASSHOPPER), -1, 2);

        int score1 = heuristic.calculateBoardValue(g1);
        int score2 = heuristic.calculateBoardValue(g2);

        assertTrue(score1 < score2); // Black is scored negative
    }

    @Test
    public void testWinSoonerThanLater_2() {
        Game g1 = newGame();
        Game g2 = newGame();

        TestSetups.sureWinInTwoTurns(g1);
        TestSetups.sureWinInTwoTurns(g2);

        g1.getBoard().moveToken(-1, 4, 2, 1); // Move A1 to T1
        g2.getBoard().moveToken(-1, 4, 2, 3); // WTF?

        int score1 = heuristic.calculateBoardValue(g1);
        int score2 = heuristic.calculateBoardValue(g2);

        assertTrue(score1 < score2); // Black is scored negative
    }

    @Test
    public void testSame() {
        Game g1 = newGame();
        Game g2 = newGame();

        TestSetups.sureWinInTwoTurns(g1);
        TestSetups.sureWinInTwoTurns(g2);

        g1.getBoard().moveToken(-1, 4, 0, 2); // Ant moved
        g2.getBoard().moveToken(-1, 3, 0, 2); // Beetle moved

        int score1 = heuristic.calculateBoardValue(g1);
        int score2 = heuristic.calculateBoardValue(g2);

        Set<Token> free = Rules.getInstance().getFreeTokens(g1.getBlackPlayer(), g1.getBoard());
        Set<Token> free2 = Rules.getInstance().getFreeTokens(g2.getBlackPlayer(), g2.getBoard());

        assertTrue(score1 == score2); // Black is scored negative
    }



}
