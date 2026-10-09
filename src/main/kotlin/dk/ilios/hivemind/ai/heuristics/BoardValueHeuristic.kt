package dk.ilios.hivemind.ai.heuristics

import dk.ilios.hivemind.game.Game

/**
 * Interface used to describe heuristic functions for minmax.
 */
interface BoardValueHeuristic {
    /**
     * Calculate board value for the given state state.
     * Value returned is between HiveAI.MIN and HiveAI.MAX.
     *
     * Positive numbers indicate that white is winning, negative numbers that black is.
     */
    fun calculateBoardValue(state: Game): Int
}
