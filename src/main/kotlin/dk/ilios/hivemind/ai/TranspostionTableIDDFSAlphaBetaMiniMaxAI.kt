package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic
import dk.ilios.hivemind.ai.transpositiontable.TranspositionTable
import dk.ilios.hivemind.ai.transpositiontable.TranspositionTableEntry
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Board
import java.util.*

/**
 * AI that implements Minimax tree search algorithm with Alpha-Beta prunning and Iterative Deepening Depth-First Search.
 * Backed by a transposition table.
 *
 * @see http://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
 *
 * @see http://en.wikipedia.org/wiki/Iterative_deepening
 *
 * @see http://en.wikipedia.org/wiki/Transposition_table
 *
 * Other things to consider:
 * - Move reordering
 * - Killer heuristic
 * - http://chessprogramming.wikispaces.com/Quiescence+Search
 * - With move reordering in place, consider NegaScout: http://en.wikipedia.org/wiki/Negascout
 * - Consider http://en.wikipedia.org/wiki/MTD-f
 */
class TranspostionTableIDDFSAlphaBetaMiniMaxAI(
    name: String?,
    heuristicFunction: BoardValueHeuristic,
    depth: Int,
    maxTimeInMillis: Int
) : AbstractMinMaxAI(name, heuristicFunction, depth, maxTimeInMillis) {
    private val random = Random()
    private val table = TranspositionTable()

    override fun copy(): HiveAI {
        return TranspostionTableIDDFSAlphaBetaMiniMaxAI(name, heuristic, searchDepth, maxTimeInMillis)
    }

    override fun nextMove(state: Game, board: Board): GameCommand? {
        start = System.currentTimeMillis()
        maximizingPlayer = state.requireActivePlayer()

        // Iterate depths, effectively a breath-first search, where top nodes get visited multiple times
        var depth = 0
        var bestValue = Int.MIN_VALUE
        var bestCommand: GameCommand? = null

        var result = arrayOfNulls<Any>(2)
        while (depth <= searchDepth && System.currentTimeMillis() - start < maxTimeInMillis) {
            result = runMinMax(state, depth, result)
            val `val` = result[0] as Int
            if (`val` > bestValue || `val` == bestValue && random.nextBoolean()) {
                bestValue = `val`
                bestCommand = result[1] as GameCommand?
                if (bestValue == HiveAI.MAX) {
                    return bestCommand // Game winning move
                }
            }

            depth++
        }

        return bestCommand // 2nd best move
    }

    private fun runMinMax(state: Game, searchDepth: Int, result: Array<Any?>): Array<Any?> {
        // Minimax traversal of game tree

        val moves = generateMoves(state)
        var bestValue = Int.MIN_VALUE
        var bestMove: GameCommand = GameCommand.PASS

        for (move in moves) {
            // Update game state and continue traversel
            applyMove(move, state)
            val value = alphabeta(state, searchDepth - 1, bestValue, HiveAI.MAX, false)
            if (value > bestValue || value == bestValue && random.nextBoolean()) {
                bestValue = value
                bestMove = move
            }
            undoMove(move, state)
        }

        result[0] = bestValue
        result[1] = bestMove
        return result
    }

    private fun alphabeta(state: Game, depth: Int, alpha: Int, beta: Int, maximizingPlayer: Boolean): Int {
        var alpha = alpha
        var beta = beta
        val originalAlpha = alpha
        val originalBeta = beta
        var bestMove: GameCommand? = null

        // Check transposition tsable and adjust values if needed or return result if possible
        val zobristKey = state.zobristKey
        val entry = table.getResult(zobristKey)
        if (entry != null && entry.depth >= depth) {
            aiStats.cacheHit()
            bestMove = entry.move
            if (entry.type == TranspositionTableEntry.PV_NODE) {
                return entry.value
            } else if (entry.type == TranspositionTableEntry.CUT_NODE && entry.value > alpha) {
                alpha = entry.value
            } else if (entry.type == TranspositionTableEntry.ALL_NODE && entry.value < beta) {
                beta = entry.value
            }

            if (alpha >= beta) {
                return entry.value // Lowerbound is better than upper bound
            }
        }

        // Run algorithm as usual
        var value: Int
        if (isGameOver(state, depth) || depth <= 0 || System.currentTimeMillis() - start > maxTimeInMillis) {
            value = value(state)
        } else {
            val moves = generateMoves(state, bestMove)
            var moveAnalyzed = 0

            if (maximizingPlayer) {
                for (move in moves) {
                    moveAnalyzed++
                    bestMove = move
                    applyMove(move, state)
                    value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer)
                    if (value > alpha) {
                        alpha = value
                    }
                    undoMove(move, state)

                    // Beta cut-off
                    if (beta <= alpha) {
                        aiStats.cutOffAfter(moveAnalyzed)
                        break
                    }
                }

                value = alpha
            } else {
                for (move in moves) {
                    moveAnalyzed++
                    bestMove = move
                    applyMove(move, state)
                    value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer)
                    if (value < beta) {
                        aiStats.cutOffAfter(moveAnalyzed)
                        beta = value
                    }
                    undoMove(move, state)

                    // Alpha cut-off
                    if (beta <= alpha) {
                        break
                    }
                }

                value = beta
            }
        }

        // Update transposition table
        if (value <= originalAlpha) {
            table.addResult(zobristKey, value, depth, TranspositionTableEntry.CUT_NODE, bestMove)
        } else if (value >= originalBeta) {
            table.addResult(zobristKey, value, depth, TranspositionTableEntry.ALL_NODE, bestMove)
        } else {
            table.addResult(zobristKey, value, depth, TranspositionTableEntry.PV_NODE, bestMove)
        }

        return value
    }

    override fun maintainsStandardPosition(): Boolean {
        return true
    }
}
