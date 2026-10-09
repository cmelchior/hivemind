package dk.ilios.hivemind.ai.moves

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.BugType
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.HexagonUtils
import dk.ilios.hivemind.model.Token
import dk.ilios.hivemind.model.rules.Rules
import java.util.*

/**
 * Move generator that generate all possible moves, but puts moves that makes them neighbors to the opposing queen
 * first on the list.
 */
class AggressiveMovesFirstGenerator : MoveGenerator() {
    override fun generateMoves(initialList: MutableList<GameCommand>, state: Game): MutableList<GameCommand> {
        val result = LinkedList<GameCommand>(initialList)
        val player = state.requireActivePlayer()
        val board = state.getBoard()

        // If in game ending position, no moves are possible
        if (Rules.instance.isQueenSurrounded(player, board) || Rules.instance
                .isQueenSurrounded(checkNotNull(state.otherPlayer), board)
        ) {
            return result
        }

        // If turn 4 and not placed queen, it must be placed now
        val availableTokensFromSupply: MutableSet<Token>
        if (player.moves == 3 && !player.hasPlacedQueen()) {
            availableTokensFromSupply = HashSet<Token>()
            availableTokensFromSupply.add(player.getFromSupply(BugType.QUEEN_BEE))
        } else {
            availableTokensFromSupply = player.supply
        }

        // Get opposite queen coordinates
        val oppositeQueenHex = checkNotNull(state.otherPlayer).queen!!.hex
        val queenQ = if (oppositeQueenHex != null) oppositeQueenHex.q else Hex.SUPPLY
        val queenR = if (oppositeQueenHex != null) oppositeQueenHex.r else Hex.SUPPLY

        // 1) Get moves for all tokens on the board (only allowed if player has placed queen)
        if (player.hasPlacedQueen()) {
            val inPlayTokens: MutableSet<Token> = Rules.instance.getFreeTokens(player, board)
            for (token in inPlayTokens) {
                val hexes = checkNotNull(Rules.instance.getTargetHexes(token, board))
                for (hex in hexes) {
                    if (HexagonUtils.distance(queenQ, queenR, hex.q, hex.r) <= 1) {
                        result.addFirst(
                            createGameCommand(
                                token,
                                hex
                            )
                        ) // Moves near opposing queens added in front of the list.
                    } else {
                        result.add(createGameCommand(token, hex)) // else add to end of list.
                    }
                }
            }
        }

        // 2) Get all moves adding tokens from supply
        for (token in availableTokensFromSupply) {
            val hexes: MutableList<Hex> = Rules.instance.getStartHexes(player, board)
            for (hex in hexes) {
                if (HexagonUtils.distance(queenQ, queenR, hex.q, hex.r) <= 1) {
                    result.addFirst(
                        createGameCommand(
                            token,
                            hex
                        )
                    ) // Moves near opposing queens added in front of the list.
                } else {
                    result.add(createGameCommand(token, hex)) // else add to end of list.
                }
            }
        }

        if (result.isEmpty()) {
            result.add(GameCommand.PASS)
        }

        return result
    }
}
