package dk.ilios.hivemind.parser.predicates;

import dk.ilios.hivemind.game.Game;

/**
 * Interface for predicates used to determine wether or not to analyze a game.
 */
public interface Predicate {
    public boolean analyseGame(Game game);
}
