package dk.ilios.hivemind.ai.heuristics;

import dk.ilios.hivemind.ai.HiveAI;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.model.BugType;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Player;
import dk.ilios.hivemind.model.Token;
import dk.ilios.hivemind.model.rules.Rules;

import java.util.List;

public class SimpleHeuristicV3 implements BoardValueHeuristic {

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

        boolean blackWon = Rules.getInstance().isQueenSurrounded(whitePlayer, state.getBoard());
        boolean whiteWon = Rules.getInstance().isQueenSurrounded(blackPlayer, state.getBoard());

        if (blackWon && whiteWon) {
            // A draw is considered almost as bad as a LOSS (no one wants an AI that tries to DRAW)
            if (state.getActivePlayer().isWhitePlayer()) {
                return HiveAI.MIN + 1;
            }  else {
                return HiveAI.MAX - 1;
            }
        } else if (blackWon) {
            return HiveAI.MIN;
        } else if (whiteWon) {
            return HiveAI.MAX;
        }

        int whiteAntsInPlay = 0;
        int blackAntsInPlay = 0;

        List<Hex> hexes = state.getBoard().getFilledHexes();
        for (Hex hex: hexes) {
            Token t = hex.getTopToken();
            if (t.getOriginalType() == BugType.SOLDIER_ANT) {
                if (t.getPlayer().isBlack()) {
                    blackAntsInPlay++;
                } else {
                    whiteAntsInPlay++;
                }
            }
        }

        int whiteFreeTokens = Rules.getInstance().getFreeTokens(whitePlayer, state.getBoard()).size();
        int whiteTokensOnBoard = whitePlayer.getNoStartingBugs() - whitePlayer.getSupply().size();
        int whiteHexesFilledAroundOpposingQueen = state.getBoard().getNeighborTokens(blackPlayer.getQueen().getHex()).size();

        int blackFreeTokens = Rules.getInstance().getFreeTokens(blackPlayer, state.getBoard()).size();
        int blackTokensOnBoard = blackPlayer.getNoStartingBugs() - blackPlayer.getSupply().size();
        int blackHexesFilledAroundOpposingQueen = state.getBoard().getNeighborTokens(whitePlayer.getQueen().getHex()).size();

        return 100*(whiteHexesFilledAroundOpposingQueen - blackHexesFilledAroundOpposingQueen)
                + 20*(whiteAntsInPlay - blackAntsInPlay)
                + 10*(whiteFreeTokens - blackFreeTokens);

//                + 1*(whiteTokensOnBoard-blackTokensOnBoard)
//                + 5*(whiteFreeTokens - blackFreeTokens);
    }
}
