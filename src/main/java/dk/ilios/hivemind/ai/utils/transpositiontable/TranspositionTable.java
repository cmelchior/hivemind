package dk.ilios.hivemind.ai.utils.transpositiontable;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a Transposition table for an AlphaBetaMinimax algorithm
 *
 * @see http://en.wikipedia.org/wiki/Transposition_table
 * @see http://www.gamedev.net/topic/503234-transposition-table-question/
 */
public class TranspositionTable {

    public static final int NO_MATCH = 0;

    private Map<Long, Integer> table = new HashMap<Long, Integer>();

    public void addResult(long zobristKey, int result, int searchDepth) {
        if (result == NO_MATCH) return; // Ignore 0 results as we use them to indicate empty. Acceptable loss for now.
        table.put(zobristKey, result);
    }

    /**
     * Return the already calculated value for a zobrist key or NaN if no key exists.
     */
    public int getResult(long zobristKey) {
        Integer val = table.get(zobristKey);
        if (val == null) {
            return NO_MATCH;
        } else {
            return val;
        }
    }
}
