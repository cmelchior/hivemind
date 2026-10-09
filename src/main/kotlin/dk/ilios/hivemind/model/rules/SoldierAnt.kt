package dk.ilios.hivemind.model.rules

import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token

class SoldierAnt : Bug() {
    override fun isFreeToMove(token: Token, board: Board): Boolean {
        return isRoomToSlideAwayOnGround(token, board)
    }

    override fun getTargetHexes(token: Token, board: Board): MutableList<Hex> {
        // Recursively find all squares you can slide to
        val visitedHexes = ArrayList<Hex>()
        visitedHexes.add(checkNotNull(token.hex))
        return recursiveVisit(token, board, ArrayList<Hex>(), visitedHexes)
    }

    private fun recursiveVisit(
        from: Token,
        board: Board,
        targetHexes: MutableList<Hex>,
        visitedHexes: MutableList<Hex>
    ): MutableList<Hex> {
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

    override fun canMimic(): Boolean {
        return false
    }

    override fun canMoveOthers(): Boolean {
        return false
    }
}
