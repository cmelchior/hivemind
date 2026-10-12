package dk.ilios.hivemind.model.rules

import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token

class Mosquito : Bug() {
    override fun isFreeToMove(token: Token, board: Board): Boolean =
        false // A mosquito never moves except by mimicing other bugs.

    override fun getTargetHexes(token: Token, board: Board): List<Hex> =
        emptyList() // A mosquito can only move my mimicing other bugs.

    override fun canMimic(): Boolean = true

    override fun canMoveOthers(): Boolean = false
}
