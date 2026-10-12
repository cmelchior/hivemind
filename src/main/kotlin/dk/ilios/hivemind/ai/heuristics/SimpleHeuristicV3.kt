package dk.ilios.hivemind.ai.heuristics

import dk.ilios.hivemind.ai.HiveAI
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.model.BugType
import dk.ilios.hivemind.model.rules.Rules

class SimpleHeuristicV3 : BoardValueHeuristic {
    override fun calculateBoardValue(state: Game): Int {
        /**
         * Heuristics
         * - More pieces is better
         * - More free pieces is better
         * - Number of free hexes around the queen both offensive and defensive
         * - Different bugs have different values
         * -
         */
        val whitePlayer = state.whitePlayer
        val blackPlayer = state.blackPlayer

        val blackWon: Boolean = Rules.instance.isQueenSurrounded(whitePlayer, state.board)
        val whiteWon: Boolean = Rules.instance.isQueenSurrounded(blackPlayer, state.board)

        if (blackWon && whiteWon) {
            // A draw is considered almost as bad as a LOSS (no one wants an AI that tries to DRAW)
            if (state.requireActivePlayer().isWhitePlayer) {
                return HiveAI.MIN + 1
            } else {
                return HiveAI.MAX - 1
            }
        } else if (blackWon) {
            return HiveAI.MIN
        } else if (whiteWon) {
            return HiveAI.MAX
        }

        var whiteAntsInPlay = 0
        var blackAntsInPlay = 0

        val hexes = state.board.filledHexes
        for (hex in hexes) {
            val t = hex.topToken
            if (t!!.originalType == BugType.SOLDIER_ANT) {
                if (t.player.isBlack) {
                    blackAntsInPlay++
                } else {
                    whiteAntsInPlay++
                }
            }
        }

        val whiteFreeTokens: Int = Rules.instance.getFreeTokens(whitePlayer, state.board).size
        val whiteTokensOnBoard = whitePlayer.noStartingBugs - whitePlayer.supply.size
        val whiteHexesFilledAroundOpposingQueen =
            state.board.getNeighborTokens(blackPlayer.queen!!.hex).size

        val blackFreeTokens: Int = Rules.instance.getFreeTokens(blackPlayer, state.board).size
        val blackTokensOnBoard = blackPlayer.noStartingBugs - blackPlayer.supply.size
        val blackHexesFilledAroundOpposingQueen =
            state.board.getNeighborTokens(whitePlayer.queen!!.hex).size

        return (100 * (whiteHexesFilledAroundOpposingQueen - blackHexesFilledAroundOpposingQueen) + 20 * (whiteAntsInPlay - blackAntsInPlay) + 10 * (whiteFreeTokens - blackFreeTokens))

        //                + 1*(whiteTokensOnBoard-blackTokensOnBoard)
//                + 5*(whiteFreeTokens - blackFreeTokens);
    }
}
