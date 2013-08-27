package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.heuristics.TestSetups;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Player;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MonteCarloAITest {

    @Test
    public void testCanDetectWinTurnOne() {
        final HiveAI ai = new UCTMonteCarloTreeSearchAI("MCTS", 3, 10000);

        Game game = new Game();
        Player p1 = new Player("White", Player.PlayerType.WHITE); p1.fillBaseSupply();
        Player p2 = new Player("Black", Player.PlayerType.BLACK); p2.fillBaseSupply();

        game.addPlayers(p1, p2);
        game.setTurnLimit(10);
        game = TestSetups.sureWinInOneTurn(game);

        GameCommand command = ai.nextMove(game, game.getBoard());

        assertEquals(1, command.getToQ());
        assertEquals(1, command.getToR());
    }
}
