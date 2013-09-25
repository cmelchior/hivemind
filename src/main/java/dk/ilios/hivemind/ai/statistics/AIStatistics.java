package dk.ilios.hivemind.ai.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Utility class for keeping track of AI performance
 */
public class AIStatistics {

    private boolean DEBUG = true;

    // Result arrays
    ArrayList<Long> millisecondsPrMove = new ArrayList<Long>(); // Time in milliseconds pr. move of the game
    ArrayList<Integer> gameStatesEvaluatedPrSecond = new ArrayList<Integer>(); // Normalized "performance" value
    ArrayList<Integer> positionsEvaluatedPrMove = new ArrayList<Integer>(); // Number of positions looked at when finding a move.
    ArrayList<Integer> cacheHits = new ArrayList<Integer>(); // Number of cachehits

    int numberOfCutoffs = 0;
    int totalCutoffTurns = 0; // Average number of moves before cutoffs is totalCutoffTurns/numberOfCutoffs

    int branches = 0;
    int nodes = 0;
    int cacheHit = 0;

    // Temporary data
    String currentKey;
    int aiDepth = 3;
    int positionsEvaluated = 0;    // How many moves has been considered when getting the next move.

    /**
     * A new move request has been made.
     */
    public void startCalculatingNextMove() {
        currentKey = UUID.randomUUID().toString();
        StopWatch.getInstance().start(currentKey);
        positionsEvaluated = 0;
    }

    /**
     * The AI has returned a move
     */
    public void moveCalculated() {
        long time = StopWatch.getInstance().stop(currentKey).getElapsedTimeInMillis();
        int movesPrSecond = (int) (positionsEvaluated / (time / 1000d));

        millisecondsPrMove.add(time);
        gameStatesEvaluatedPrSecond.add(movesPrSecond);
        positionsEvaluatedPrMove.add(positionsEvaluated);
        cacheHits.add(cacheHit);

        if (DEBUG) {
            System.out.println("Turn length: " + (time/1000d) + " s.");
        }
    }

    /**
     * The AI has evaluated a game state using the heuristic function
     */
    public void boardEvaluated() {
        positionsEvaluated++;
    }

    public double getAverageTimePrMove() {
        int result = 0;
        for (long i : millisecondsPrMove) {
            result += i;
        }

        return result / (double) millisecondsPrMove.size();
    }

    public long getMeanTimePrMove() {
        if (millisecondsPrMove.size() == 0) return 0;
        Long[] list = new Long[millisecondsPrMove.size()];
        millisecondsPrMove.toArray(list);
        Arrays.sort(list);
        return list[list.length/2];
    }

    public long getMaxTimePrMove() {
        long result = 0;
        for (long i : millisecondsPrMove) {
            if (i > result) {
                result = i;
            }
        }
        return result;
    }

    public double getAveragePositionsEvaluatedPrMove() {
        int result = 0;
        for (int i : positionsEvaluatedPrMove) {
            result += i;
        }

        return result / (double) positionsEvaluatedPrMove.size();
    }

    public int getMeanPositionsEvaluatedPrMove() {
        if (positionsEvaluatedPrMove.size() == 0) return 0;
        Integer[] list = new Integer[positionsEvaluatedPrMove.size()];
        positionsEvaluatedPrMove.toArray(list);
        Arrays.sort(list);
        return list[list.length/2];
    }

    public int getMaxPositionsEvaluatedPrMove() {
        int result = 0;
        for (int i : positionsEvaluatedPrMove) {
            if (i > result) {
                result = i;
            }
        }

        return result;
    }

    public List<Long> getMillisecondsPrMove() {
        return millisecondsPrMove;
    }

    public List<Integer> getGameStatesEvaluatedPrSecond() {
        return gameStatesEvaluatedPrSecond;
    }

    public List<Integer> getPositionsEvaluatedPrMove() {
        return positionsEvaluatedPrMove;
    }

    public int getAverageBranchFactor() {
        return (nodes > 0) ? Math.round(branches/nodes) : 0;
    }

    public List<Integer> getCacheHits() {
        return cacheHits;
    }

    public void nodeBranched(int size) {
        nodes++;
        branches += size;
    }

    public void cacheHit() {
        cacheHit++;
    }

    public void cutOffAfter(int moveEvaluated) {
        numberOfCutoffs++;
        totalCutoffTurns += moveEvaluated;
    }

    public double getAverageMovesEvaluatedBeforeCutoff() {
        if (totalCutoffTurns == 0) return -1d;
        return totalCutoffTurns/(double) numberOfCutoffs;
    }

}
