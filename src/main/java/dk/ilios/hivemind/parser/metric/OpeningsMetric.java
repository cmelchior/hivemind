package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.parser.BoardspaceGameType;

import java.util.*;

/**
 * Analyzes openings, ie. the first 4 moves for White player
 */
public class OpeningsMetric extends Metric {

    public static final String WHITE_FILE_NAME = "white_openings.data";
    public static final String BLACK_FILE_NAME = "black_openings.data";
    private Map<String, Result> whiteResults = new HashMap<String, Result>();
    private Map<String, Result> blackResults = new HashMap<String, Result>();
    private int whiteTotal = 0;
    private int blackTotal = 0;

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        analyzeWhite(type, variant, game);
        analyzeBlack(type, variant, game);
    }

    private void analyzeBlack(BoardspaceGameType type, String variant, Game game) {
        if (game.getBlackPlayer().getMoves() < 4) return;
        StringBuilder sb = new StringBuilder();

        // Collect first 4 moves from white
        for (int i = 1; i <= 4; i++) {
            GameCommand command = game.getMove(game.getBlackPlayer(), i);
            if (i > 1) {
                sb.append("-");
            }
            if (command.getToken() == null) {
                sb.append("??");
            }  else {
                sb.append(command.getToken().getId());
            }
        }

        String opening  = sb.toString();
        if (!blackResults.containsKey(opening)) {
            blackResults.put(opening, new Result(opening, 1));
        } else {
            Result r = blackResults.get(opening);
            r.count += 1;
            blackResults.put(opening, r);
        }

        blackTotal++;
    }

    private void analyzeWhite(BoardspaceGameType type, String variant, Game game) {
        if (game.getWhitePlayer().getMoves() < 4) return;
        StringBuilder sb = new StringBuilder();

        // Collect first 4 moves from white
        for (int i = 1; i <= 4; i++) {
            GameCommand command = game.getMove(game.getWhitePlayer(), i);
            if (i > 1) {
                sb.append("-");
            }
            if (command.getToken() == null) {
                sb.append("??");
            }  else {
                sb.append(command.getToken().getId());
            }
        }

        String opening  = sb.toString();
        if (!whiteResults.containsKey(opening)) {
            whiteResults.put(opening, new Result(opening, 1));
        } else {
            Result r = whiteResults.get(opening);
            r.count += 1;
            whiteResults.put(opening, r);
        }

        whiteTotal++;
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
