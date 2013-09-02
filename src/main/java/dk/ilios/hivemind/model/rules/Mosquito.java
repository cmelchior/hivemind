package dk.ilios.hivemind.model.rules;

import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Token;

import java.util.Collections;
import java.util.List;

public class Mosquito extends Bug {

    @Override
    public boolean isFreeToMove(Token token, Board board) {
        return false;  // A mosquito never moves except by mimicing other bugs.
    }

    @Override
    public List<Hex> getTargetHexes(Token token, Board board) {
        return Collections.emptyList(); // A mosquito can only move my mimicing other bugs.

    }

    @Override
    public boolean canMimic() {
        return true;
    }

    @Override
    public boolean canMoveOthers() {
        return false;
    }

}
