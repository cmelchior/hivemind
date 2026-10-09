package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.statistics.AIStatistics
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.BugType
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token
import dk.ilios.hivemind.model.rules.Rules
import java.util.*

/**
 * Test AI that randomly either places a bug or moves a existing bug.
 * For the first 3 turns there is a 50% each turn to place the queen.
 * If queen has not been placed by turn 3 it will always happen on turn 4.
 */
class RandomAI(override val name: String?) : HiveAI {
    private val random = Random()
    private lateinit var state: Game
    private val stats = AIStatistics(this)


    override fun copy(): HiveAI {
        return RandomAI(name)
    }

    @Synchronized
    override fun nextMove(state: Game, board: Board): GameCommand {
        this.state = state
        val activePlayer = state.requireActivePlayer()

        // If by 4th turn the queen hasn't been placed yet, it must be placed
        if (state.requireActivePlayer().moves == 3 && !activePlayer.hasPlacedQueen()) {
            return moveQueenToRandomBoardLocation()
        }

        // 50% chance to place queen the first 3 turns
        if (!activePlayer.hasPlacedQueen() && random.nextBoolean()) {
            return moveQueenToRandomBoardLocation()
        }


        // 50% to move token on board instead of adding new. Always add tokens until queen has been placed
        val moveTokenOnBoard =
            activePlayer.hasPlacedQueen() && random.nextBoolean() && this.randomTokenFromBoard != null

        // Supply still has available tokens
        if (!moveTokenOnBoard && haveTokensInSupply()) {
            val token = checkNotNull(this.randomTokenFromSupply)
            return moveTokenFromSupplyToRandomBoardLocation(token)
        } else {
            val token = this.randomTokenFromBoard
            if (token != null) {
                return moveTokenToRandomLocation(token)
            } else {
                return GameCommand.PASS
            }
        }
    }

    override val aiStats: AIStatistics
        get() = stats

    private fun moveQueenToRandomBoardLocation(): GameCommand {
        val token = state.requireActivePlayer().getFromSupply(BugType.QUEEN_BEE)
        return moveTokenFromSupplyToRandomBoardLocation(token)
    }

    private fun haveTokensInSupply(): Boolean {
        return state.requireActivePlayer().supply.size > 0
    }

    private fun moveTokenToRandomLocation(token: Token): GameCommand {
        val targets = checkNotNull(Rules.instance.getTargetHexes(token, state.getBoard()))
        if (targets.size == 0) {
            return GameCommand.PASS
        } else {
            val fromHex = token.hex
            val hex = targets.get(random.nextInt(targets.size))

            return GameCommand(fromHex!!.q, fromHex.r, hex.q, hex.r, token, false)
        }
    }

    private fun moveTokenFromSupplyToRandomBoardLocation(token: Token): GameCommand {
        val targets: MutableList<Hex> =
            Rules.instance.getStartHexes(token.player, state.getBoard())
        if (targets.size == 0) {
            return GameCommand.PASS
        } else {
            val hex = targets.get(random.nextInt(targets.size))
            return GameCommand(Hex.SUPPLY, Hex.SUPPLY, hex.q, hex.r, token, false)
        }
    }

    val randomTokenFromSupply: Token?
        get() {
            val player = state.requireActivePlayer()
            val supply = player.supply
            val size = supply.size
            if (size > 0) {
                for (t in supply) {
                    return player.getFromSupply(t.type)
                }
            }

            return null
        }

    val randomTokenFromBoard: Token?
        get() {
            val activePlayer = state.requireActivePlayer()
            val tokens: MutableSet<Token> =
                Rules.instance
                    .getFreeTokens(activePlayer, state.getBoard())
            for (token in tokens) {
                if (token.player == activePlayer) {
                    return token
                }
            }

            return null
        }


    override fun maintainsStandardPosition(): Boolean {
        return false
    }
}
