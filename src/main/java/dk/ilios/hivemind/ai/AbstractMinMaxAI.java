package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.heuristics.BoardValueHeuristic;
import dk.ilios.hivemind.ai.moves.MoveGenerator;
import dk.ilios.hivemind.ai.moves.StandardMoveGenerator;
import dk.ilios.hivemind.ai.statistics.AIStatistics;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Player;
import dk.ilios.hivemind.model.rules.Rules;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMinMaxAI implements HiveAI {

    protected String name;
    protected Game state;
    protected AIStatistics aiStats = new AIStatistics(this);

    protected MoveGenerator moveGenerator = new StandardMoveGenerator();
    protected BoardValueHeuristic heuristic;

    protected int searchDepth;      // Search limit in depth
    protected int maxTimeInMillis;  // Search limit in milliseconds
    protected long start; // Start time in millis when nextMove was called
    protected Player maximizingPlayer; // Player is who is acting as MAX player in the MinMax algorithm


    public AbstractMinMaxAI(String name, BoardValueHeuristic heuristicFunction, int searchDepth, int maxTimeInMillis) {
        this.name = name;
        this.heuristic = heuristicFunction;
        this.searchDepth = searchDepth;
        this.maxTimeInMillis = maxTimeInMillis;
    }

    protected int calculateBoardValue(Game state) {
        aiStats.boardEvaluated();
        return heuristic.calculateBoardValue(state);
    }

    @Override
    public AIStatistics getAiStats() {
        return aiStats;
    }

    @Override
    public String getName() {
        return name;
    }

    protected void setMaximizingPlayer(Player player) {
        maximizingPlayer = player;
    }

    /**
     * Heuristic function. Evaluate the value of the board.
     * + is good for the maximizing player, - is good for the minimizing player
     */
    protected int value(Game state) {
        int result = calculateBoardValue(state);
        if (maximizingPlayer.isBlack()) {
            result = result * -1; // Negative values are good for black in the heuristic function, so if he is maximizing we invert all values
        }
        return result;
    }



    protected Game applyMove(GameCommand command, Game state) {
        command.execute(state);
        return state;
    }

    protected Game undoMove(GameCommand command, Game state) {
        command.undo(state);
        return state;
    }

    protected boolean isGameOver(Game state, int depth) {
        boolean whiteDead = Rules.getInstance().isQueenSurrounded(state.getWhitePlayer(), state.getBoard());
        boolean blackDead = Rules.getInstance().isQueenSurrounded(state.getBlackPlayer(), state.getBoard());
        boolean turnLimit = false;
        if (state.getTurnLimit() > 0) {
            int lookAhead = (depth - searchDepth) * -1;
            turnLimit = (state.getActivePlayer().getMoves() + lookAhead) > state.getTurnLimit();
        }
        return whiteDead || blackDead || turnLimit;
    }

    /**
     * Standard move generation
     */
    protected List<GameCommand> generateMoves(Game state, GameCommand... priorityMoves) {
        ArrayList<GameCommand> initialList = new ArrayList<GameCommand>();
        List<GameCommand> result = moveGenerator.generateMoves(initialList, state);

        for (int i = priorityMoves.length; i > 0; i--) {
            GameCommand priorityMove = priorityMoves[i - 1];
            if (priorityMove != null && result.contains(priorityMove)) {
                result.remove(priorityMove);
                result.add(0, priorityMove);
            }
        }

        aiStats.nodeBranched(result.size());
        return result;
    }

    @Override
    public boolean maintainsStandardPosition() {
        return true;
    }
}
