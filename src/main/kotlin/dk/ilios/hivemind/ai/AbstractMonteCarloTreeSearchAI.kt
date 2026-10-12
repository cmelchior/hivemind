package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.moves.MoveGenerator
import dk.ilios.hivemind.ai.moves.StandardMoveGenerator
import dk.ilios.hivemind.ai.statistics.AIStatistics
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.BugType
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Token
import dk.ilios.hivemind.model.rules.Rules

abstract class AbstractMonteCarloTreeSearchAI(name: String?, maxDepth: Int, maxTimeMillis: Int) : HiveAI {
    protected val timeLimit: Int // Timelimit pr. move in millis
    protected val maxDepth: Int // Max depth to run simulation

    final override val name: String?
    protected lateinit var state: Game

    protected var moveGenerator: MoveGenerator = StandardMoveGenerator()

    final override val aiStats: AIStatistics = AIStatistics(this)
    protected var start: Long = 0 // Start time for requesting a new move.


    init {
        this.name = name
        this.maxDepth = maxDepth
        this.timeLimit = maxTimeMillis
    }


    /**
     * Generate all moves for the current game state.
     */
    protected fun generateMoves(state: Game): List<GameCommand> = buildList {
        val player = state.requireActivePlayer()
        val board = state.board


        // If turn 4 and not placed queen, it must be placed now
        val supply: Set<Token>
        if (player.moves == 3 && !player.hasPlacedQueen()) {
            supply = setOf(player.getFromSupply(BugType.QUEEN_BEE))
        } else {
            supply = player.supply
        }

        // Get all moves adding tokens from supply
        for (token in supply) {
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

        // If no moves available / PASS
        if (isEmpty()) {
            add(GameCommand.PASS)
        }

        aiStats.nodeBranched(size)
    }

    protected fun createGameCommand(token: Token, hex: Hex): GameCommand {
        val fromHex = token.hex
        if (fromHex == null) {
            return GameCommand(Hex.SUPPLY, Hex.SUPPLY, hex.q, hex.r, token, false)
        } else {
            return GameCommand(fromHex.q, fromHex.r, hex.q, hex.r, token, false)
        }
    }

    /**
     * Returns true if game is in a terminal state, ie. a player has won.
     */
    protected fun isGameOver(state: Game): Boolean {
        val whiteDead: Boolean =
            Rules.instance.isQueenSurrounded(state.whitePlayer, state.board)
        val blackDead: Boolean =
            Rules.instance.isQueenSurrounded(state.blackPlayer, state.board)
        return whiteDead || blackDead
    }


    /**
     * Class describing a MCTS Tree node
     */
    protected inner class GameNode(// Command object to execute on game state to get to this game state from parent.
        val command: GameCommand?, // Reference to parent node
        val parent: GameNode?
    ) {
        private var maxChildren =
            -1 // How many children does the game node has. 0 Indicate game end result. -1 = Node has not been explored
        private val children: MutableMap<GameCommand?, GameNode> =
            hashMapOf<GameCommand?, GameNode>() // Currently explored children

        // MCTS properties
        var visits: Int = 0 // Games played through this node
            private set
        var totalResults: Int = 0 // Combined value of game results through this node
            private set
        val isTerminal: Boolean = false


        val childNodes: List<GameNode>
            get() = children.values.toList()

        fun hasChild(child: GameCommand?): Boolean = children.keys.contains(child)

        fun setMaxChildren(maxChildren: Int) {
            // Only possible to set max children once
            if (this.maxChildren == -1) {
                this.maxChildren = maxChildren
            }
        }

        fun getMaxChildren(): Int = maxChildren

        val isCompletelyVisited: Boolean
            get() = children.size == maxChildren

        fun addChild(node: GameNode) {
            children[node.command] = node
        }

        fun addResult(result: Int) {
            visits++
            totalResults += result
        }

        val value: Double
            get() = totalResults / visits.toDouble()

        fun forwardGameState(state: Game) {
            if (command != null) {
                command.execute(state)
            }
        }

        fun rewindGameState(state: Game) {
            if (command != null) {
                command.undo(state)
            }
        }
    }

    override fun maintainsStandardPosition(): Boolean = false
}
