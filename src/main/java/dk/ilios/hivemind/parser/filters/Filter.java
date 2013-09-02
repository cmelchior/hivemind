package dk.ilios.hivemind.parser.filters;

import dk.ilios.hivemind.game.Game;

/**
 * Interface for predicates used to determine wether or not to analyze a game.
 */
public interface Filter {
    public boolean analyseGame(String type, Game game);
}
