package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.statistics.AIStatistics
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Board

/**
 * Interface for Hive AI implementations
 */
interface HiveAI {
    /**
     * Return the next game move which the AI considers the best depending on the state of the game.
     */
    fun nextMove(state: Game, board: Board): GameCommand?

    /**
     * Return metrics from this AI
     */
    val aiStats: AIStatistics

    /**
     * Return the name of the AI
     */
    val name: String?

    /**
     * Copy the AI parameters to create a new version with the same name.
     */
    fun copy(): HiveAI

    /**
     * Returns true if the HiveAI uses zobrist keys and Standard Position. If not, no need to use computational power to maintain Standard
     * Position if it is not used.
     */
    fun maintainsStandardPosition(): Boolean

    companion object {
        val MAX: Int = Int.MAX_VALUE // Maximum value when evaluating a hive board.
        val MIN: Int = Int.MIN_VALUE + 1 // Minimum value when evaluating a hive board.
    }
}
