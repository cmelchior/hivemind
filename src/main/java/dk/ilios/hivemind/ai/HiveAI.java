package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.statistics.AIStatistics;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Board;

/**
 * Interface for Hive AI implementations
 */
public interface HiveAI extends Cloneable {

    /**
     * Return the next game move which the AI considers the best depending on the state of the game.
     */
    public GameCommand nextMove(Game state, Board board);

    /**
     * Return metrics from this AI
     * @return
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
}
