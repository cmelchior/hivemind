package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.statistics.AIStatistics;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Board;

/**
 * Interface for Hive AI implementations
 */
public interface HiveAI {
    public GameCommand nextMove(Game state, Board board);
    public AIStatistics getAiStats();
    public String getName();
}
