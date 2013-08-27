package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic;
import dk.ilios.hivemind.ai.heuristics.SimpleHeuristicV1;
import dk.ilios.hivemind.ai.heuristics.TestSetups;
import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.game.CommandProvider;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.BugType;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Player;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by cme on 7/30/13.
 */
public class SimpleMinMaxAITest {

    HiveAsciiPrettyPrinter printer;
    Player p1;
    Player p2;

    @Before
    public void setup() {
        printer = new HiveAsciiPrettyPrinter();
        p1 = new Player("White", Player.PlayerType.WHITE);
        p2 = new Player("Black", Player.PlayerType.BLACK);

        // Very simplistic game
        p1.addToSupply(BugType.QUEEN_BEE, 1);
        p1.addToSupply(BugType.SPIDER, 1);
        p2.addToSupply(BugType.QUEEN_BEE, 1);
        p2.addToSupply(BugType.SPIDER, 1);
    }

    @Test
    public void testOneDepth() {
        final HiveAI ai = new SimpleMinMaxAI("OneDepth", new BoardValueHeuristic() {
            @Override
            public int calculateBoardValue(Game state) {
                // Give queen at 0,1 max priority for player 2
                Hex queenHex = p2.getQueen().getHex();
                if (queenHex != null && queenHex.getQ() == 0 && queenHex.getR() == 1) {
                    return -100;
                } else {
                    return 0;
                }
            }
        }, 1, 30000);

        p1.setCommandProvider(new CommandProvider() {
            @Override
            public GameCommand getCommand(Game currentState, Board board) {
                return new GameCommand(Hex.SUPPLY, Hex.SUPPLY, 0, 0, p1.getFromSupply(BugType.QUEEN_BEE), false);
            }
        });

       p2.setCommandProvider(new CommandProvider() {
           @Override
           public GameCommand getCommand(Game currentState, Board board) {
               return ai.nextMove(currentState, board);
           }
       });

        // Test placement of first bug once another bug has been placed
        Game game = new Game();
        game.setManualStepping(true);
        game.addPlayers(p1, p2);
        game.setTurnLimit(1);
        game.start();
        game.continueGame(); // Turn 1 (White)
        GameCommand command = ai.nextMove(game, game.getBoard()); // Turn 1 (Black)

        assertEquals(p2.getQueen(), command.getToken());
        assertEquals(0, command.getToQ());
        assertEquals(1, command.getToR());
    }

    @Test
    public void testTwoDepth() {
        final HiveAI ai = new SimpleMinMaxAI("TwoDepth", new BoardValueHeuristic() {
            @Override
            public int calculateBoardValue(Game state) {
                // White really wants his queen at (0,1), so black will put his token there.
                Hex whiteQueenHex = state.getWhitePlayer().getQueen().getHex();
                if (whiteQueenHex.getQ() == 0 && whiteQueenHex.getR() == 1) {
                    return 100; // White really likes this move
                } else {
                    return -25;
                }
            }
        }, 2, 30000);

        p1.setCommandProvider(new CommandProvider() {
            @Override
            public GameCommand getCommand(Game currentState, Board board) {
                int turn = currentState.getActivePlayer().getTurns();
                if (turn == 0) {
                    return new GameCommand(Hex.SUPPLY, Hex.SUPPLY, 0, 0, p1.getFromSupply(BugType.QUEEN_BEE), false);
                } else {
                    return GameCommand.PASS;
                }
            }
        });

        p2.setCommandProvider(new CommandProvider() {
            @Override
            public GameCommand getCommand(Game currentState, Board board) {
                return ai.nextMove(currentState, board);
            }
        });

        // Test placement of first bug once another bug has been placed
        Game game = new Game();
        game.setManualStepping(true);
        game.addPlayers(p1, p2);
        game.setTurnLimit(2);
        game.start();
        game.continueGame(); // Turn 1 (White)

        GameCommand command = ai.nextMove(game, game.getBoard()); // Turn 1 (Black)

        assertEquals(0, command.getToQ());
        assertEquals(1, command.getToR());
    }

    @Test
    public void testCanDetectOneTurnWin() {
        final HiveAI ai = new SimpleMinMaxAI("OneDepth", new SimpleHeuristicV1(), 1, 30000);

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

    @Test
    public void testCanDetectWinTurnOne() {
        final HiveAI ai = new SimpleMinMaxAI("SimpleMinMax", new SimpleHeuristicV1(), 1, 30000);

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


    @Test
    public void testCanDetectWinTurnTwo() {
        final HiveAI ai = new SimpleMinMaxAI("SimpleMinMax", new SimpleHeuristicV1(), 3, 30000);

        Game game = new Game();
        Player p1 = new Player("White", Player.PlayerType.WHITE); p1.fillBaseSupply();
        Player p2 = new Player("Black", Player.PlayerType.BLACK); p2.fillBaseSupply();

        game.addPlayers(p1, p2);
        game.setTurnLimit(10);
        game = TestSetups.sureWinInTwoTurns(game);

        GameCommand command = ai.nextMove(game, game.getBoard());

        assertEquals(2, command.getToQ());
        assertEquals(1, command.getToR());
    }
}
