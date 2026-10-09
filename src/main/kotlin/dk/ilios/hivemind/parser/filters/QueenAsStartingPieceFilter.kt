package dk.ilios.hivemind.parser.filters

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.model.BugType

/**
 * Don't analyze games that start with the queen for either player
 */
class QueenAsStartingPieceFilter : Filter {
    override fun analyseGame(type: String, game: Game): Boolean {
        val whiteTurn1 = game.getMove(game.getWhitePlayer(), 1)
        val blackTurn1 = game.getMove(game.getBlackPlayer(), 1)
        if (whiteTurn1 != null && blackTurn1 != null) {
            return !(whiteTurn1.token!!.originalType == BugType.QUEEN_BEE
                    || blackTurn1.token!!.originalType == BugType.QUEEN_BEE)
        } else {
            return false
        }
    }
}
