package dk.ilios.hivemind.ai.heuristics

import dk.ilios.hivemind.ai.HiveAI
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.model.rules.Rules

class SimpleHeuristicV1 : BoardValueHeuristic {
    override fun calculateBoardValue(state: Game): Int {
        /**
         * Heuristics
         * - More pieces is better
         * - More free pieces is better
         * - Number of free hexes around the queen both offensive and defensive
         * - Different bugs have different values
         * -
         */
        val whitePlayer = state.getWhitePlayer()
        val blackPlayer = state.getBlackPlayer()

        // This means that DRAW is considered a LOSS
        val blackWon: Boolean = Rules.instance.isQueenSurrounded(whitePlayer, state.getBoard())
        val whiteWon: Boolean = Rules.instance.isQueenSurrounded(blackPlayer, state.getBoard())

        if (blackWon && whiteWon) {
            // A draw is considered a LOSS
            if (state.requireActivePlayer().isWhitePlayer) {
                return HiveAI.MIN + 5
            } else {
                return HiveAI.MAX - 5
            }
        } else if (blackWon) {
            return HiveAI.MIN
        } else if (whiteWon) {
            return HiveAI.MAX
        }

        val whiteFreeTokens: Int = Rules.instance.getFreeTokens(whitePlayer, state.getBoard()).size
        val whiteTokensOnBoard = whitePlayer.noStartingBugs - whitePlayer.supply.size
        val whiteHexesFilledAroundOpposingQueen =
            state.getBoard().getNeighborTokens(blackPlayer.queen!!.hex).size


        val blackFreeTokens: Int = Rules.instance.getFreeTokens(blackPlayer, state.getBoard()).size
        val blackTokensOnBoard = blackPlayer.noStartingBugs - blackPlayer.supply.size
        val blackHexesFilledAroundOpposingQueen =
            state.getBoard().getNeighborTokens(whitePlayer.queen!!.hex).size

        return (10 * (whiteHexesFilledAroundOpposingQueen - blackHexesFilledAroundOpposingQueen) + 2 * (whiteFreeTokens - blackFreeTokens) + 1 * (whiteTokensOnBoard - blackTokensOnBoard))
    }
}
