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

public class WinnerOpeningMetric extends Metric {

    public static final String WHITE_FILE_NAME = "white_winner_openings.data";
    public static final String BLACK_FILE_NAME = "black_winner_openings.data";
    private Map<String, Result> whiteResults = new HashMap<String, Result>();
    private Map<String, Result> blackResults = new HashMap<String, Result>();

    private int whiteTotal = 0;
    private int blackTotal = 0;

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        if (game.getWhitePlayer().getMoves() < 4) return;
        if (game.getStatus() != GameStatus.RESULT_WHITE_WINS && game.getStatus() != GameStatus.RESULT_BLACK_WINS) return;
        StringBuilder sb = new StringBuilder();

        boolean isWhite = (game.getStatus() == GameStatus.RESULT_WHITE_WINS);
        Player p = (isWhite) ? game.getWhitePlayer() : game.getBlackPlayer();

        // Collect first 4 moves from player
        for (int i = 1; i <= 4; i++) {
            GameCommand command = game.getMove(p, i);
            if (i > 1) {
                sb.append("-");
            }
            sb.append(command.getToken().getId());
        }

        String opening  = sb.toString();
        Map<String, Result> map = isWhite ? whiteResults : blackResults;

        if (!map.containsKey(opening)) {
            map.put(opening, new Result(opening, 1));
        } else {
            Result r = map.get(opening);
            r.count += 1;
            map.put(opening, r);
        }

        if (isWhite) {
            whiteTotal++;
        } else {
            blackTotal++;
        }
    }

    @Override
    public void save() {
        save(WHITE_FILE_NAME, whiteResults, whiteTotal);
        save(BLACK_FILE_NAME, blackResults, blackTotal);
    }

    private void save(String fileName, Map<String, Result> results, int total) {
        String[][] file = new String[results.size() + 2][3];
        file[0] = new String[] { "Opening", "Count", "%" };

        Object[] keys = new ArrayList<Result>(results.values()).toArray();
        Arrays.sort(keys);

        int row = 1;
        for (int i = keys.length - 1; i >= 0; i--) {
            Result r = (Result) keys[i];
            file[row][0] = r.key;
            file[row][1] = Integer.toString(r.count);
            file[row][2] = Float.toString(r.count*100/(float) total);
            row++;
        }

        file[row] = new String[] { "Total", Integer.toString(total)};
        saveStringToDisc(fileName, matrixToString(file));
    }

    private class Result implements Comparable {
        public String key;
        public int count;

        public Result(String opening, int count) {
            this.key = opening;
            this.count = count;
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof Result) || o == null) return 1;

            Result r2 = (Result) o;
            if (count == r2.count) {
                return key.compareTo(r2.key);
            } else {
                if (count < r2.count) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }
}
