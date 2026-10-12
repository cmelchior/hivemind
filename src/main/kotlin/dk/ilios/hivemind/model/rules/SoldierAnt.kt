package dk.ilios.hivemind.model.rules

import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token

class SoldierAnt : Bug() {
    override fun isFreeToMove(token: Token, board: Board): Boolean = isRoomToSlideAwayOnGround(token, board)

    override fun getTargetHexes(token: Token, board: Board): List<Hex> {
        // Recursively find all squares you can slide to
        val visitedHexes = hashSetOf(checkNotNull(token.hex))
        return recursiveVisit(token, board, mutableListOf<Hex>(), visitedHexes)
    }

    private fun recursiveVisit(
        from: Token,
        board: Board,
        targetHexes: MutableList<Hex>,
        visitedHexes: MutableSet<Hex>
    ): List<Hex> {
        val neighbors = board.getNeighborHexes(from.hex)
        for (targetHex in neighbors) {
            if (visitedHexes.contains(targetHex)) continue
            visitedHexes.add(targetHex)
            if (Rules.instance.canSlideTo(checkNotNull(from.hex), targetHex, board)) {
                targetHexes.add(targetHex)
                val originalHex = checkNotNull(from.hex)
                board.moveToken(from, targetHex.q, targetHex.r)
                recursiveVisit(from, board, targetHexes, visitedHexes)
                board.moveToken(from, originalHex.q, originalHex.r)
            }
        }

        return targetHexes
    }

    override fun canMimic(): Boolean = false

    override fun canMoveOthers(): Boolean = false
}
