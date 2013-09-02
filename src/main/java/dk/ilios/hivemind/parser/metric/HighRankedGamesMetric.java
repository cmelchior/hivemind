package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.parser.BoardspaceGameType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Count how many high ranked games (1600+) exists in each category.
 */
public class HighRankedGamesMetric extends Metric {

    public static final String FILE_NAME = "high_ranked_games.data";
    private Map<String, Result> results = new HashMap<String, Result>();

    public HighRankedGamesMetric() {
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

        if (game.getWhitePlayer().getRanking() >= 0 && game.getBlackPlayer().getRanking() >= 0) {
            allResults.games++;
            variantResults.games++;
            groupResults.games++;
        }
    }

    @Override
    public void save() {
        String[][] file = new String[results.size() + 1][3];

        // Labels
        file[0] = new String[] { "Type", "Count"};

        // Add game types in sorted alphabetical order
        Object[] keys = new ArrayList<String>(results.keySet()).toArray();
        Arrays.sort(keys);

        for (int i = 0; i < keys.length; i++) {
            file[i + 1][0] = Integer.toString(i + 1);
            file[i + 1][1] = (String) keys[i];
            file[i + 1][2] = Integer.toString(results.get(keys[i]).games);
        }

        saveStringToDisc(FILE_NAME, matrixToString(file));
    }

    private class Result {
        public int games = 0;
    }
}
