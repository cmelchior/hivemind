package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.statistics.AIStatistics;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Board;

/**
 * Interface for Hive AI implementations
 */
public interface HiveAI {

    public final static int MAX = Integer.MAX_VALUE;        // Maximum value when evaluating a hive board.
    public final static int MIN = Integer.MIN_VALUE + 1;    // Minimum value when evaluating a hive board.

    /**
     * Return the next game move which the AI considers the best depending on the state of the game.
     */
    public GameCommand nextMove(Game state, Board board);

    /**
     * Return metrics from this AI
     */
    public AIStatistics getAiStats();

    /**
     * Return the name of the AI
     */
    public String getName();

    /**
     * Copy the AI parameters to create a new version with the same name.
     */
    public HiveAI copy();

    /**
     * Returns true if the HiveAI uses zobrist keys and Standard Position. If not, no need to use computational power to maintain Standard
     * Position if it is not used.
     */
    public boolean maintainsStandardPosition();
}
