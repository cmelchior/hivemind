package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.*;
import dk.ilios.hivemind.model.rules.Rules;

import java.util.*;

/**
 * AI that implements Minimax tree search algorithm with Alpha-Beta prunning.
 *
 */
public class AlphaBetaMiniMaxAI extends AbstractMinMaxAI {

    private Random random = new Random();

    public AlphaBetaMiniMaxAI(String name, BoardValueHeuristic heuristicFunction, int depth, int maxTimeInMillis) {
        super(name, heuristicFunction, depth, maxTimeInMillis);
    }

    @Override
    public HiveAI copy() {
        return new AlphaBetaMiniMaxAI(name, heuristic, searchDepth, maxTimeInMillis);
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
            int value = alphabeta(state, searchDepth - 1, bestValue, Integer.MAX_VALUE, false);
            if (value > bestValue || value == bestValue && random.nextBoolean()) {
                bestValue = value;
                bestMove = move;
            }
            undoMove(move, state);
        }

        return bestMove;
    }

    private int alphabeta(Game state, int depth, int alpha, int beta, boolean maximizingPlayer) {

        if (isGameOver(state, depth) || depth <= 0 || System.currentTimeMillis() - start > maxTimeInMillis) {
            return value(state);

        } else {

            List<GameCommand> moves = generateMoves(state);
            int moveEvaluated = 0;
            if (maximizingPlayer) {
                for (GameCommand move : moves) {
                    applyMove(move, state);
                    moveEvaluated++;
                    int value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer);
                    if (value > alpha) {
                        alpha  = value;
                    }
                    undoMove(move, state);

                    // Beta cut-off
                    if (beta <= alpha) {
                        aiStats.cutOffAfter(moveEvaluated);
                        break;
                    }
                }
                return alpha;

            } else {

                for (GameCommand move : moves) {
                    applyMove(move, state);
                    int value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer);
                    if (value < beta) {
                        beta = value;
                    }
                    undoMove(move, state);

                    // Alpha cut-off
                    if (beta <= alpha) {
                        aiStats.cutOffAfter(moveEvaluated);
                        break;
                    }
                }
                return beta;
            }
        }
    }
}
