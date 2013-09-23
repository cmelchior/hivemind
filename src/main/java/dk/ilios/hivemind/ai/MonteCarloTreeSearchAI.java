package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic;
import dk.ilios.hivemind.ai.heuristics.SimpleHeuristicV3;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.Player;

import java.util.List;
import java.util.Random;

/**
 * Basic Monte Carlo Tree Search with standard UCT (Upper confidence bound)
 *
 * - Random playout
 */
public class MonteCarloTreeSearchAI extends AbstractMonteCarloTreeSearchAI {

    Random random = new Random();
    BoardValueHeuristic evaluationFunction = new SimpleHeuristicV3();

    private Game state;
    private Player startPlayer;

    protected long start; // Start time for requesting a new move.
    protected int simulationsMade = 0;

    public MonteCarloTreeSearchAI(String name, int maxDepth, int maxTimeMillis) {
        super(name, maxDepth, maxTimeMillis);
    }

    @Override
    public HiveAI copy() {
        return new MonteCarloTreeSearchAI(name, maxDepth, timeLimit);
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

        System.out.println("Simulations played: " + simulationsMade);
        return max(root);
    }

    /**
     * Select a leaf node (ie. game node with unexplored children) using some
     * selection algorithm.
     */
    private GameNode selection(GameNode node) {
        node.forwardGameState(state); // Update game state to match current node

        // If partial expanded. Chance of expanding from here is equal to chance of hitting "empty" node if
        // selecting child at random.
        if (node.getChildren().isEmpty() || (!node.isCompletelyVisited() && node.getChildren().size() < random.nextInt(node.getMaxChildren() + 1))) {
            return node; // Either node is completly visited or we randomly chose a leaf node from here.
        } else {
            // Select random child node and continue down tree
            GameNode result = selection(node.getChildren().get(random.nextInt(node.getChildren().size())));
            return result;
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
        if (moves.size() == 0) {
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
    }

    /**
     * Returns the best move from the given game state.
     */
    private GameCommand max(GameNode root) {
        GameCommand command = GameCommand.PASS;
        double maxValue = Double.MIN_VALUE;

        List<GameNode> children = root.getChildren();
        for (GameNode node : children) {
            if (node.getValue() > maxValue || (node.getValue() == maxValue && random.nextBoolean())) {
                maxValue = node.getValue();
                command = node.getCommand();
            }
        }

        System.out.println("Value for command: " + maxValue);
        return command;
    }
}