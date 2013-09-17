package dk.ilios.hivemind.parser.filters;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.BugType;

/**
 * Don't analyze games that start with the queen for either player
 */
public class QueenAsStartingPieceFilter implements Filter {
    @Override
    public boolean analyseGame(String type, Game game) {
        GameCommand whiteTurn1 = game.getMove(game.getWhitePlayer(), 1);
        GameCommand blackTurn1 = game.getMove(game.getBlackPlayer(), 1);
        if (whiteTurn1 != null && blackTurn1 != null) {
            return !(whiteTurn1.getToken().getOriginalType() == BugType.QUEEN_BEE
                    || blackTurn1.getToken().getOriginalType() == BugType.QUEEN_BEE);
        } else {
            return false;
        }
    }
}
