package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Board;

import java.util.List;
import java.util.Random;

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
        int bestValue = HiveAI.MIN;
        GameCommand bestMove = GameCommand.PASS;

        for (GameCommand move : moves) {
            // Update game state and continue traversel
            applyMove(move, state);
            int value = alphabeta(state, searchDepth - 1, bestValue, HiveAI.MAX, false);
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
                if (bestValue == HiveAI.MAX) break;
            }
            undoMove(move, state);
        }

        return bestMove;
    }

    private int alphabeta(Game state, int depth, int alpha, int beta, boolean maximizingPlayer) {

        boolean timeout = System.currentTimeMillis() - start > maxTimeInMillis;
        boolean maxDepthReached = depth <= 0;

        if (isGameOver(state, depth) || maxDepthReached || timeout) {
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
                        alpha = value;
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
                    moveEvaluated++;
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
