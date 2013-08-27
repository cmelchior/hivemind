package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Board;

import java.util.List;
import java.util.Random;

/**
 * Hive AI using Negamax with Alpha/beta prunning.
 *
 * @see http://en.wikipedia.org/wiki/Negamax
 */
public class NegamaxAI extends AbstractMinMaxAI {

    private Random random = new Random();

    public NegamaxAI(String name, BoardValueHeuristic heuristicFunction, int depth, int maxTimeInMillis) {
        super(name, heuristicFunction, depth, maxTimeInMillis);
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
            // Update game state and continue traversel
            applyMove(move, state);
            int value = negamax(state, searchDepth - 1, bestValue, Integer.MAX_VALUE, false);
            if (value > bestValue || value == bestValue && random.nextBoolean()) {
                bestValue = value;
                bestMove = move;
            }
            undoMove(move, state);
        }

        return bestMove;
    }

    private int negamax(Game state, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (isGameOver(state, depth) || depth <= 0 || System.currentTimeMillis() - start > maxTimeInMillis) {
            return value(state);

        } else {
            List<GameCommand> moves = generateMoves(state);
            for (GameCommand move : moves) {
                applyMove(move, state);
                int value = negamax(state, depth - 1, -beta, -alpha, !maximizingPlayer) * -1;
                undoMove(move, state);
                if (value >= beta) {
                    return value;
                } else if (value > alpha) {
                    alpha = value;
                }
            }
            return alpha;
        }
    }
}
