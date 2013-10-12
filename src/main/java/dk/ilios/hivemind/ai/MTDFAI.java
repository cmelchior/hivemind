package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic;
import dk.ilios.hivemind.ai.transpositiontable.TranspositionTable;
import dk.ilios.hivemind.ai.transpositiontable.TranspositionTableEntry;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.utils.LimitedBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Implementation of a MTD(f) algorithm search algorithm.
 * We use our best AlphaBeta algorithm as basis (TranspositionsTable, Killer Moves).
 *
 * @see http://askeplaat.wordpress.com/publications/mtdf-algorithm/
 */
public class MTDFAI extends AbstractMinMaxAI {

    private Random random = new Random();
    private TranspositionTable table = new TranspositionTable();
    private ArrayList<LimitedBuffer<GameCommand>> killerMoves = new ArrayList<LimitedBuffer<GameCommand>>();

    public MTDFAI(String name, BoardValueHeuristic heuristicFunction, int depth, int maxTimeInMillis) {
        super(name, heuristicFunction, depth, maxTimeInMillis);
        for (int i = 0; i < depth; i++) {
            killerMoves.add(new LimitedBuffer<GameCommand>(2));
        }
    }

    @Override
    public HiveAI copy() {
        return new KillerHeuristicTranspostionTableIDDFSAlphaBetaMiniMaxAI(name, heuristic, searchDepth, maxTimeInMillis);
    }

    @Override
    public GameCommand nextMove(Game state, Board board) {
        start = System.currentTimeMillis();
        maximizingPlayer = state.getActivePlayer();

        // Clear previous killer moves
        for (LimitedBuffer<GameCommand> buffer : killerMoves) {
            buffer.clear();
        }

        // Iterate depths, effectively a breath-first search, where top nodes get visited multiple times
        int depth = 1;
        int bestValue = 0;
        GameCommand bestCommand = null;

        Object[] result;
        while(depth <= searchDepth && System.currentTimeMillis() - start < maxTimeInMillis) {
            result = MTDF(state, bestValue, depth);
            bestValue = (Integer) result[0];
            bestCommand = (GameCommand) result[1];
            if (bestValue == Integer.MAX_VALUE) {
                return bestCommand; // Game winning move
            }

            depth++;
        }

        return bestCommand; // Best known move
    }

    private Object[] MTDF(Game state, int bestValue, int depth) {
        int value = bestValue;
        Object[] result = new Object[2];

        int upperbound = Integer.MAX_VALUE;
        int lowerbound = Integer.MIN_VALUE;
        int beta;

        do {
            beta = (value == lowerbound) ? value + 1 : value;
            result = runAlphaBetaWithMemory(state, beta - 1, beta, depth, result);
            value = (Integer) result[0];
            if (value < beta) {
                upperbound = value;
            } else {
                lowerbound = value;
            }
        } while (lowerbound >= upperbound);

        return result;
    }

    private Object[] runAlphaBetaWithMemory(Game state, int startAlpha, int startBeta, int depth, Object[] result) {

        // Minimax traversal of game tree
        List<GameCommand> moves = generateMoves(state);
        int bestValue = Integer.MIN_VALUE;
        GameCommand bestMove = GameCommand.PASS;

        for (GameCommand move : moves) {
            // Update game state and continue traversel
            applyMove(move, state);
            int value = alphaBetaWithMemory(state, bestValue, Integer.MAX_VALUE, searchDepth - 1, false);
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



    private int alphaBetaWithMemory(Game state, int alpha, int beta, int depth, boolean maximizingPlayer) {

        // Check transposition table and adjust values if needed or return result if possible
        long zobristKey = state.getZobristKey();
        TranspositionTableEntry entry = table.getResult(zobristKey);
        if (entry != null && entry.depth >= depth) {
            aiStats.cacheHit();
            if (entry.type == TranspositionTableEntry.PV_NODE) {
                return entry.value; // Shouldn't happen in zero-window searches (why?)
            } else if (entry.type == TranspositionTableEntry.CUT_NODE && entry.value > alpha) {
                alpha = entry.value;
            } else if (entry.type == TranspositionTableEntry.ALL_NODE && entry.value < beta) {
                beta = entry.value;
            }

            if (alpha >= beta) {
                return entry.value;
            }
        }

        int originalAlpha = alpha;
        int originalBeta = beta;
        GameCommand bestMove = (entry != null) ? entry.move : null;

        // Run algorithm as usual
        int value;
        if (isGameOver(state, depth) || depth <= 0 || System.currentTimeMillis() - start > maxTimeInMillis) {
            value = value(state);
        } else {

            // Generate moves
            GameCommand[] killMoves = new GameCommand[2];
            killerMoves.get(depth).toArray(killMoves);
            List<GameCommand> moves = generateMoves(state, bestMove, killMoves[0], killMoves[1]);
            int moveEvaluated = 0;

            if (maximizingPlayer) {
                for (GameCommand move : moves) {
                    moveEvaluated++;
                    bestMove = move;
                    applyMove(move, state);
                    value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer);
                    if (value > alpha) {
                        alpha = value;
                    }
                    undoMove(move, state);

                    // Beta cut-off
                    if (beta <= alpha) {
                        aiStats.cutOffAfter(moveEvaluated);
                        killerMoves.get(depth).add(move);
                        break;
                    }
                }

                value = alpha;

            } else {

                for (GameCommand move : moves) {
                    moveEvaluated++;
                    bestMove = move;
                    applyMove(move, state);
                    value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer);
                    if (value < beta) {
                        beta = value;
                    }
                    undoMove(move, state);

                    // Alpha cut-off
                    if (beta <= alpha) {
                        aiStats.cutOffAfter(moveEvaluated);
                        killerMoves.get(depth).add(move);
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

    private int alphabeta(Game state, int depth, int alpha, int beta, boolean maximizingPlayer) {

        int originalAlpha = alpha;
        int originalBeta = beta;
        GameCommand bestMove = null;

        // Check transposition table and adjust values if needed or return result if possible
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

            // Generate moves
            GameCommand[] killMoves = new GameCommand[2];
            killerMoves.get(depth).toArray(killMoves);
            List<GameCommand> moves = generateMoves(state, bestMove, killMoves[0], killMoves[1]);
            int moveEvaluated = 0;

            if (maximizingPlayer) {
                for (GameCommand move : moves) {
                    moveEvaluated++;
                    bestMove = move;
                    applyMove(move, state);
                    value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer);
                    if (value > alpha) {
                        alpha = value;
                    }
                    undoMove(move, state);

                    // Beta cut-off
                    if (beta <= alpha) {
                        aiStats.cutOffAfter(moveEvaluated);
                        killerMoves.get(depth).add(move);
                        break;
                    }
                }

                value = alpha;

            } else {

                for (GameCommand move : moves) {
                    moveEvaluated++;
                    bestMove = move;
                    applyMove(move, state);
                    value = alphabeta(state, depth - 1, alpha, beta, !maximizingPlayer);
                    if (value < beta) {
                        beta = value;
                    }
                    undoMove(move, state);

                    // Alpha cut-off
                    if (beta <= alpha) {
                        aiStats.cutOffAfter(moveEvaluated);
                        killerMoves.get(depth).add(move);
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
            table.addResult(zobristKey, value, depth, TranspositionTableEntry.PV_NODE, bestMove);  // Shouldn't happen in zero-window searches (why?)
        }

        return value;
    }

    @Override
    public boolean maintainsStandardPosition() {
        return true;
    }
}
