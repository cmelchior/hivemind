package dk.ilios.hivemind.ai.transpositiontable;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a Transposition table for an AlphaBetaMinimax algorithm
 *
 * @see http://en.wikipedia.org/wiki/Transposition_table
 * @see http://www.gamedev.net/topic/503234-transposition-table-question/
 * @see http://homepages.cwi.nl/~paulk/theses/Carolus.pdf
 * @see https://groups.google.com/forum/#!topic/rec.games.chess.computer/p8GbiiLjp0o
 */
public class TranspositionTable {

    private Map<Long, TranspositionTableEntry> table = new HashMap<Long, TranspositionTableEntry>();

    public void addResult(long zobristKey, int value, int depth, int valueType) {
        TranspositionTableEntry existingEntry = table.get(zobristKey);
        if (existingEntry == null || depth >= existingEntry.depth) {
            table.put(zobristKey, new TranspositionTableEntry(value, depth, valueType));
        }
    }

    /**
     * Return the already calculated value for a zobrist key or null if no key exists.
     */
    public TranspositionTableEntry getResult(long zobristKey) {
        return table.get(zobristKey);
    }
}
