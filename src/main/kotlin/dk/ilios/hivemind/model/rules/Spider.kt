package dk.ilios.hivemind.model.rules

import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token

class Spider : Bug() {
    override fun isFreeToMove(token: Token, board: Board): Boolean {
        return isRoomToSlideAwayOnGround(token, board)
    }

    override fun getTargetHexes(token: Token, board: Board): MutableList<Hex> {
        // Recursively find all squares you can slide to. Only squares at distance 3 count
        val visitedHexes = ArrayList<Hex>()
        visitedHexes.add(checkNotNull(token.hex))
        return recursiveVisit(token, 0, board, ArrayList<Hex>(), visitedHexes)
    }

    private fun recursiveVisit(
        from: Token,
        distance: Int,
        board: Board,
        targetHexes: MutableList<Hex>,
        visitedHexes: MutableList<Hex>
    ): MutableList<Hex> {
        var distance = distance
        val neighbors = board.getNeighborHexes(from.hex)
        distance++
        for (targetHex in neighbors) {
            if (visitedHexes.contains(targetHex)) continue
            visitedHexes.add(targetHex)
            if (Rules.instance.canSlideTo(checkNotNull(from.hex), targetHex, board)) {
                // Only hexes exactly 3 fullfill the spiders movement rules

                if (distance == 3) {
                    targetHexes.add(targetHex)
                }

                // Only need to look for more targets if not at maximum range
                if (distance < 3) {
                    val originalHex = checkNotNull(from.hex)
                    board.moveToken(from, targetHex.q, targetHex.r)
                    recursiveVisit(from, distance, board, targetHexes, visitedHexes)
                    board.moveToken(from, originalHex.q, originalHex.r)
                }
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
