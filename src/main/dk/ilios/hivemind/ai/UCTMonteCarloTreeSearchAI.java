package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic;
import dk.ilios.hivemind.ai.heuristics.SimpleHeuristicV3;
import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.Player;

import java.util.List;
import java.util.Random;

/**
 * Basic Monte Carlo Tree Search
 * - Random expansion/exploitation
 * - Random playout
 *
 * @See [1] Barbara Konz: Applying Monte Carlo Tree Search to Strategic Game Hive
 * @see [2] Guillaume Maurice Jean-Bernard Chaslot: Monte Carlo Tree Search
 * @see [3] Magarana: Open Source Magic AI - https://code.google.com/p/magarena/source/browse/src/magic/ai/MCTSAI2.java
 */
public class UCTMonteCarloTreeSearchAI extends AbstractMonteCarloTreeSearchAi {

    Random random = new Random();
    BoardValueHeuristic evaluationFunction = new SimpleHeuristicV3();

    private Game state;
    private Player startPlayer;

    protected long start; // Start time for requesting a new move.
    protected int simulationsMade = 0;

    // MCTS values
    private double C =  100; // UCT constant for valueing expansion/exploitation. See [1]

    public UCTMonteCarloTreeSearchAI(String name, int maxDepth, int maxTimeMillis) {
        super(name, maxDepth, maxTimeMillis);
    }

    @Override
    public GameCommand nextMove(Game state, Board board) {
        this.state = state;
        startPlayer = state.getActivePlayer();
        start = System.currentTimeMillis();

        GameNode root = new GameNode(null, null);

        while (System.currentTimeMillis() - start < timeLimit) {
            GameNode leaf = selection(root);
            GameNode simulationStartNode = expand(leaf);
            int result = simulation(simulationStartNode);
            backpropagation(simulationStartNode, result);
            simulationsMade++;
        }

//        System.out.println("Simulations played: " + simulationsMade);
        return max(root);
    }

    /**
     * Select a leaf node (ie. game node with unexplored children) using some
     * selection algorithm.
     */
    private GameNode selection(GameNode node) {
        node.forwardGameState(state); // Update game state to match current node

        // RESEARCH: Is a node considered a leaf until all children has been visited?
        // [2] and [3] seems to indicate yes, but it is not very good described in [1] and [2].
        if (node.isCompletelyVisited()) {

            // UTC (Upper Confidence Bound for Trees) used as described in [2]
            double bestScore = Double.NEGATIVE_INFINITY;
            GameNode bestNode = null;
            List<GameNode> children = node.getChildren();
            int terminalNodes = 0;
            for (GameNode child : children) {
                if (child.isTerminal()) {
                    terminalNodes++;
                    continue; // Don't select terminal nodes
                }

                double score =  child.getValue() + C * Math.sqrt(Math.log(node.getVisits())/child.getVisits());
                if (score > bestScore) {
                    bestScore = score;
                    bestNode = child;
                }
            }

            // If no child can be selected, return parent node
            // TODO Ideally we should move up the tree and try another branch.
            if (bestNode == null) {
                return node; // Current node doesn't have any viable children
            } else {
                return selection(bestNode);
            }

        } else {
            return node; // Current node has unvisited childen
        }
    }

    /**
     * Given a leaf node, find a suitable move and add it to the game tree.
     */
    private GameNode expand(GameNode leaf) {

        // Generate all moves from this node
        List<GameCommand> moves = generateMoves(state);
        leaf.setMaxChildren(moves.size());

        // Terminal node check
        if (leaf.isTerminal()) {
            return leaf;
        }

        // 1) Start from a random position
        // 2) Check if move is already a a child, it yes, search right until move found (with loop back to start if needed)
        // 2a) As expand is only called on not fully visited nodes, this should always return a result
        // 3) Add move to tree as a node
        int moveCount = moves.size();
        int moveIndex = random.nextInt(moves.size());
        while (leaf.hasChild(moves.get(moveIndex))) {
            moveIndex = (moveIndex + 1) % moves.size();
            if (--moveCount < 0) {
                throw new IllegalStateException("Expand cannot find a new child node" + leaf);
            }
        }

        // Update tree with new node
        GameCommand command = moves.get(moveIndex);
        GameNode node = new GameNode(command, leaf);
        leaf.addChild(node);
        node.forwardGameState(state);

        return node;
    }

    /**
     * Run a simulation of a game, returns the final value.
     * @Return Return -1/0/+1 or heuristic value?
     */
    private int simulation(GameNode simulationStartNode) {
        RandomAI moveGenerator = new RandomAI("MCTSRandomMoveGenerator");
        return runSimulation(state, moveGenerator, maxDepth);
    }

    private int runSimulation(Game state, HiveAI moveGenerator, int depth) {
        if (isGameOver(state) || depth == 0) {
            return evaluate(state);
        } else {
            GameCommand command = moveGenerator.nextMove(state, state.getBoard());
            command.execute(state);
            int result = runSimulation(state, moveGenerator, depth - 1);
            command.undo(state);
            return result;

        }
    }
    /**
     * Return MAX_VALUE if won, MIN_VALUE if lost, 0 in any other case
     */
    private int evaluate(Game state) {
        int result = evaluationFunction.calculateBoardValue(state);

        if (result == Integer.MIN_VALUE || result == Integer.MAX_VALUE) {
            if (startPlayer.isWhitePlayer()) {
                return (result == Integer.MAX_VALUE ? 1 : -1);
            } else {
                return (result == Integer.MIN_VALUE ? 1 : -1);
            }
        } else {
            return 0; // Any intermediate results are a "draw"
        }
    }


    /**
     * Update the game tree with the result from the simulation. Starts with
     * node from where the simulation started and ends with the root node.
     */
    private void backpropagation(GameNode node, int result) {
        GameNode n = node;
        n.rewindGameState(state);
        while (n.getParent() != null) {
            n.addResult(result);
            n = n.getParent();
            n.rewindGameState(state);
        }

        // Add result to root node as well
        n.addResult(result);
    }

    /**
     * Returns the best move from the given game state.
     */
    private GameCommand max(GameNode root) {
        GameNode bestNode = null;
        double maxValue = Double.MIN_VALUE;

        List<GameNode> children = root.getChildren();
        for (GameNode node : children) {
            if (node.getValue() > maxValue || (node.getValue() == maxValue && random.nextBoolean())) {
                maxValue = node.getValue();
                bestNode = node;
            }
        }

//        System.out.println("Visits: " + bestNode.getVisits() + ", Value: " + maxValue);
        return (bestNode != null && bestNode.getCommand() != null) ? bestNode.getCommand() : GameCommand.PASS;
    }
}