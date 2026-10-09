package dk.ilios.hivemind.ai.transpositiontable

import dk.ilios.hivemind.game.GameCommand

/**
 * Implementation of a Transposition table for an AlphaBetaMinimax algorithm
 *
 * @see http://en.wikipedia.org/wiki/Transposition_table
 *
 * @see http://www.gamedev.net/topic/503234-transposition-table-question/
 *
 * @see http://homepages.cwi.nl/~paulk/theses/Carolus.pdf
 *
 * @see https://groups.google.com/forum/.!topic/rec.games.chess.computer/p8GbiiLjp0o
 */
class TranspositionTable {
    private val table: MutableMap<Long?, TranspositionTableEntry?> = HashMap<Long?, TranspositionTableEntry?>()

    fun addResult(zobristKey: Long, value: Int, depth: Int, valueType: Int, bestMove: GameCommand?) {
        val existingEntry = table.get(zobristKey)
        if (existingEntry == null || depth >= existingEntry.depth) {
            table.put(zobristKey, TranspositionTableEntry(value, depth, valueType, bestMove))
        }
    }

    /**
     * Return the already calculated value for a zobrist key or null if no key exists.
     */
    fun getResult(zobristKey: Long): TranspositionTableEntry? {
        return table.get(zobristKey)
    }
}
