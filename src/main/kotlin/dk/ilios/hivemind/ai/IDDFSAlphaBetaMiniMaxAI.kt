package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Board
import java.util.*

/**
 * AI that implements Minimax tree search algorithm with Alpha-Beta prunning and Iterative Deepening Depth-First Search.
 *
 * @see http://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
 *
 * @see http://en.wikipedia.org/wiki/Iterative_deepening
 *
 * Other things to consider:
 * - Transposition table
 * - Move reordering
 * - Killer heuristic
 * - http://chessprogramming.wikispaces.com/Quiescence+Search
 * - With move reordering in place, consider NegaScout: http://en.wikipedia.org/wiki/Negascout
 * - Consider http://en.wikipedia.org/wiki/MTD-f
 */
class IDDFSAlphaBetaMiniMaxAI(
    name: String?,
    heuristicFunction: BoardValueHeuristic,
    depth: Int,
    maxTimeInMillis: Int
) : AbstractMinMaxAI(name, heuristicFunction, depth, maxTimeInMillis) {
    private val random = Random()

    override fun copy(): HiveAI {
        return IDDFSAlphaBetaMiniMaxAI(name, heuristic, searchDepth, maxTimeInMillis)
    }


    override fun nextMove(state: Game, board: Board): GameCommand? {
        start = System.currentTimeMillis()
        maximizingPlayer = state.requireActivePlayer()

        // Iterate depths, effectively a breath-first search, where top nodes get visited multiple times
        var depth = 0
        var bestValue = Int.MIN_VALUE
        var bestCommand: GameCommand? = GameCommand.PASS

        var result = arrayOfNulls<Any>(2)
        while (depth <= searchDepth && System.currentTimeMillis() - start < maxTimeInMillis) {
            result = runMinMax(state, depth, result)
            val `val` = result[0] as Int
            if (`val` > bestValue) {
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
                if (bestValue == HiveAI.MAX) break
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
        if (isGameOver(state, depth) || depth <= 0 || System.currentTimeMillis() - start > maxTimeInMillis) {
            return value(state)
        } else {
            val moves = generateMoves(state)

            if (maximizingPlayer) {
                for (move in moves) {
                    applyMove(move, state)
                    val value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer)
                    if (value > alpha) {
                        alpha = value
                    }
                    undoMove(move, state)

                    // Beta cut-off
                    if (beta <= alpha) {
                        break
                    }
                }
                return alpha
            } else {
                for (move in moves) {
                    applyMove(move, state)
                    val value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer)
                    if (value < beta) {
                        beta = value
                    }
                    undoMove(move, state)

                    // Alpha cut-off
                    if (beta <= alpha) {
                        break
                    }
                }
                return beta
            }
        }
    }
}
