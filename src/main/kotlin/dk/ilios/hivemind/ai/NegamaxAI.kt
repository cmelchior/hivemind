package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Board
import kotlin.math.max

/**
 * Hive AI using Negamax with Alpha/beta prunning.
 *
 * @see http://en.wikipedia.org/wiki/Negamax
 */
class NegamaxAI(name: String?, heuristicFunction: BoardValueHeuristic, depth: Int, maxTimeInMillis: Int) :
    AbstractMinMaxAI(name, heuristicFunction, depth, maxTimeInMillis) {
    override fun copy(): HiveAI {
        return NegamaxAI(name, heuristic, searchDepth, maxTimeInMillis)
    }

    override fun nextMove(state: Game, board: Board): GameCommand {
        maximizingPlayer = state.requireActivePlayer()
        start = System.currentTimeMillis()

        // Player A = white, Player B = black
        if (state.requireActivePlayer().isWhitePlayer) {
            return negamaxRoot(state, searchDepth, HiveAI.MIN, HiveAI.MAX, 1)
        } else {
            return negamaxRoot(state, searchDepth, HiveAI.MIN, HiveAI.MAX, -1)
        }
    }

    private fun negamaxRoot(state: Game, depth: Int, alpha: Int, beta: Int, color: Int): GameCommand {
        var alpha = alpha
        val moves = generateMoves(state)
        var bestValue: Int = HiveAI.MIN
        var bestMove: GameCommand = GameCommand.PASS

        val i = 0
        for (move in moves) {
            applyMove(move, state)
            val value = -negamax(state, searchDepth - 1, -beta, -alpha, -color)
            alpha = max(alpha, value)
            if (value > bestValue) {
                bestValue = value
                bestMove = move
            }
            undoMove(move, state)
        }

        return bestMove
    }

    private fun negamax(state: Game, depth: Int, alpha: Int, beta: Int, color: Int): Int {
        var alpha = alpha
        if (isGameOver(state, depth) || depth <= 0 || System.currentTimeMillis() - start > maxTimeInMillis) {
            return color * calculateBoardValue(state)
        } else {
            var bestValue: Int = HiveAI.MIN
            val moves = generateMoves(state)
            for (move in moves) {
                applyMove(move, state)
                val value = -negamax(state, depth - 1, -beta, -alpha, -color)
                undoMove(move, state)
                bestValue = max(bestValue, value)
                alpha = max(alpha, value)
                if (alpha >= beta) break
            }
            return bestValue
        }
    }
}
