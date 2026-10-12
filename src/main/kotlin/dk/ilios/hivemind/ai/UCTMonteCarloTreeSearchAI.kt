package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic
import dk.ilios.hivemind.ai.heuristics.SimpleHeuristicV3
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Player
import kotlin.random.Random
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Basic Monte Carlo Tree Search
 * - Random expansion/exploitation
 * - Random playout
 *
 * @See [1] Barbara Konz: Applying Monte Carlo Tree Search to Strategic Game Hive
 * @see [2] Guillaume Maurice Jean-Bernard Chaslot: Monte Carlo Tree Search
 *
 * @see [3] Magarana: Open Source Magic AI - https://code.google.com/p/magarena/source/browse/src/magic/ai/MCTSAI2.java
 */
class UCTMonteCarloTreeSearchAI(name: String?, maxDepth: Int, maxTimeMillis: Int) :
    AbstractMonteCarloTreeSearchAI(name, maxDepth, maxTimeMillis) {
    var random: Random = Random.Default
    var evaluationFunction: BoardValueHeuristic = SimpleHeuristicV3()

    private var startPlayer: Player? = null

    protected var simulationsMade: Int = 0

    // MCTS values
    private val C = 100.0 // UCT constant for valueing expansion/exploitation. See [1]

    override fun copy(): HiveAI = UCTMonteCarloTreeSearchAI(name, maxDepth, timeLimit)

    override fun nextMove(state: Game, board: Board): GameCommand? {
        this.state = state
        startPlayer = state.requireActivePlayer()
        start = System.currentTimeMillis()

        val root = GameNode(null, null)

        while (System.currentTimeMillis() - start < timeLimit) {
            val leaf = selection(root)
            val simulationStartNode = expand(leaf)
            val result = simulation(simulationStartNode)
            backpropagation(simulationStartNode, result)
            simulationsMade++
        }

        return max(root)
    }

    /**
     * Select a leaf node (ie. game node with unexplored children) using some
     * selection algorithm.
     */
    private fun selection(node: GameNode): GameNode {
        node.forwardGameState(state) // Update game state to match current node

        // RESEARCH: Is a node considered a leaf until all children has been visited?
        // [2] and [3] seems to indicate yes, but it is not very good described in [1] and [2].
        if (node.isCompletelyVisited) {
            // UTC (Upper Confidence Bound for Trees) used as described in [2]

            var bestScore = Double.NEGATIVE_INFINITY
            var bestNode: GameNode? = null
            val children = node.childNodes
            var terminalNodes = 0
            for (child in children) {
                if (child.isTerminal) {
                    terminalNodes++
                    continue  // Don't select terminal nodes
                }

                val score = child.value + C * sqrt(ln(node.visits.toDouble()) / child.visits)
                if (score > bestScore) {
                    bestScore = score
                    bestNode = child
                }
            }

            // If no child can be selected, return parent node
            // TODO Ideally we should move up the tree and try another branch.
            if (bestNode == null) {
                return node // Current node doesn't have any viable children
            } else {
                return selection(bestNode)
            }
        } else {
            return node // Current node has unvisited childen
        }
    }

    /**
     * Given a leaf node, find a suitable move and add it to the game tree.
     */
    private fun expand(leaf: GameNode): GameNode {
        // Generate all moves from this node

        val moves = generateMoves(state)
        leaf.setMaxChildren(moves.size)

        // Terminal node check
        if (leaf.isTerminal) {
            return leaf
        }

        // 1) Start from a random position
        // 2) Check if move is already a a child, it yes, search right until move found (with loop back to start if needed)
        // 2a) As expand is only called on not fully visited nodes, this should always return a result
        // 3) Add move to tree as a node
        var moveCount = moves.size
        var moveIndex = random.nextInt(moves.size)
        while (leaf.hasChild(moves[moveIndex])) {
            moveIndex = (moveIndex + 1) % moves.size
            check(--moveCount >= 0) { "Expand cannot find a new child node" + leaf }
        }

        // Update tree with new node
        val command = moves[moveIndex]
        val node = GameNode(command, leaf)
        leaf.addChild(node)
        node.forwardGameState(state)

        return node
    }

    /**
     * Run a simulation of a game, returns the final value.
     * @Return Return -1/0/+1 or heuristic value?
     */
    private fun simulation(simulationStartNode: GameNode?): Int {
        val moveGenerator = RandomAI("MCTSRandomMoveGenerator")
        return runSimulation(state, moveGenerator, maxDepth)
    }

    private fun runSimulation(state: Game, moveGenerator: HiveAI, depth: Int): Int {
        if (isGameOver(state) || depth == 0) {
            return evaluate(state)
        } else {
            val command = moveGenerator.nextMove(state, state.board)
            command!!.execute(state)
            val result = runSimulation(state, moveGenerator, depth - 1)
            command.undo(state)
            return result
        }
    }

    /**
     * Return MAX_VALUE if won, MIN_VALUE if lost, 0 in any other case
     */
    private fun evaluate(state: Game): Int {
        val result = evaluationFunction.calculateBoardValue(state)

        if (result == HiveAI.MIN || result == HiveAI.MAX) {
            if (startPlayer!!.isWhitePlayer) {
                return (if (result == HiveAI.MAX) 1 else -1)
            } else {
                return (if (result == HiveAI.MIN) 1 else -1)
            }
        } else {
            return 0 // Any intermediate results are a "draw"
        }
    }


    /**
     * Update the game tree with the result from the simulation. Starts with
     * node from where the simulation started and ends with the root node.
     */
    private fun backpropagation(node: GameNode, result: Int) {
        var n = node
        n.rewindGameState(state)
        while (n.parent != null) {
            n.addResult(result)
            n = n.parent
            n.rewindGameState(state)
        }

        // Add result to root node as well
        n.addResult(result)
    }

    /**
     * Returns the best move from the given game state.
     */
    private fun max(root: GameNode): GameCommand? {
        var bestNode: GameNode? = null
        var maxValue = Double.MIN_VALUE

        val children = root.childNodes
        for (node in children) {
            if (node.value > maxValue || (node.value == maxValue && random.nextBoolean())) {
                maxValue = node.value
                bestNode = node
            }
        }

        //        System.out.println("Visits: " + bestNode.getVisits() + ", Value: " + maxValue);
        return if (bestNode != null && bestNode.command != null) bestNode.command else GameCommand.PASS
    }
}
