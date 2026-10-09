package dk.ilios.hivemind.ai.transpositiontable

import dk.ilios.hivemind.game.GameCommand

/**
 * Wrapper for transposition table entries.
 *
 * @see http://chessprogramming.wikispaces.com/Node+Types.ALL
 */
class TranspositionTableEntry(value: Int, depth: Int, type: Int, move: GameCommand?) {
    val value: Int // Value for the node
    val depth: Int // Search depth
    val type: Int // Type of value [PV_NODE, CUT_NODE, ALL_NODE]
    val move: GameCommand?

    init {
        this.value = value
        this.depth = depth
        this.type = type
        this.move = move
    }

    companion object {
        const val PV_NODE: Int = 0 // Exact match. For bounds [a,b]: a < x < b
        const val CUT_NODE: Int = 1 // Lower bound - Beta cutoff. Fail high. For bounds [a,b]: x >= b
        const val ALL_NODE: Int = 2 // No move higher than alpha. Fail low. For bounds [a,b]: x <= a
    }
}
