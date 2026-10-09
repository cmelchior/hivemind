package dk.ilios.hivemind.model.rules

import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token

abstract class Bug {
    /**
     * Returns true if the token can move.
     * INVARIANT: It is already assumed that moving the token will full the "One Hive" rule.
     *
     * @param token Token to move
     * @param board Board
     * @return True if the token can move
     */
    abstract fun isFreeToMove(token: Token, board: Board): Boolean

    /**
     * Returns the squares the token can move to.
     *
     * @return Legal hexes the token can move to
     */
    abstract fun getTargetHexes(token: Token, board: Board): MutableList<Hex>?


    /**
     * Returns true if the given bug type has the "Mimic movement" ability, eg. the Mosquito
     */
    abstract fun canMimic(): Boolean


    /**
     * Returns true if the given bug type has the ability to move other bugs around, eg. the PillBug
     */
    abstract fun canMoveOthers(): Boolean


    /**
     * Returns true if it is possible to slide away from the given position.
     * This requires two empty spaces next to each other.
     *
     * INVARIANT: Only works at the ground level.
     *
     * @return True if token can slide away.
     */
    protected fun isRoomToSlideAwayOnGround(token: Token, board: Board): Boolean {
        val hexes = board.getNeighborHexes(token.hex) // Clockwise, starting from top

        // Fullfill the "Freedom to move" rule if two hexes next to each other is empty
        // Go a full round + 1 to check for empty spaces across list start/end.
        var free = 0
        for (i in 0..<hexes.size + 1) {
            val hex = hexes.get(i % hexes.size)
            if (hex.isEmpty) {
                free++
                if (free == 2) {
                    return true
                }
            } else {
                free = 0
            }
        }

        return false
    }
}
