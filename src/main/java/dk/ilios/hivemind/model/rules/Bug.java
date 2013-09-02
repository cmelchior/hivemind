package dk.ilios.hivemind.model.rules;

import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Token;

import java.util.List;
import java.util.Set;

public abstract class Bug {

    /**
     * Returns true if the token can move.
     * INVARIANT: It is already assumed that moving the token will full the "One Hive" rule.
     *
     * @param token Token to move
     * @param board Board
     * @return True if the token can move
     */
    public abstract boolean isFreeToMove(Token token, Board board);

    /**
     * Returns the squares the token can move to.
     *
     * @param token
     * @param board
     * @return Legal hexes the token can move to
     */
    public abstract List<Hex> getTargetHexes(Token token, Board board);


    /**
     * Returns true if the given bug type has the "Mimic movement" ability, eg. the Mosquito
     */
    public abstract boolean canMimic();


    /**
     * Returns true if the given bug type has the ability to move other bugs around, eg. the PillBug
     * @return
     */
    public abstract boolean canMoveOthers();


    /**
     * Returns true if it is possible to slide away from the given position.
     * This requires two empty spaces next to each other.
     *
     * INVARIANT: Only works at the ground level.
     *
     *
     * @param token
     * @param board
     * @return True if token can slide away.
     */
    protected boolean isRoomToSlideAwayOnGround(Token token, Board board) {
        List<Hex> hexes = board.getNeighborHexes(token.getHex());

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

}
