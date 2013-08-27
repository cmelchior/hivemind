package dk.ilios.hivemind.model.rules;

import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Token;

import java.util.List;

public class PillBug extends Bug {
    @Override
    public boolean isFreeToMove(Token token, Board board) {
        return false;
    }

    @Override
    public List<Hex> getTargetHexes(Token token, Board board) {
        return null;
    }

    @Override
    public boolean canMimic() {
        return false;
    }

    @Override
    public boolean canMoveOthers() {
        return true;
    }

}
