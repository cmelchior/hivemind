package dk.ilios.hivemind.ai.moves

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.BugType
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token
import dk.ilios.hivemind.model.rules.Rules

/**
 * Standard move generator, that return all possible moves for the given game state.
 * No ordering of moves is assumed.
 */
class StandardMoveGenerator : MoveGenerator() {
    override fun generateMoves(initialList: List<GameCommand>, state: Game): List<GameCommand> = buildList {
        addAll(initialList)
        val player = state.requireActivePlayer()
        val board = state.board

        // If in game ending position, no moves are possible
        if (Rules.instance.isQueenSurrounded(player, board) || Rules.instance
                .isQueenSurrounded(checkNotNull(state.otherPlayer), board)
        ) {
            return@buildList
        }

        // If turn 4 and not placed queen, it must be placed now
        val availableTokensFromSupply: Set<Token>
        if (player.moves == 3 && !player.hasPlacedQueen()) {
            availableTokensFromSupply = setOf(player.getFromSupply(BugType.QUEEN_BEE))
        } else {
            availableTokensFromSupply = player.supply
        }

        // Get all moves adding tokens from supply
        for (token in availableTokensFromSupply) {
            val hexes: List<Hex> = Rules.instance.getStartHexes(player, board)
            for (hex in hexes) {
                add(createGameCommand(token, hex))
            }
        }

        // Get moves for all tokens on the board (only allowed if player has placed queen)
        if (player.hasPlacedQueen()) {
            val inPlayTokens: Set<Token> = Rules.instance.getFreeTokens(player, board)
            for (token in inPlayTokens) {
                val hexes = Rules.instance.getTargetHexes(token, board)
                for (hex in hexes) {
                    add(createGameCommand(token, hex))
                }
            }
        }

        if (isEmpty()) {
            add(GameCommand.PASS)
        }
    }
}
