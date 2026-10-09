package dk.ilios.hivemind.model.rules

import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token

class Grasshopper : Bug() {
    override fun isFreeToMove(token: Token, board: Board): Boolean {
        // A grasshopper can always move, due to the One Hive rule which ensures that there always is a neighbor to jump over
        return true
    }

    override fun getTargetHexes(token: Token, board: Board): MutableList<Hex> {
        // Find neighbor tokens and travel in their direction until we hit a empty space
        val targets: MutableList<Hex> = ArrayList<Hex>()
        val neighbors = board.getNeighborTokens(token.hex)
        for (direction in neighbors) {
            targets.add(getFirstEmptyHexInDirection(checkNotNull(token.hex), checkNotNull(direction.hex), board))
        }

        return targets
    }

    private fun getFirstEmptyHexInDirection(from: Hex, to: Hex, board: Board): Hex {
        val startQ = from.q
        val startR = from.r

        val directionQ = to.q - from.q
        val directionR = to.r - from.r

        var distance = 2 // We know that Distance:1 contains a token, startGame looking from Distance:2.
        while (!board.getHex(startQ + distance * directionQ, startR + distance * directionR).isEmpty) {
            distance++
        }

        return board.getHex(startQ + distance * directionQ, startR + distance * directionR)
    }

    override fun canMimic(): Boolean {
        return false
    }

    override fun canMoveOthers(): Boolean {
        return false
    }
}
