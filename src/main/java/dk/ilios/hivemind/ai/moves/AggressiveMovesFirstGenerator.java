package dk.ilios.hivemind.ai.moves;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.*;
import dk.ilios.hivemind.model.rules.Rules;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Move generator that generate all possible moves, but puts moves that makes them neighbors to the opposing queen
 * first on the list.
 */
public class AggressiveMovesFirstGenerator extends MoveGenerator {

    @Override
    public List<GameCommand> generateMoves(List<GameCommand> initialList, Game state) {
        LinkedList<GameCommand> result = new LinkedList<GameCommand>(initialList);
        Player player = state.getActivePlayer();
        Board board = state.getBoard();

        // If in game ending position, no moves are possible
        if (Rules.getInstance().isQueenSurrounded(player, board) || Rules.getInstance().isQueenSurrounded(state.getOtherPlayer(), board)) {
            return result;
        }

        // If turn 4 and not placed queen, it must be placed now
        Set<Token> availableTokensFromSupply;
        if (player.getMoves() == 3 && !player.hasPlacedQueen()) {
            availableTokensFromSupply = new HashSet<Token>();
            availableTokensFromSupply.add(player.getFromSupply(BugType.QUEEN_BEE));
        }  else {
            availableTokensFromSupply = player.getSupply();
        }

        // Get opposite queen coordinates
        Hex oppositeQueenHex = state.getOtherPlayer().getQueen().getHex();
        int queenQ = (oppositeQueenHex != null) ? oppositeQueenHex.getQ() : Hex.SUPPLY;
        int queenR = (oppositeQueenHex != null) ? oppositeQueenHex.getR() : Hex.SUPPLY;

        // 1) Get moves for all tokens on the board (only allowed if player has placed queen)
        if (player.hasPlacedQueen()) {
            Set<Token> inPlayTokens = Rules.getInstance().getFreeTokens(player, board);
            for (Token token : inPlayTokens) {
                List<Hex> hexes = Rules.getInstance().getTargetHexes(token, board);
                for (Hex hex : hexes) {
                    if (HexagonUtils.distance(queenQ, queenR, hex.getQ(), hex.getR()) <= 1) {
                        result.addFirst(createGameCommand(token, hex)); // Moves near opposing queens added in front of the list.
                    } else {
                        result.add(createGameCommand(token, hex)); // else add to end of list.
                    }
                }
            }
        }

        // 2) Get all moves adding tokens from supply
        for (Token token : availableTokensFromSupply) {
            List<Hex> hexes = Rules.getInstance().getStartHexes(player, board);
            for (Hex hex : hexes) {
                if (HexagonUtils.distance(queenQ, queenR, hex.getQ(), hex.getR()) <= 1) {
                    result.addFirst(createGameCommand(token, hex)); // Moves near opposing queens added in front of the list.
                } else {
                    result.add(createGameCommand(token, hex)); // else add to end of list.
                }
            }
        }

        if (result.isEmpty()) {
            result.add(GameCommand.PASS);
        }

        return result;
    }
}
