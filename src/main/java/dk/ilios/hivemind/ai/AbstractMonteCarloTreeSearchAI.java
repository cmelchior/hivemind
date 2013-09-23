package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.moves.MoveGenerator;
import dk.ilios.hivemind.ai.moves.StandardMoveGenerator;
import dk.ilios.hivemind.ai.statistics.AIStatistics;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.*;
import dk.ilios.hivemind.model.rules.Rules;

import java.util.*;

public abstract class AbstractMonteCarloTreeSearchAI implements HiveAI {

    protected final int timeLimit; // Timelimit pr. move in millis
    protected final int maxDepth; // Max depth to run simulation

    protected String name;
    protected Game state;

    protected MoveGenerator moveGenerator = new StandardMoveGenerator();

    protected AIStatistics aiStats = new AIStatistics();
    protected long start; // Start time for requesting a new move.


    public AbstractMonteCarloTreeSearchAI(String name, int maxDepth, int maxTimeMillis) {
        this.name = name;
        this.maxDepth = maxDepth;
        this.timeLimit = maxTimeMillis;
    }


    @Override
    public AIStatistics getAiStats() {
        return aiStats;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Generate all moves for the current game state.
     */
    protected List<GameCommand> generateMoves(Game state) {
        List<GameCommand> result = new ArrayList<GameCommand>();
        Player player = state.getActivePlayer();
        Board board = state.getBoard();


        // If turn 4 and not placed queen, it must be placed now
        Set<Token> supply;
        if (player.getMoves() == 3 && !player.hasPlacedQueen()) {
            supply = new HashSet<Token>();
            supply.add(player.getFromSupply(BugType.QUEEN_BEE));
        }  else {
            supply = player.getSupply();
        }

        // Get all moves adding tokens from supply
        for (Token token : supply) {
            List<Hex> hexes = Rules.getInstance().getStartHexes(player, board);
            for (Hex hex : hexes) {
                result.add(createGameCommand(token, hex));
            }
        }

        // Get moves for all tokens on the board (only allowed if player has placed queen)
        if (player.hasPlacedQueen()) {
            Set<Token> inPlayTokens = Rules.getInstance().getFreeTokens(player, board);
            for (Token token : inPlayTokens) {
                List<Hex> hexes = Rules.getInstance().getTargetHexes(token, board);
                for (Hex hex : hexes) {
                    result.add(createGameCommand(token, hex));
                }
            }
        }

        // If no moves available / PASS
        if (result.isEmpty()) {
            result.add(GameCommand.PASS);
        }

        aiStats.nodeBranched(result.size());
        return result;
    }

    protected GameCommand createGameCommand(Token token, Hex hex) {
        if (token.getHex() == null) {
            return new GameCommand(Hex.SUPPLY, Hex.SUPPLY, hex.getQ(), hex.getR(), token, false);
        } else {
            return new GameCommand(token.getHex().getQ(), token.getHex().getR(), hex.getQ(), hex.getR(), token, false);
        }
    }

    /**
     * Returns true if game is in a terminal state, ie. a player has won.
     */
    protected boolean isGameOver(Game state) {
        boolean whiteDead = Rules.getInstance().isQueenSurrounded(state.getWhitePlayer(), state.getBoard());
        boolean blackDead = Rules.getInstance().isQueenSurrounded(state.getBlackPlayer(), state.getBoard());
        return whiteDead || blackDead;
    }


    /**
     * Class describing a MCTS Tree node
     */
    protected class GameNode {
        private GameNode parent; // Reference to parent node
        private int maxChildren = -1; // How many children does the game node has. 0 Indicate game end result. -1 = Node has not been explored
        private Map<GameCommand, GameNode> children = new HashMap<GameCommand, GameNode>(); // Currently explored children
        private GameCommand command; // Command object to execute on game state to get to this game state from parent.

        // MCTS properties
        private int visits = 0; // Games played through this node
        private int totalResults = 0; // Combined value of game results through this node
        private boolean terminal;

        public GameNode(GameCommand command, GameNode parent) {
            this.parent = parent;
            this.command = command;
        }

        public GameNode getParent() {
            return parent;
        }

        public List<GameNode> getChildren() {
            return new ArrayList<GameNode>(children.values());
        }

        public boolean hasChild(GameCommand child) {
            return children.keySet().contains(child);
        }

        public GameCommand getCommand() {
            return command;
        }

        public void setMaxChildren(int maxChildren) {
            // Only possible to set max children once
            if (this.maxChildren == -1) {
                this.maxChildren = maxChildren;
            }
        }

        public int getMaxChildren() {
            return maxChildren;
        }

        public boolean isCompletelyVisited() {
            return children.size() == maxChildren;
        }

        public void addChild(GameNode node) {
            children.put(node.getCommand(), node);
        }

        public void addResult(int result) {
            visits++;
            totalResults += result;
        }

        public double getValue() {
            return totalResults / (double) visits;
        }

        public int getVisits() {
            return visits;
        }

        public int getTotalResults() {
            return totalResults;
        }

        public void forwardGameState(Game state) {
            if (command != null) {
                command.execute(state);
            }
        }

        public void rewindGameState(Game state) {
            if (command != null) {
                command.undo(state);
            }
        }

        public boolean isTerminal() {
            return terminal;
        }
    }

    @Override
    public boolean maintainsStandardPosition() {
        return false;
    }
}
