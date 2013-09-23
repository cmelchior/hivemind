package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic;
import dk.ilios.hivemind.ai.transpositiontable.TranspositionTable;
import dk.ilios.hivemind.ai.transpositiontable.TranspositionTableEntry;
import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Board;

import java.util.List;
import java.util.Random;

/**
 * AI that implements Minimax tree search algorithm with Alpha-Beta prunning and Iterative Deepening Depth-First Search.
 * Backed by a transposition table.
 *
 * @see http://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
 * @see http://en.wikipedia.org/wiki/Iterative_deepening
 * @see http://en.wikipedia.org/wiki/Transposition_table
 *
 * Other things to consider:
 * - Move reordering
 * - Killer heuristic
 * - http://chessprogramming.wikispaces.com/Quiescence+Search
 * - With move reordering in place, consider NegaScout: http://en.wikipedia.org/wiki/Negascout
 * - Consider http://en.wikipedia.org/wiki/MTD-f
 *
 * NOT implemented yet
 */
public class TranspostionTableIDDFSAlphaBetaMiniMaxAI extends AbstractMinMaxAI {

    private Random random = new Random();
    private TranspositionTable table = new TranspositionTable();

    public TranspostionTableIDDFSAlphaBetaMiniMaxAI(String name, BoardValueHeuristic heuristicFunction, int depth, int maxTimeInMillis) {
        super(name, heuristicFunction, depth, maxTimeInMillis);
    }

    @Override
    public HiveAI copy() {
        return new TranspostionTableIDDFSAlphaBetaMiniMaxAI(name, heuristic, searchDepth, maxTimeInMillis);
    }

    @Override
    public GameCommand nextMove(Game state, Board board) {
        start = System.currentTimeMillis();
        maximizingPlayer = state.getActivePlayer();

        // Iterate depths, effectively a breath-first search, where top nodes get visited multiple times
        int depth = 0;
        int bestValue = Integer.MIN_VALUE;
        GameCommand bestCommand = null;

        Object[] result = new Object[2];
        while(depth <= searchDepth && System.currentTimeMillis() - start < maxTimeInMillis) {
            result = runMinMax(state, depth, result);
            int val = (Integer) result[0];
            if (val > bestValue || val == bestValue && random.nextBoolean()) {
                bestValue = val;
                bestCommand = (GameCommand) result[1];
                if (bestValue == Integer.MAX_VALUE) {
                    return bestCommand; // Game winning move
                }
            }

            depth++;
        }

        return bestCommand; // 2nd best move
    }

    private Object[] runMinMax(Game state, int searchDepth, Object[] result) {

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

        result[0] = bestValue;
        result[1] = bestMove;
        return result;
    }

    private int alphabeta(Game state, int depth, int alpha, int beta, boolean maximizingPlayer) {

        int originalAlpha = alpha;
        int originalBeta = beta;
        GameCommand bestMove = null;

        // Check transposition tsable and adjust values if needed or return result if possible
        long zobristKey = state.getZobristKey();
        TranspositionTableEntry entry = table.getResult(zobristKey);
        if (entry != null && entry.depth >= depth) {
            aiStats.cacheHit();
            bestMove = entry.move;
            if (entry.type == TranspositionTableEntry.PV_NODE) {
                return entry.value;
            } else if (entry.type == TranspositionTableEntry.CUT_NODE && entry.value > alpha) {
                alpha = entry.value;
            } else if (entry.type == TranspositionTableEntry.ALL_NODE && entry.value < beta) {
                beta = entry.value;
            }

            if (alpha >= beta) {
                return entry.value; // Lowerbound is better than upper bound
            }
        }

        // Run algorithm as usual
        int value;
        if (isGameOver(state, depth) || depth <= 0 || System.currentTimeMillis() - start > maxTimeInMillis) {
            value = value(state);
        } else {
            List<GameCommand> moves = generateMoves(state, bestMove);
            if (maximizingPlayer) {
                for (GameCommand move : moves) {
                    bestMove = move;
                    applyMove(move, state);
                    value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer);
                    if (value > alpha) {
                        alpha = value;
                    }
                    undoMove(move, state);

                    // Beta cut-off
                    if (beta <= alpha) {
                        break;
                    }
                }

                value = alpha;

            } else {

                for (GameCommand move : moves) {
                    bestMove = move;
                    applyMove(move, state);
                    value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer);
                    if (value < beta) {
                        beta = value;
                    }
                    undoMove(move, state);

                    // Alpha cut-off
                    if (beta <= alpha) {
                        break;
                    }
                }

                value = beta;
            }
        }

        // Update transposition table
        if (value <= originalAlpha) {
            table.addResult(zobristKey, value, depth, TranspositionTableEntry.CUT_NODE, bestMove);
        } else if (value >= originalBeta) {
            table.addResult(zobristKey, value, depth, TranspositionTableEntry.ALL_NODE, bestMove);
        } else {
            table.addResult(zobristKey, value, depth, TranspositionTableEntry.PV_NODE, bestMove);
        }

        return value;
    }

    @Override
    public boolean maintainsStandardPosition() {
        return true;
    }

}
