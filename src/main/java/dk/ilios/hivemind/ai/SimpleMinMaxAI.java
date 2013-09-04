package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.*;
import dk.ilios.hivemind.model.rules.Rules;

import java.util.*;

/**
 * AI that implements standard MinMax tree search.
 *
 */
public class SimpleMinMaxAI extends AbstractMinMaxAI {

    private Random random = new Random();
    private int searchDepth = 0; // Search depth for tree

    public SimpleMinMaxAI(String name, BoardValueHeuristic heuristic, int depth, int maxTimeInMillis) {
        super(name, heuristic, depth, maxTimeInMillis);
        this.searchDepth = depth;
    }

    @Override
    public HiveAI copy() {
        return new SimpleMinMaxAI(name, heuristic, searchDepth, maxTimeInMillis);
    }

    @Override
    public GameCommand nextMove(Game state, Board board) {
        maximizingPlayer = state.getActivePlayer();
        start = System.currentTimeMillis();

        // Minimax traversal of game tree
        List<GameCommand> moves = generateMoves(state);
        int bestValue = Integer.MIN_VALUE;
        GameCommand bestMove = GameCommand.PASS;

        for (GameCommand move : moves) {
            applyMove(move, state);
            int value = minimax(state, searchDepth - 1, false);
            if (value > bestValue || value == bestValue && random.nextBoolean()) {
                bestValue = value;
                bestMove = move;
            }
            undoMove(move, state);
        }

        return bestMove;
    }

    /**
     * Minimax traversal. Returns a number between Integer.MIN and Integer.MAX
     * + means current player is winning, - he is loosing
     */
    private int minimax(Game state, int depth, boolean maximizingPlayer) {
        if (isGameOver(state, depth) || depth <= 0 || System.currentTimeMillis() - start > maxTimeInMillis) {
            // Positive values are good for the maximizing player, negative values for minimizing player
            return value(state);

        } else {

            List<GameCommand> moves = generateMoves(state);
            int bestValue = Integer.MIN_VALUE;

            for (GameCommand move : moves) {
                // Update game state and continue traversel
                applyMove(move, state);
                int value = minimax(state, depth - 1, !maximizingPlayer);
                if (maximizingPlayer) {
                    if (value > bestValue || (value == bestValue && random.nextBoolean())) {
                        bestValue = value;
                    }
                } else {
                    if (value < bestValue || (value == bestValue && random.nextBoolean())) {
                        bestValue = value;
                    }
                }
                undoMove(move, state);
            }

            return bestValue;
        }
    }
}
