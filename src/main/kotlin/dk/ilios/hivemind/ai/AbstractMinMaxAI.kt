package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic
import dk.ilios.hivemind.ai.moves.MoveGenerator
import dk.ilios.hivemind.ai.moves.StandardMoveGenerator
import dk.ilios.hivemind.ai.statistics.AIStatistics
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.rules.Rules

abstract class AbstractMinMaxAI(
    final override val name: String?, protected var heuristic: BoardValueHeuristic, // Search limit in depth
    protected var searchDepth: Int, // Search limit in milliseconds
    protected var maxTimeInMillis: Int
) : HiveAI {
    protected lateinit var state: Game
    final override val aiStats: AIStatistics = AIStatistics(this)

    protected var moveGenerator: MoveGenerator = StandardMoveGenerator()

    protected var start: Long = 0 // Start time in millis when nextMove was called
    protected var maximizingPlayer: Player? = null // Player is who is acting as MAX player in the MinMax algorithm


    protected fun calculateBoardValue(state: Game): Int {
        aiStats.boardEvaluated()
        return heuristic.calculateBoardValue(state)
    }


    /**
     * Heuristic function. Evaluate the value of the board.
     * + is good for the maximizing player, - is good for the minimizing player
     */
    protected fun value(state: Game): Int {
        var result = calculateBoardValue(state)
        if (maximizingPlayer!!.isBlack) {
            result =
                result * -1 // Negative values are good for black in the heuristic function, so if he is maximizing we invert all values
        }
        return result
    }


    protected fun applyMove(command: GameCommand, state: Game): Game {
        command.execute(state)
        return state
    }

    protected fun undoMove(command: GameCommand, state: Game): Game {
        command.undo(state)
        return state
    }

    protected fun isGameOver(state: Game, depth: Int): Boolean {
        val whiteDead: Boolean =
            Rules.instance.isQueenSurrounded(state.whitePlayer, state.board)
        val blackDead: Boolean =
            Rules.instance.isQueenSurrounded(state.blackPlayer, state.board)
        var turnLimit = false
        if (state.turnLimit > 0) {
            val lookAhead = (depth - searchDepth) * -1
            turnLimit = (state.requireActivePlayer().moves + lookAhead) > state.turnLimit
        }
        return whiteDead || blackDead || turnLimit
    }

    /**
     * Standard move generation
     */
    protected fun generateMoves(state: Game, vararg priorityMoves: GameCommand?): List<GameCommand> {
        val moves = moveGenerator.generateMoves(emptyList(), state)
        val result = if (priorityMoves.isEmpty()) {
            moves
        } else {
            moves.toMutableList().apply {
                for (priorityMove in priorityMoves.reversed()) {
                    if (priorityMove != null && remove(priorityMove)) {
                        add(0, priorityMove)
                    }
                }
            }
        }

        aiStats.nodeBranched(result.size)
        return result
    }

    override fun maintainsStandardPosition(): Boolean = true
}
