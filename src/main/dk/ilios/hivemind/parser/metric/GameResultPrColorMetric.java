package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.ai.HiveAI;
import dk.ilios.hivemind.ai.IDDFSAlphaBetaMiniMaxAI;
import dk.ilios.hivemind.ai.heuristics.SimpleHeuristicV3;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.game.GameStatus;
import dk.ilios.hivemind.parser.BoardspaceGameType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Reports game result, ie. which color won or draw.
 */
public class GameResultPrColorMetric extends Metric {

    public static final String FILE_NAME = "game_results.data";
    private Map<String, Result> results = new HashMap<String, Result>();

    private HiveAI ai = new IDDFSAlphaBetaMiniMaxAI("WinnerAI", new SimpleHeuristicV3(), 2, 20);
    private boolean predictMoves = false; // If true, the next two moves are predicted if the game has not ended

    public GameResultPrColorMetric(boolean predictLastMoves) {
        predictMoves = predictLastMoves;
        results.put("all", new Result());
    }

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        String variantKey = "all-" + type.getPrefix();
        String groupKey = type.getPrefix() + "-" + variant;

        if (!results.containsKey(variantKey)) {
            results.put(variantKey, new Result());
        }

        if (!results.containsKey(groupKey)) {
            results.put(groupKey, new Result());
        }

        Result allResults = results.get("all");
        Result variantResults = results.get(variantKey);
        Result groupResults = results.get(groupKey);

        GameStatus status = game.getStatus();
        switch (status) {
            case RESULT_DRAW:
                allResults.draw++;
                variantResults.draw++;
                groupResults.draw++;
                break;

            case RESULT_BLACK_WINS:
                allResults.blackWin++;
                variantResults.blackWin++;
                groupResults.blackWin++;
                break;

            case RESULT_WHITE_WINS:
                allResults.whiteWin++;
                variantResults.whiteWin++;
                groupResults.whiteWin++;
                break;

            default:
                // Try to play two more turns to determine if the game was ended because a win was "sure"
                if (predictMoves) {
                    game.continueGame(ai.nextMove(game, game.getBoard()));
                    game.continueGame(ai.nextMove(game, game.getBoard()));
                }

                if (game.getStatus() == GameStatus.RESULT_WHITE_WINS) {
                    allResults.whiteWin++;
                    variantResults.whiteWin++;
                    groupResults.whiteWin++;

                } else if (game.getStatus() == GameStatus.RESULT_BLACK_WINS) {
                    allResults.blackWin++;
                    variantResults.blackWin++;
                    groupResults.blackWin++;

                } else {
                    allResults.other++;
                    variantResults.other++;
                    groupResults.other++;
                }
        }

    }

    @Override
    public void save() {
        StringBuilder sb = new StringBuilder();
        StringBuilder labels = new StringBuilder("label");
        StringBuilder white = new StringBuilder("white-wins");
        StringBuilder black = new StringBuilder("black-wins");
        StringBuilder draw = new StringBuilder("draw");
        StringBuilder other = new StringBuilder("other");

        Object[] keys = new ArrayList<String>(results.keySet()).toArray();
        Arrays.sort(keys);

        for (Object key : keys) {
            Result r = results.get(key);
            labels.append('\t').append(key);
            white.append('\t').append(r.whiteWin*100/r.getTotal());
            black.append('\t').append(r.blackWin*100/r.getTotal());
            draw.append('\t').append(r.draw*100/r.getTotal());
            other.append('\t').append(r.other*100/r.getTotal());
        }

        sb.append(labels.toString()).append('\n');
        sb.append(white.toString()).append('\n');
        sb.append(black.toString()).append('\n');
        sb.append(draw.toString()).append('\n');
        sb.append(other.toString()).append('\n');

        saveStringToDisc(FILE_NAME, sb.toString());
    }

    private class Result {
        public int whiteWin = 0;
        public int blackWin = 0;
        public int draw = 0;
        public int other = 0;

        public float getTotal() {
            return (float) whiteWin + blackWin + draw + other;
        }
    }


}
