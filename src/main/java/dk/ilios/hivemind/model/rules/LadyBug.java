package dk.ilios.hivemind.model.rules;


import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Token;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LadyBug extends Bug {

    @Override
    public boolean isFreeToMove(Token token, Board board) {
        // As a ladybug starts by crawling on top of the hive, she can always move.
        return true;
    }

    @Override
    public List<Hex> getTargetHexes(Token token, Board board) {
        // Recursively find all squares you can slide to. Only squares at distance 3 count
        ArrayList<Hex> visitedHexes = new ArrayList<Hex>();
        visitedHexes.add(token.getHex());
        Set<Hex> result = recursiveVisit(token, 1, board, new HashSet<Hex>());
        result.remove(token.getHex()); // Remove starting hex
        return new ArrayList<Hex>(result);
    }

    private Set<Hex> recursiveVisit(Token from, int distance, Board board, Set<Hex> targetHexes) {

        // Crawl up on hive
        if (distance == 1) {
            List<Token> neighbors = board.getNeighborTokens(from.getHex());
            distance++;
            for (Token neighbor : neighbors) {
                Hex originalHex = from.getHex();
                Hex toHex = neighbor.getHex();
                if (Rules.getInstance().canCrawlUp(from.getHex(), toHex, board)) {
                    board.moveToken(from, toHex.getQ(), toHex.getR());
                    recursiveVisit(from, distance, board, targetHexes);
                    board.moveToken(from, originalHex.getQ(), originalHex.getR());
                }
            }

        // Move around hive
        } else if (distance == 2) {
            List<Token> neighbors = board.getNeighborTokens(from.getHex());
            distance++;
            for (Token neighbor : neighbors) {
                Hex originalHex = from.getHex();
                Hex toHex = neighbor.getHex();
                if (Rules.getInstance().canSlideTo(originalHex, toHex, board) || Rules.getInstance().canCrawlUp(originalHex, toHex, board)) {
                    board.moveToken(from, toHex.getQ(), toHex.getR());
                    recursiveVisit(from, distance, board, targetHexes);
                    board.moveToken(from, originalHex.getQ(), originalHex.getR());
                }
            }

        // Move down from hive
        } else if (distance == 3) {
            List<Hex> neighbors = board.getNeighborHexes(from.getHex());
            for (Hex neighbor : neighbors) {
                if (neighbor.getHeight() > 0) continue;
                if (Rules.getInstance().canCrawlDown(from.getHex(), neighbor, board)) {
                    targetHexes.add(neighbor);
                }
            }

            return targetHexes;
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
