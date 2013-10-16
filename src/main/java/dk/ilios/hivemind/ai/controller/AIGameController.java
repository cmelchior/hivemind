package dk.ilios.hivemind.ai.controller;

import dk.ilios.hivemind.ai.HiveAI;
import dk.ilios.hivemind.ai.statistics.GameStatistics;
import dk.ilios.hivemind.game.CommandProvider;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.Player;
import dk.ilios.hivemind.model.StandardPositionMode;

import java.util.*;
import java.util.concurrent.*;

/**
 * Controller class for executing AI battles.
 * Given x number of opponents, y number of battles are executed among all pairs.
 * Results are then printed to the screen.
 */
public class AIGameController {

    // How many games can run simultaneously. Max should be #CPUs - 1 to avoid CPU contention.
    // Look into if it is possible to force Java to use different CPU's. Right now we just cross fingers and hope.
    private static final int THREADS = 3;

    private int turnLimit;
    private int numberOfMatches;

    private long duration;
    private Set<HiveAI> opponents = new HashSet<HiveAI>();
    private List<GameStatistics> gameResults = Collections.synchronizedList(new ArrayList<GameStatistics>());

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
        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        for (HiveAI oppA : opponents) {
            for (HiveAI oppB : opponents) {
                if (oppA.equals(oppB)) continue;
                for (int i = 0; i < numberOfMatches; i++) {
                    final HiveAI a = oppA.copy();
                    final HiveAI b = oppB.copy();
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            runGame(a, b, false);
                        }
                    });
                }
            }
        }

        // Wait for tasks to finish
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            /* Ignore */
        }

        duration = System.currentTimeMillis() - start;
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
        game.setStandardPositionMode((whiteAI.maintainsStandardPosition() || blackAI.maintainsStandardPosition()) ? StandardPositionMode.ENABLED : StandardPositionMode.DISABLED);
        try {
            game.start();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        GameStatistics statistics = game.getStatistics();
        statistics.addWhiteAIStats(whiteAI.getAiStats());
        statistics.addBlackAIStats(blackAI.getAiStats());

        gameResults.add(game.getStatistics());
        System.out.println(game.getStatistics().shortSummary());
    }

    public void printLog(boolean longSummary) {
        StringBuilder sb = new StringBuilder("Games done: " + (duration/1000f) + "s.\n");
        sb.append("================\n");
        for (GameStatistics stats : gameResults) {
            sb.append((longSummary ? stats.longSummary() : stats.shortSummary()));
            sb.append('\n');
        }
        sb.append("================");
        System.out.println(sb.toString());
    }
}
