package dk.ilios.hivemind.model.rules

import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token

class Mosquito : Bug() {
    override fun isFreeToMove(token: Token, board: Board): Boolean {
        return false // A mosquito never moves except by mimicing other bugs.
    }

    override fun getTargetHexes(token: Token, board: Board): MutableList<Hex> {
        return mutableListOf<Hex>() // A mosquito can only move my mimicing other bugs.
    }

    override fun canMimic(): Boolean {
        return true
    }

    override fun canMoveOthers(): Boolean {
        return false
    }
}
