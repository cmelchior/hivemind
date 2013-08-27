package dk.ilios.hivemind.model.rules;

import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Token;

import java.util.ArrayList;
import java.util.List;

public class Grasshopper extends Bug {
    @Override
    public boolean isFreeToMove(Token token, Board board) {
        // A grasshopper can always move, due to the One Hive rule which ensures that there always is a neighbor to jump over
        return true;
    }

    @Override
    public List<Hex> getTargetHexes(Token token, Board board) {
        // Find neighbor tokens and travel in their direction until we hit a empty space
        List<Hex> targets = new ArrayList<Hex>();
        List<Token> neighbors = board.getNeighborTokens(token.getHex());
        for (Token direction : neighbors) {
            targets.add(getFirstEmptyHexInDirection(token.getHex(), direction.getHex(), board));
        }

        return targets;
    }

    private Hex getFirstEmptyHexInDirection(Hex from, Hex to, Board board) {
        int startQ = from.getQ();
        int startR = from.getR();

        int directionQ = to.getQ() - from.getQ();
        int directionR = to.getR() - from.getR();

        int distance = 2; // We know that Distance:1 contains a token, startGame looking from Distance:2.
        while (!board.getHex(startQ + distance*directionQ, startR + distance*directionR).isEmpty()) {
            distance++;
        }

        return board.getHex(startQ + distance*directionQ, startR + distance*directionR);
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
