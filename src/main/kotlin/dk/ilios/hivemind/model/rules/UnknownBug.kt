package dk.ilios.hivemind.model.rules

import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token

class UnknownBug : Bug() {
    override fun isFreeToMove(token: Token, board: Board): Boolean {
        return false
    }

    override fun getTargetHexes(token: Token, board: Board): MutableList<Hex> {
        return mutableListOf<Hex>()
    }

    override fun canMimic(): Boolean {
        return false
    }

    override fun canMoveOthers(): Boolean {
        return false
    }
}
