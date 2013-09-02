package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameStatus;
import dk.ilios.hivemind.model.Player;
import dk.ilios.hivemind.parser.BoardspaceGameType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * How many bugs are on the board at the end of the game?
 */
public class BugsOnBoardAtGameEndMetric extends Metric {

    public static final String FILE_NAME = "bugs_on_board_at_end.data";
    public static final String DIFF_FILE_NAME = "bugs_on_board_at_end_diff.data";


    private Map<Integer, Result> results = new HashMap<Integer, Result>();
    private Map<Integer, Integer> diffResults = new HashMap<Integer, Integer>();
    private int diffTotal = 0;

    public BugsOnBoardAtGameEndMetric() {
        results.put(11, new Result());
        results.put(12, new Result());
        results.put(13, new Result());
        results.put(14, new Result());
    }

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        GameStatus status = game.getStatus();
        if (!status.winnerFound()) return;
        int winBoard = -1;
        int looseBoard = -1;
        Player black = game.getBlackPlayer();
        Player white = game.getWhitePlayer();
        if (status == GameStatus.RESULT_BLACK_WINS) {
            winBoard = black.getNoStartingBugs() - black.getSupply().size();
            looseBoard = white.getNoStartingBugs() - white.getSupply().size();
        } else if (status == GameStatus.RESULT_WHITE_WINS) {
            winBoard = white.getNoStartingBugs() - white.getSupply().size();
            looseBoard = black.getNoStartingBugs() - black.getSupply().size();
        }

        int startingBugs = game.getActivePlayer().getNoStartingBugs();
        results.get(startingBugs).winner[winBoard] = results.get(startingBugs).winner[winBoard] + 1;
        results.get(startingBugs).looser[looseBoard] = results.get(startingBugs).looser[looseBoard] + 1;

        int diff = winBoard - looseBoard;
        if (diffResults.containsKey(diff)) {
            diffResults.put(diff, diffResults.get(diff) + 1);
        }  else {
            diffResults.put(diff, 1);
        }
        diffTotal++;
    }

    @Override
    public void save() {
        saveStandard();
        saveDiff();
    }

    private void saveStandard() {
        String[][] file = new String[16][9]; // [y][x]

        // Set labels
        file[0] = new String[] { "Board", "Winner(11)", "Looser(11)", "Winner(12)", "Looser(12)", "Winner(13)", "Looser(13)", "Winner(14)", "Looser(14)" };

        for (int i = 11; i <= 14; i++) {
            Result r = results.get(i);
            int[] winner = r.winner;
            int[] looser = r.looser;

            for (int j = 0; j < winner.length; j++) {
                file[j + 1][0] = Integer.toString(j); // Just override for now
                file[j + 1][(i - 11)*2 + 1] = Integer.toString(winner[j]);
                file[j + 1][(i - 11)*2 + 2] = Integer.toString(looser[j]);
            }
        }

        saveStringToDisc(FILE_NAME, matrixToString(file));
    }

    private void saveDiff() {
        String[][] file = new String[diffResults.size() + 1][3]; // [y][x]

        // Set labels
        file[0] = new String[] { "Diff", "Count", "Percent" };

        // Fill in values
        Object[] keys = new ArrayList<Integer>(diffResults.keySet()).toArray();
        Arrays.sort(keys);

        for (int i = 0; i < keys.length; i++) {
            file[i + 1][0] = Integer.toString((Integer) keys[i]);
            file[i + 1][1] = Integer.toString(diffResults.get(keys[i]));
            file[i + 1][2] = Float.toString(diffResults.get(keys[i])*100/(float)diffTotal);
        }

        saveStringToDisc(DIFF_FILE_NAME, matrixToString(file));
    }

    private class Result {
        public int[] winner = new int[15];
        public int[] looser = new int[15];
    }
}
