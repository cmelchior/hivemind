package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Board;

import java.util.List;

/**
 * Hive AI using Negamax with Alpha/beta prunning.
 *
 * @see http://en.wikipedia.org/wiki/Negamax
 */
public class NegamaxAI extends AbstractMinMaxAI {

    public NegamaxAI(String name, BoardValueHeuristic heuristicFunction, int depth, int maxTimeInMillis) {
        super(name, heuristicFunction, depth, maxTimeInMillis);
    }

    @Override
    public HiveAI copy() {
        return new NegamaxAI(name, heuristic, searchDepth, maxTimeInMillis);
    }

    @Override
    public GameCommand nextMove(Game state, Board board) {
        maximizingPlayer = state.getActivePlayer();
        start = System.currentTimeMillis();

        // Player A = white, Player B = black
        if (state.getActivePlayer().isWhitePlayer()) {
            return negamaxRoot(state, searchDepth, HiveAI.MIN, HiveAI.MAX, 1);
        } else {
            return negamaxRoot(state, searchDepth, HiveAI.MIN, HiveAI.MAX, -1);
        }
    }

    private GameCommand negamaxRoot(Game state, int depth, int alpha, int beta, int color) {
        List<GameCommand> moves = generateMoves(state);
        int bestValue = HiveAI.MIN;
        GameCommand bestMove = GameCommand.PASS;

        int i = 0;
        for (GameCommand move : moves) {
            applyMove(move, state);
            int value = -negamax(state, searchDepth - 1, -beta, -alpha, -color);
            alpha = Math.max(alpha, value);
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
            undoMove(move, state);
        }

        return bestMove;
    }

    private int negamax(Game state, int depth, int alpha, int beta, int color) {
        if (isGameOver(state, depth) || depth <= 0 || System.currentTimeMillis() - start > maxTimeInMillis) {
            return color * calculateBoardValue(state);

        } else {
            int bestValue = HiveAI.MIN;
            List<GameCommand> moves = generateMoves(state);
            for (GameCommand move : moves) {
                applyMove(move, state);
                int value = -negamax(state, depth - 1, -beta, -alpha, -color);
                undoMove(move, state);
                bestValue = Math.max(bestValue, value);
                alpha = Math.max(alpha, value);
                if (alpha >= beta) break;
            }
            return bestValue;
        }
    }
}
