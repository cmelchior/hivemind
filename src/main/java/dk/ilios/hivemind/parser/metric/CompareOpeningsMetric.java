package dk.ilios.hivemind.parser.metric;


import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.game.GameStatus;
import dk.ilios.hivemind.model.Player;
import dk.ilios.hivemind.parser.BoardspaceGameType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Show openings for both black and white and their respective win rates
 */
public class CompareOpeningsMetric extends Metric {

    public static final String COUNT_FILE_NAME = "compare_openings_count.data";
    public static final String PERCENTAGE_FILE_NAME = "compare_openings_percentage.data";
    private Map<String, Result> results = new HashMap<String, Result>();

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        if (game.getWhitePlayer().getTurns() < 4 || game.getBlackPlayer().getTurns() < 4) return;

        String whiteOpening = getOpening(game.getWhitePlayer(), game);
        String blackOpening = getOpening(game.getBlackPlayer(), game);

        String key = whiteOpening + blackOpening;

        if (!results.containsKey(key)) {
            Result result = new Result();
            result.whiteOpening = whiteOpening;
            result.blackOpening = blackOpening;
            results.put(key, result);
        }

        Result r = results.get(key);

        GameStatus status = game.getStatus();
        switch(status) {

            case RESULT_MATCH_IN_PROGRESS:
            case RESULT_TURN_LIMIT_REACHED:
            case RESULT_TIME_LIMIT_REACHED:
            case RESULT_NOT_STARTED:
                r.others++;
                break;
            case RESULT_DRAW:
                r.draws++;
                break;
            case RESULT_BLACK_WINS:
                r.blackWins++;
                break;
            case RESULT_WHITE_WINS:
                r.whiteWins++;
                break;
        }
    }

    private String getOpening(Player player, Game game) {

        StringBuilder sb = new StringBuilder();

        // Collect first 4 moves from player
        for (int i = 1; i <= 4; i++) {
            GameCommand command = game.getMove(player, i);
            if (i > 1) {
                sb.append("-");
            }

            if (command.getToken() != null) {
                sb.append(command.getToken().getId());
            } else {
                sb.append("PASS");
            }
        }

        return sb.toString();
    }

    @Override
    public void save() {
        saveCount();
        savePercentage();
    }

    private void saveCount() {
        String[][] file = new String[results.size() + 1][5];
        file[0] = new String[] { "White", "WhiteWin", "Draw", "BlackWin", "Black"};

        Object[] keys = new ArrayList<Result>(results.values()).toArray();
        Arrays.sort(keys);

        int row = 1;
        for (int i = keys.length - 1; i >= 0; i--) {
            Result r = (Result) keys[i];
            file[row][0] = r.whiteOpening;
            file[row][1] = Integer.toString(r.whiteWins);
            file[row][2] = Integer.toString(r.draws);
            file[row][3] = Integer.toString(r.blackWins);
            file[row][4] = r.blackOpening;
            row++;
        }

        saveStringToDisc(COUNT_FILE_NAME, matrixToString(file));
    }

    private void savePercentage() {
        String[][] file = new String[results.size() + 1][5];
        file[0] = new String[] { "White", "WhiteWin", "Draw", "BlackWin", "Black"};

        Object[] keys = new ArrayList<Result>(results.values()).toArray();
        Arrays.sort(keys);

        int row = 1;
        for (int i = keys.length - 1; i >= 0; i--) {
            Result r = (Result) keys[i];
            file[row][0] = r.whiteOpening;
            file[row][1] = r.total() > 0 ? Float.toString(100*r.whiteWins/(float)r.total()) : "0.0";
            file[row][2] = r.total() > 0 ? Float.toString(100*r.draws/(float)r.total()) : "0.0";
            file[row][3] = r.total() > 0 ? Float.toString(100*r.blackWins/(float)r.total()) : "0.0";
            file[row][4] = r.blackOpening;
            row++;
        }

        saveStringToDisc(PERCENTAGE_FILE_NAME, matrixToString(file));
    }

    private class Result implements Comparable {
        public String whiteOpening;
        public String blackOpening;
        public int blackWins = 0;
        public int whiteWins = 0;
        public int draws = 0;
        public int others = 0;

        public int total() {
            return whiteWins + blackWins + draws;
        }

        public String key() {
            return whiteOpening + blackOpening;
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof Result) || o == null) return 1;

            Result r2 = (Result) o;
            if (total() == r2.total()) {
                return key().compareTo(r2.key());
            } else {
                if (total() < r2.total()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }
}
