package dk.ilios.hivemind.model.rules;

import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Token;

import java.util.ArrayList;
import java.util.List;

public class QueenBee extends Bug {

    @Override
    public boolean isFreeToMove(Token token, Board board) {
        List<Hex> hexes = board.getNeighborHexes(token.getHex());

        // Calling this is assumed to already fullfill "One Hive".
        // Fullfill the "Freedom to move" rule if two hexes next to each other is empty
        int free = 0;
        for (Hex hex : hexes) {
            if (hex.isEmpty()) {
                free++;
                if (free == 2) {
                    return true;
                }
            } else {
                free = Math.max(0, free - 1);
            }
        }

        return false;
    }

    @Override
    public List<Hex> getTargetHexes(Token token, Board board) {
        List<Hex> neighbors = board.getNeighborHexes(token.getHex());
        List<Hex> result = new ArrayList<Hex>();

        Hex startingHex = token.getHex();
        for (Hex targetHex : neighbors) {
            boolean canSlide = Rules.getInstance().canSlideTo(startingHex, targetHex, board);
            boolean stillOneHive = Rules.getInstance().isOneHiveIntact(startingHex, targetHex, board);
            if (canSlide && stillOneHive) {
                result.add(targetHex);
            }
        }

        return result;
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
