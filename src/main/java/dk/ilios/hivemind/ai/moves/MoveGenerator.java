package dk.ilios.hivemind.ai.moves;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for move generators used by HiveAI's.
 */
public abstract class MoveGenerator {

    public abstract List<GameCommand> generateMoves(List<GameCommand> initialList, Game state);

    protected GameCommand createGameCommand(Token token, Hex hex) {
        if (token.getHex() == null) {
            return new GameCommand(Hex.SUPPLY, Hex.SUPPLY, hex.getQ(), hex.getR(), token, false);
        } else {
            return new GameCommand(token.getHex().getQ(), token.getHex().getR(), hex.getQ(), hex.getR(), token, false);
        }
    }

}
