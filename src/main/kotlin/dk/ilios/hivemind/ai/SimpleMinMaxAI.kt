package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Board
import kotlin.random.Random

/**
 * AI that implements standard MinMax tree search.
 *
 */
class SimpleMinMaxAI(name: String?, heuristic: BoardValueHeuristic, depth: Int, maxTimeInMillis: Int) :
    AbstractMinMaxAI(name, heuristic, depth, maxTimeInMillis) {
    private val random = Random.Default

    override fun copy(): HiveAI = SimpleMinMaxAI(name, heuristic, searchDepth, maxTimeInMillis)

    override fun nextMove(state: Game, board: Board): GameCommand {
        maximizingPlayer = state.requireActivePlayer()
        start = System.currentTimeMillis()

        // Minimax traversal of game tree
        // Ply 1
        val moves = generateMoves(state)
        var bestValue = Int.MIN_VALUE
        var bestMove: GameCommand = GameCommand.PASS

        for (move in moves) {
            applyMove(move, state)
            val value = minimax(state, searchDepth - 1, false)
            if (value > bestValue || value == bestValue && random.nextBoolean()) {
                bestValue = value
                bestMove = move
            }
            undoMove(move, state)
        }

        return bestMove
    }

    /**
     * Minimax traversal. Returns a number between Integer.MIN and Integer.MAX
     * + means current player is winning, - he is loosing
     */
    private fun minimax(state: Game, depth: Int, maximizingPlayer: Boolean): Int {
        if (isGameOver(state, depth) || depth <= 0 || System.currentTimeMillis() - start > maxTimeInMillis) {
            // Positive values are good for the maximizing player, negative values for minimizing player
            return value(state)
        } else {
            val moves = generateMoves(state)
            var bestValue: Int = if (maximizingPlayer) HiveAI.MIN else HiveAI.MAX

            for (move in moves) {
                // Update game state and continue traversel
                applyMove(move, state)
                val value = minimax(state, depth - 1, !maximizingPlayer)
                if (maximizingPlayer) {
                    if (value > bestValue || (value == bestValue && random.nextBoolean())) {
                        bestValue = value
                    }
                } else {
                    if (value < bestValue || (value == bestValue && random.nextBoolean())) {
                        bestValue = value
                    }
                }
                undoMove(move, state)
            }

            return bestValue
        }
    }
}
