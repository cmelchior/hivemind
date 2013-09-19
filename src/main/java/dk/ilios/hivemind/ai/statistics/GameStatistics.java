package dk.ilios.hivemind.ai.statistics;

import dk.ilios.hivemind.game.GameStatus;
import dk.ilios.hivemind.model.Player;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Class that keeps track of a game
 */
public class GameStatistics {

    private long start; // Start time in milliseconds from epox
    private long durartionInMillis; // Duration of game in milliseconds

    private String whiteName = "?";
    private int whiteTurns = 0;
    private int whiteMoves = 0;
    private int whitePasses = 0;

    private String blackName = "?";
    private int blackTurns = 0;
    private int blackMoves = 0;
    private int blackPasses = 0;

    private GameStatus status = GameStatus.RESULT_NOT_STARTED;

    private AIStatistics whiteAI;
    private AIStatistics blackAI;

    public void addWhiteAIStats(AIStatistics whiteAIStats) {
        this.whiteAI = whiteAIStats;
    }

    public void addBlackAIStats(AIStatistics blackAIStats) {
        this.blackAI = blackAIStats;
    }

    public String shortSummary() {
        return String.format("%s (%s) vs. %s (%s) - %s ms.: %s", whiteName, whiteTurns, blackName, blackTurns, durartionInMillis, status);
    }

    public String longSummary() {
        StringBuilder sb = new StringBuilder(shortSummary());
        sb.append('\n');
        sb.append("Branching: " + whiteAI.getAverageBranchFactor() + " vs. " + blackAI.getAverageBranchFactor());
        sb.append('\n');
        sb.append("Time pr move (max.): "  + whiteAI.getMaxTimePrMove() + " vs. " + blackAI.getMaxTimePrMove());
        sb.append('\n');
        sb.append("Time pr move (avg.): "  + whiteAI.getAverageTimePrMove() + " vs. " + blackAI.getAverageTimePrMove());
        sb.append('\n');
        sb.append("Time pr move (mean): "  + whiteAI.getMeanTimePrMove() + " vs. " + blackAI.getMeanTimePrMove());
        sb.append('\n');
        sb.append("Positions pr move (max.): "  + whiteAI.getMaxPositionsEvaluatedPrMove() + " vs. " + blackAI.getMaxPositionsEvaluatedPrMove());
        sb.append('\n');
        sb.append("Positions pr move (avg.): "  + whiteAI.getAveragePositionsEvaluatedPrMove() + " vs. " + blackAI.getAveragePositionsEvaluatedPrMove());
        sb.append('\n');
        sb.append("Positions pr move (mean): "  + whiteAI.getMeanPositionsEvaluatedPrMove() + " vs. " + blackAI.getMeanPositionsEvaluatedPrMove());
        sb.append('\n');
        sb.append("------------------");
        sb.append('\n');
        sb.append(whiteName + " time pr. move: " + Arrays.toString(whiteAI.getMillisecondsPrMove().toArray()));
        sb.append('\n');
        sb.append(whiteName + " game states pr. sec.: " + Arrays.toString(whiteAI.getGameStatesEvaluatedPrSecond().toArray()));
        sb.append('\n');
        sb.append(whiteName + " game states pr. move: " + Arrays.toString(whiteAI.getPositionsEvaluatedPrMove().toArray()));
        sb.append('\n');
        sb.append(whiteName + " cache hits pr. move: " + Arrays.toString(whiteAI.getCacheHits().toArray()));
        sb.append('\n');
        sb.append(blackName + " time pr. move: " + Arrays.toString(blackAI.getMillisecondsPrMove().toArray()));
        sb.append('\n');
        sb.append(blackName + " game states pr. sec.: " + Arrays.toString(blackAI.getGameStatesEvaluatedPrSecond().toArray()));
        sb.append('\n');
        sb.append(blackName + " game states pr. move: " + Arrays.toString(blackAI.getPositionsEvaluatedPrMove().toArray()));
        sb.append('\n');
        sb.append(blackName + " cache hits pr. move: " + Arrays.toString(blackAI.getCacheHits().toArray()));
        sb.append('\n');
        sb.append("------------------");

        return sb.toString();
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public void setWhiteName(String name) {
        this.whiteName = name;
    }

    public void setBlackName(String name) {
        this.blackName = name;
    }

    private void blackMoves() {
        blackMoves++;
        blackTurns++;
    }

    private void blackPasses() {
        blackPasses++;
        blackTurns++;
    }

    private void whiteMoves() {
        whiteMoves++;
        whiteTurns++;
    }

    private void whitePasses() {
        whitePasses++;
        whiteTurns++;
    }

    public void playerMoves(Player player) {
        if (player.getName().equals(whiteName)) {
            whiteMoves();
        } else {
            blackMoves();
        }
    }

    public void playerPasses(Player player) {
        if (player.getName().equals(whiteName)) {
            whitePasses();
        } else {
            blackPasses();
        }
    }

    public void stopGame() {
        durartionInMillis = new Date().getTime() - start;
    }

    public void startGame() {
        start = new Date().getTime();
    }
}
