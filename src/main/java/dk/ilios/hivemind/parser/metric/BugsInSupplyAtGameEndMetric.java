package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameStatus;
import dk.ilios.hivemind.parser.BoardspaceGameType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * How many bugs are still left in the supply at the end of the game?
 */
public class BugsInSupplyAtGameEndMetric extends Metric {

    public static final String FILE_NAME = "bugs_in_supply_at_end.data";
    public static final String DIFF_FILE_NAME = "bugs_in_supply_at_end_diff.data";

    private Map<Integer, Result> results = new HashMap<Integer, Result>();
    private Map<Integer, Integer> diffResults = new HashMap<Integer, Integer>();
    private int diffTotal = 0;

    public BugsInSupplyAtGameEndMetric() {
        results.put(11, new Result(11));
        results.put(12, new Result(12));
        results.put(13, new Result(13));
        results.put(14, new Result(14));
    }

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        GameStatus status = game.getStatus();
        if (!status.winnerFound()) return;
        int winSupply = -1;
        int looseSupply = -1;
        if (status == GameStatus.RESULT_BLACK_WINS) {
            winSupply = game.getBlackPlayer().getSupply().size();
            looseSupply = game.getWhitePlayer().getSupply().size();
        } else if (status == GameStatus.RESULT_WHITE_WINS) {
            winSupply = game.getWhitePlayer().getSupply().size();
            looseSupply = game.getBlackPlayer().getSupply().size();
        }

        int startingBugs = game.getActivePlayer().getNoStartingBugs();
        results.get(startingBugs).winner[winSupply] = results.get(startingBugs).winner[winSupply] + 1;
        results.get(startingBugs).looser[looseSupply] = results.get(startingBugs).looser[looseSupply] + 1;

        int diff = winSupply - looseSupply;
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
        file[0] = new String[] { "Supply", "Winner(11)", "Looser(11)", "Winner(12)", "Looser(12)", "Winner(13)", "Looser(13)", "Winner(14)", "Looser(14)" };

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
        String[][] file = new String[diffResults.size() + 2][3]; // [y][x]

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

        public Result(int maxNumber) {

        }
    }
}
