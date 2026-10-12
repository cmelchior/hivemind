package dk.ilios.hivemind.ai.moves

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token

/**
 * Interface for move generators used by HiveAI's.
 */
abstract class MoveGenerator {
    abstract fun generateMoves(initialList: List<GameCommand>, state: Game): List<GameCommand>

    protected fun createGameCommand(token: Token, hex: Hex): GameCommand {
        val fromHex = token.hex
        if (fromHex == null) {
            return GameCommand(Hex.SUPPLY, Hex.SUPPLY, hex.q, hex.r, token, false)
        } else {
            return GameCommand(fromHex.q, fromHex.r, hex.q, hex.r, token, false)
        }
    }
}
