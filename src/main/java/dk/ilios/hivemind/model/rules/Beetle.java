package dk.ilios.hivemind.model.rules;

import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Token;

import java.util.ArrayList;
import java.util.List;

public class Beetle extends Bug {

    @Override
    public boolean isFreeToMove(Token token, Board board) {
        // A bettle can always move. Even though some positions can be walled in, the bettle can always just go
        // on top of the wall instead.
        return true;
    }

    @Override
    public List<Hex> getTargetHexes(Token token, Board board) {
        List<Hex> neighbors = board.getNeighborHexes(token.getHex());
        List<Hex> result = new ArrayList<Hex>();

        Hex startingHex = token.getHex();
        for (Hex targetHex : neighbors) {
            boolean canSlide = Rules.getInstance().canSlideTo(startingHex, targetHex, board);
            boolean canCrawl = Rules.getInstance().canCrawlUp(startingHex, targetHex, board);
            boolean stillOneHive = Rules.getInstance().isOneHiveIntact(startingHex, targetHex, board);
            if ((canSlide || canCrawl) && stillOneHive) {
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
