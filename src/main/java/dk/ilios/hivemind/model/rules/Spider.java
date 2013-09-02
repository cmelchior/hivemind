package dk.ilios.hivemind.model.rules;

import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Token;

import java.util.ArrayList;
import java.util.List;

public class Spider extends Bug {

    @Override
    public boolean isFreeToMove(Token token, Board board) {
        return isRoomToSlideAwayOnGround(token, board);
    }

    @Override
    public List<Hex> getTargetHexes(Token token, Board board) {
        // Recursively find all squares you can slide to. Only squares at distance 3 count
        ArrayList<Hex> visitedHexes = new ArrayList<Hex>();
        visitedHexes.add(token.getHex());
        return recursiveVisit(token, 0, board, new ArrayList<Hex>(), visitedHexes);
    }

    private List<Hex> recursiveVisit(Token from, int distance, Board board, List<Hex> targetHexes, List<Hex> visitedHexes) {
        List<Hex> neighbors = board.getNeighborHexes(from.getHex());
        distance++;
        for (Hex targetHex : neighbors) {
            if (visitedHexes.contains(targetHex)) continue;
            visitedHexes.add(targetHex);
            if (Rules.getInstance().canSlideTo(from.getHex(), targetHex, board)) {

                // Only hexes exactly 3 fullfill the spiders movement rules
                if (distance == 3) {
                    targetHexes.add(targetHex);
                }

                // Only need to look for more targets if not at maximum range
                if (distance < 3) {
                    Hex originalHex = from.getHex();
                    board.moveToken(from, targetHex.getQ(), targetHex.getR());
                    recursiveVisit(from, distance, board, targetHexes, visitedHexes);
                    board.moveToken(from, originalHex.getQ(), originalHex.getR());
                }
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
