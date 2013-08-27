package dk.ilios.hivemind.model.rules;

import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Token;

import java.util.ArrayList;
import java.util.List;

public class SoldierAnt extends Bug {

    @Override
    public boolean isFreeToMove(Token token, Board board) {
        return isRoomToSlideAwayOnGround(token, board);
    }

    @Override
    public List<Hex> getTargetHexes(Token token, Board board) {
        // Recursively find all squares you can slide to
        ArrayList<Hex> visitedHexes = new ArrayList<Hex>();
        visitedHexes.add(token.getHex());
        return recursiveVisit(token, board, new ArrayList<Hex>(), visitedHexes);
    }

    private List<Hex> recursiveVisit(Token from, Board board, List<Hex> targetHexes, List<Hex> visitedHexes) {
        List<Hex> neighbors = board.getNeighborHexes(from.getHex());
        for (Hex targetHex : neighbors) {
            if (visitedHexes.contains(targetHex)) continue;
            visitedHexes.add(targetHex);
            if (Rules.getInstance().canSlideTo(from.getHex(), targetHex, board)) {
                targetHexes.add(targetHex);
                Hex originalHex = from.getHex();
                board.moveToken(from, targetHex.getQ(), targetHex.getR());
                recursiveVisit(from, board, targetHexes, visitedHexes);
                board.moveToken(from, originalHex.getQ(), originalHex.getR());
            }
        }

        return targetHexes;
    }

    @Override
    public boolean canMimic() {
        return false;
    }

    @Override
    public boolean canMoveOthers() {
        return false;
    }
}
