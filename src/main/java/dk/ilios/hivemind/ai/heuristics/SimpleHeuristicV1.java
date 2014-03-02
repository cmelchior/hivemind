package dk.ilios.hivemind.ai.heuristics;

import dk.ilios.hivemind.ai.HiveAI;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.model.Player;
import dk.ilios.hivemind.model.rules.Rules;

public class SimpleHeuristicV1 implements BoardValueHeuristic {

    @Override
    public int calculateBoardValue(Game state) {
        /**
         * Heuristics
         * - More pieces is better
         * - More free pieces is better
         * - Number of free hexes around the queen both offensive and defensive
         * - Different bugs have different values
         * -
         */
        Player whitePlayer = state.getWhitePlayer();
        Player blackPlayer = state.getBlackPlayer();

        // This means that DRAW is considered a LOSS
        boolean blackWon = Rules.getInstance().isQueenSurrounded(whitePlayer, state.getBoard());
        boolean whiteWon = Rules.getInstance().isQueenSurrounded(blackPlayer, state.getBoard());

        if (blackWon && whiteWon) {
            // A draw is considered a LOSS
            if (state.getActivePlayer().isWhitePlayer()) {
                return HiveAI.MIN + 5;
            }  else {
                return HiveAI.MAX - 5;
            }
        } else if (blackWon) {
            return HiveAI.MIN;
        } else if (whiteWon) {
            return HiveAI.MAX;
        }

        int whiteFreeTokens = Rules.getInstance().getFreeTokens(whitePlayer, state.getBoard()).size();
        int whiteTokensOnBoard = whitePlayer.getNoStartingBugs() - whitePlayer.getSupply().size();
        int whiteHexesFilledAroundOpposingQueen = state.getBoard().getNeighborTokens(blackPlayer.getQueen().getHex()).size();


        int blackFreeTokens = Rules.getInstance().getFreeTokens(blackPlayer, state.getBoard()).size();
        int blackTokensOnBoard = blackPlayer.getNoStartingBugs() - blackPlayer.getSupply().size();
        int blackHexesFilledAroundOpposingQueen = state.getBoard().getNeighborTokens(whitePlayer.getQueen().getHex()).size();

        return 10*(whiteHexesFilledAroundOpposingQueen - blackHexesFilledAroundOpposingQueen)
                + 2*(whiteFreeTokens - blackFreeTokens)
                + 1*(whiteTokensOnBoard-blackTokensOnBoard);
    }
}
