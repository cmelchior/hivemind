package dk.ilios.hivemind.ai.heuristics;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.model.Player;

/**
 * Interface used to describe heuristic functions for minmax.
 */
public interface BoardValueHeuristic {
    /**
     * Calculate board value for the given state state.
     * Value returned is between Integer.MIN_VALUE and Integer.MAX_VALUE.
     *
     * Positive numbers indicate that white is winning, negative numbers that black is.
     */
    public int calculateBoardValue(Game state);
}
