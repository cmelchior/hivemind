package dk.ilios.hivemind.model.rules

import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token

class Beetle : Bug() {
    override fun isFreeToMove(token: Token, board: Board): Boolean {
        // A bettle can always move. Even though some positions can be walled in, the bettle can always just go
        // on top of the wall instead.
        return true
    }

    override fun getTargetHexes(token: Token, board: Board): MutableList<Hex> {
        val neighbors = board.getNeighborHexes(token.hex)
        val result: MutableList<Hex> = ArrayList<Hex>()

        val startingHex = checkNotNull(token.hex)
        for (targetHex in neighbors) {
            val canSlide: Boolean = Rules.instance.canSlideTo(startingHex, targetHex, board)
            val canCrawl: Boolean = Rules.instance.canCrawlUp(startingHex, targetHex, board)
            val stillOneHive: Boolean = Rules.instance.isOneHiveIntact(startingHex, targetHex, board)
            if ((canSlide || canCrawl) && stillOneHive) {
                result.add(targetHex)
            }
        }

        return result
    }

    override fun canMimic(): Boolean {
        return false
    }

    override fun canMoveOthers(): Boolean {
        return false
    }
}
