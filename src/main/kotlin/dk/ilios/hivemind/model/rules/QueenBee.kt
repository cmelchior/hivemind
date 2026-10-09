package dk.ilios.hivemind.model.rules

import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token
import kotlin.math.max

class QueenBee : Bug() {
    override fun isFreeToMove(token: Token, board: Board): Boolean {
        val hexes = board.getNeighborHexes(token.hex)

        // Calling this is assumed to already fullfill "One Hive".
        // Fullfill the "Freedom to move" rule if two hexes next to each other is empty
        var free = 0
        for (hex in hexes) {
            if (hex.isEmpty) {
                free++
                if (free == 2) {
                    return true
                }
            } else {
                free = max(0, free - 1)
            }
        }

        return false
    }

    override fun getTargetHexes(token: Token, board: Board): MutableList<Hex> {
        val neighbors = board.getNeighborHexes(token.hex)
        val result: MutableList<Hex> = ArrayList<Hex>()

        val startingHex = checkNotNull(token.hex)
        for (targetHex in neighbors) {
            val canSlide: Boolean = Rules.instance.canSlideTo(startingHex, targetHex, board)
            val stillOneHive: Boolean = Rules.instance.isOneHiveIntact(startingHex, targetHex, board)
            if (canSlide && stillOneHive) {
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
