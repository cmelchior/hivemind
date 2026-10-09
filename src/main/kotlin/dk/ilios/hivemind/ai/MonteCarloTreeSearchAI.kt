package dk.ilios.hivemind.ai

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic
import dk.ilios.hivemind.ai.heuristics.SimpleHeuristicV3
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Player
import java.util.*

/**
 * Basic Monte Carlo Tree Search with standard UCT (Upper confidence bound)
 *
 * - Random playout
 */
class MonteCarloTreeSearchAI(name: String?, maxDepth: Int, maxTimeMillis: Int) :
    AbstractMonteCarloTreeSearchAI(name, maxDepth, maxTimeMillis) {
    var random: Random = Random()
    var evaluationFunction: BoardValueHeuristic = SimpleHeuristicV3()

    private var startPlayer: Player? = null

    protected var simulationsMade: Int = 0

    override fun copy(): HiveAI {
        return MonteCarloTreeSearchAI(name, maxDepth, timeLimit)
    }


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

        println("Simulations played: " + simulationsMade)
        return max(root)
    }

    /**
     * Select a leaf node (ie. game node with unexplored children) using some
     * selection algorithm.
     */
    private fun selection(node: GameNode): GameNode {
        node.forwardGameState(state) // Update game state to match current node

        // If partial expanded. Chance of expanding from here is equal to chance of hitting "empty" node if
        // selecting child at random.
        if (node.getChildren().isEmpty() || (!node.isCompletelyVisited && node.getChildren().size < random.nextInt(
                node.getMaxChildren() + 1
            ))
        ) {
            return node // Either node is completly visited or we randomly chose a leaf node from here.
        } else {
            // Select random child node and continue down tree
            val result = selection(node.getChildren().get(random.nextInt(node.getChildren().size)))
            return result
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
        if (moves.size == 0) {
            return leaf
        }

        // 1) Start from a random position
        // 2) Check if move is already a a child, it yes, search right until move found (with loop back to start if needed)
        // 2a) As expand is only called on not fully visited nodes, this should always return a result
        // 3) Add move to tree as a node
        var moveCount = moves.size
        var moveIndex = random.nextInt(moves.size)
        while (leaf.hasChild(moves.get(moveIndex))) {
            moveIndex = (moveIndex + 1) % moves.size
            check(--moveCount >= 0) { "Expand cannot find a new child node" + leaf }
        }

        // Update tree with new node
        val command = moves.get(moveIndex)
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
            val command = moveGenerator.nextMove(state, state.getBoard())
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
    }

    /**
     * Returns the best move from the given game state.
     */
    private fun max(root: GameNode): GameCommand? {
        var command: GameCommand? = GameCommand.PASS
        var maxValue = Double.MIN_VALUE

        val children = root.getChildren()
        for (node in children) {
            if (node.value > maxValue || (node.value == maxValue && random.nextBoolean())) {
                maxValue = node.value
                command = node.command
            }
        }

        println("Value for command: " + maxValue)
        return command
    }
}
