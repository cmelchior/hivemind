package dk.ilios.hivemind.model.rules

import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token

class PillBug : Bug() {
    override fun isFreeToMove(token: Token, board: Board): Boolean = false

    override fun getTargetHexes(token: Token, board: Board): List<Hex> = emptyList()

    override fun canMimic(): Boolean = false

    override fun canMoveOthers(): Boolean = true
}
