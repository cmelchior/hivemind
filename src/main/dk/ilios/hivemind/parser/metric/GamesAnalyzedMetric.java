package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameStatus;
import dk.ilios.hivemind.parser.BoardspaceGameType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of the number of games analyzed in each group.
 */
public class GamesAnalyzedMetric extends Metric {

    public static final String FILE_NAME = "games_analyzed.data";
    private Map<String, Integer> results = new HashMap<String, Integer>();

    private int totalGames = 0;

    public GamesAnalyzedMetric() {
        results.put("all", 0);
    }

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        String variantKey = "all-" + type.getPrefix();
        String groupKey = type.getPrefix() + "-" + variant;

        if (!results.containsKey(variantKey)) {
            results.put(variantKey, 0);
        }

        if (!results.containsKey(groupKey)) {
            results.put(groupKey, 0);
        }

        results.put("all", results.get("all") + 1);
        results.put(variantKey, results.get(variantKey) + 1);
        results.put(groupKey, results.get(groupKey) + 1);

        totalGames++;
    }

    @Override
    public void save() {
        String[][] file = new String[results.size() + 1][4];

        // Labels
        file[0] = new String[] { "Type", "Count", "%" };

        // Add game types in sorted alphabetical order
        Object[] keys = new ArrayList<String>(results.keySet()).toArray();
        Arrays.sort(keys);

        for (int i = 0; i < keys.length; i++) {
            file[i + 1][0] = Integer.toString(i + 1);
            file[i + 1][1] = (String) keys[i];
            file[i + 1][2] = Integer.toString(results.get(keys[i]));
            file[i + 1][3] = Float.toString(results.get(keys[i])*100/(float)totalGames);
        }

        saveStringToDisc(FILE_NAME, matrixToString(file));
    }
}
