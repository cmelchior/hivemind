package dk.ilios.hivemind.parser.predicates;

import dk.ilios.hivemind.game.Game;

/**
 * Only analyze games where both players has 1600+ ranking
 */
public class HighRankedGamesPredicate implements Predicate {

    private static final int REQUIRED_RANKING = 1600;

    @Override
    public boolean analyseGame(Game game) {
        return game.getWhitePlayer().getRanking() >= REQUIRED_RANKING && game.getBlackPlayer().getRanking() >= REQUIRED_RANKING;
    }
}
