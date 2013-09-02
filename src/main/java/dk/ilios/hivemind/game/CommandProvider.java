package dk.ilios.hivemind.game;

import dk.ilios.hivemind.model.Board;

/**
 * Interface for objects that can create new game moves
 */
public interface CommandProvider {
    /**
     * Make the next move based on the current game state.
     *
     * @param currentState The current game state.
     * @param board Easy reference to the board state.
     * @return The next legal move
     */
    public GameCommand getCommand(Game currentState, Board board);
}
