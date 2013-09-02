package dk.ilios.hivemind.ai.controller;

import dk.ilios.hivemind.ai.HiveAI;
import dk.ilios.hivemind.ai.statistics.GameStatistics;
import dk.ilios.hivemind.game.CommandProvider;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller class for executing AI battles.
 * Given x number of opponents, y number of battles are executed among all pairs.
 * Results are then printed to the screen.
 */
public class AIGameController {

    private int turnLimit;
    private int numberOfMatches;

    private Set<HiveAI> opponents = new HashSet<HiveAI>();
    private List<GameStatistics> gameResults = new ArrayList<GameStatistics>();

    public void addOpponent(HiveAI opponent) {
        opponents.add(opponent);
    }

    /**
     * Set turn limit pr. player
     */
    public void setTurnLimit(int turnLimit) {
        this.turnLimit = turnLimit;
    }

    public void setNumberOfMatches(int numberOfMatches) {
        this.numberOfMatches = numberOfMatches;
    }

    public void start() {
        for (HiveAI oppA : opponents) {
            for (HiveAI oppB : opponents) {
                if (oppA.equals(oppB)) continue;
                for (int i = 0; i < numberOfMatches; i++) {
                    runGame(oppA, oppB, false);
                }
            }
        }
    }

    public void startSingleGame(HiveAI whitePlayer, HiveAI blackPlayer, boolean printGameState) {
        runGame(whitePlayer, blackPlayer, printGameState);
    }

    private void runGame(final HiveAI whiteAI, final HiveAI blackAI, boolean printGameState) {

        Player whitePlayer = new Player(whiteAI.getName(), Player.PlayerType.WHITE);
        whitePlayer.fillBaseSupply();
        whitePlayer.setCommandProvider(new CommandProvider() {

            @Override
            public GameCommand getCommand(Game currentState, Board board) {
                whiteAI.getAiStats().startCalculatingNextMove();
                GameCommand command = whiteAI.nextMove(currentState, board);
                whiteAI.getAiStats().moveCalculated();
                return command;
            }
        });

        Player blackPlayer = new Player(blackAI.getName(), Player.PlayerType.BLACK);
        blackPlayer.fillBaseSupply();
        blackPlayer.setCommandProvider(new CommandProvider() {
            @Override
            public GameCommand getCommand(Game currentState, Board board) {
                blackAI.getAiStats().startCalculatingNextMove();
                GameCommand command = blackAI.nextMove(currentState, board);
                blackAI.getAiStats().moveCalculated();
                return command;
            }
        });

        Game game = new Game();
        game.setTurnLimit(turnLimit);
        game.setPrintGameStateAfterEachMove(printGameState);
        game.addPlayers(whitePlayer, blackPlayer);
        try {
            game.start();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        GameStatistics statistics = game.getStatistics();
        statistics.addWhiteAIStats(whiteAI.getAiStats());
        statistics.addBlackAIStats(blackAI.getAiStats());

        gameResults.add(game.getStatistics());
    }

    public void printLog() {
        StringBuilder sb = new StringBuilder("Games done: \n");
        sb.append("================\n");
        for (GameStatistics stats : gameResults) {
            sb.append(stats.shortSummary());
            sb.append('\n');
        }
        sb.append("================");
        System.out.println(sb.toString());
    }
}
