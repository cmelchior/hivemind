package dk.ilios.hivemind.ai.transpositiontable;

import dk.ilios.hivemind.game.GameCommand;

/**
 * Wrapper for transposition table entries.
 *
 * @see http://chessprogramming.wikispaces.com/Node+Types#ALL
 */
public class TranspositionTableEntry {

    public static final int PV_NODE = 0;        // Exact match. For bounds [a,b]: a < x < b
    public static final int CUT_NODE = 1;       // Lower bound - Beta cutoff. Fail high. For bounds [a,b]: x >= b
    public static final int ALL_NODE = 2;       // No move higher than alpha. Fail low. For bounds [a,b]: x <= a

    public final int value; // Value for the node
    public final int depth; // Search depth
    public final int type;  // Type of value [PV_NODE, CUT_NODE, ALL_NODE]
    public final GameCommand move;

    public TranspositionTableEntry(int value, int depth, int type, GameCommand move) {
        this.value = value;
        this.depth = depth;
        this.type = type;
        this.move = move;
    }
}
