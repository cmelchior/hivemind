package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Board
import java.util.*

/**
 * AI that implements Minimax tree search algorithm with Alpha-Beta prunning.
 *
 */
class AlphaBetaMiniMaxAI(name: String?, heuristicFunction: BoardValueHeuristic, depth: Int, maxTimeInMillis: Int) :
    AbstractMinMaxAI(name, heuristicFunction, depth, maxTimeInMillis) {
    private val random = Random()

    override fun copy(): HiveAI {
        return AlphaBetaMiniMaxAI(name, heuristic, searchDepth, maxTimeInMillis)
    }

    override fun nextMove(state: Game, board: Board): GameCommand {
        maximizingPlayer = state.requireActivePlayer()
        start = System.currentTimeMillis()

        // Minimax traversal of game tree
        val moves = generateMoves(state)
        var bestValue: Int = HiveAI.MIN
        var bestMove: GameCommand = GameCommand.PASS

        for (move in moves) {
            // Update game state and continue traversel
            applyMove(move, state)
            val value = alphabeta(state, searchDepth - 1, bestValue, HiveAI.MAX, false)
            if (value > bestValue) {
                bestValue = value
                bestMove = move
                if (bestValue == HiveAI.MAX) break
            }
            undoMove(move, state)
        }

        return bestMove
    }

    private fun alphabeta(state: Game, depth: Int, alpha: Int, beta: Int, maximizingPlayer: Boolean): Int {
        var alpha = alpha
        var beta = beta
        val timeout = System.currentTimeMillis() - start > maxTimeInMillis
        val maxDepthReached = depth <= 0

        if (isGameOver(state, depth) || maxDepthReached || timeout) {
            return value(state)
        } else {
            val moves = generateMoves(state)
            var moveEvaluated = 0
            if (maximizingPlayer) {
                for (move in moves) {
                    applyMove(move, state)
                    moveEvaluated++
                    val value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer)
                    if (value > alpha) {
                        alpha = value
                    }
                    undoMove(move, state)

                    // Beta cut-off
                    if (beta <= alpha) {
                        aiStats.cutOffAfter(moveEvaluated)
                        break
                    }
                }

                return alpha
            } else {
                for (move in moves) {
                    applyMove(move, state)
                    moveEvaluated++
                    val value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer)
                    if (value < beta) {
                        beta = value
                    }
                    undoMove(move, state)

                    // Alpha cut-off
                    if (beta <= alpha) {
                        aiStats.cutOffAfter(moveEvaluated)
                        break
                    }
                }

                return beta
            }
        }
    }
}
