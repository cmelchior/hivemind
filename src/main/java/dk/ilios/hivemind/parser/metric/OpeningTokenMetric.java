package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.parser.BoardspaceGameType;

import java.util.*;

/**
 * What token is the first to be placed by the starting (white) player.
 */
public class OpeningTokenMetric extends Metric {

    private static final boolean DEBUG = false;

    public static final String FILE_NAME = "opening_token.data";
    public static final String WEIGHTED_FILE_NAME = "weighted_opening_token.data";
    public static final String TOTAL_KEY = "Total";
    public static final String[] keys = new String[] {"A", "G", "B", "S", "Q", "M", "L", "P", TOTAL_KEY };

    private Map<String, Integer> results = new HashMap<String, Integer>();
    private Map<Integer, Map<String, Integer>> weightedResults = new HashMap<Integer, Map<String, Integer>>();

    public OpeningTokenMetric() {
        results = constructTokenMap();
        for (int i = 5; i <= 8; i++) {
            weightedResults.put(i, constructTokenMap());
        }
    }

    private Map<String, Integer> constructTokenMap() {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (String key : keys) {
            map.put(key, 0);
        }

        return map;
    }

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        GameCommand c = game.getMove(game.getWhitePlayer(), 1);
        if (c != null) {
            String tokenType = c.getToken().getId().substring(0, 1);
            int tokenTypesNo = getNumberOfDifferentTokens(variant);

            results.put(tokenType, results.get(tokenType) + 1);
            results.put(TOTAL_KEY, results.get(TOTAL_KEY) + 1);
            weightedResults.get(tokenTypesNo).put(tokenType, weightedResults.get(tokenTypesNo).get(tokenType) + 1);
            weightedResults.get(tokenTypesNo).put(TOTAL_KEY, weightedResults.get(tokenTypesNo).get(TOTAL_KEY) + 1);
        } else {
            if (DEBUG) System.out.println("No move found: " + game.getName() + ", turns: " + (game.getWhitePlayer().getTurns() + game.getBlackPlayer().getTurns()));
        }
    }

    private int getNumberOfDifferentTokens(String variant) {
        String[] parts = variant.split("-");
        if (parts.length == 1) {
            return 5;
        } else {
            return 5 + parts[1].length();
        }
    }

    @Override
    public void save() {
        saveNormalResults();
        saveWeightedResults();
    }

    private void saveNormalResults() {
        String[][] file = new String[results.size()][3]; // [y][x]

        // Fill in values
        Object[] keys = new ArrayList<String>(results.keySet()).toArray();
        Arrays.sort(keys);

        // Set labels
        for (int i = 0; i < keys.length; i++) {
            file[i][0] = Integer.toString(i + 1);
            file[i][1] = (String) keys[i];
            file[i][2] = Integer.toString(results.get(keys[i]));
        }

        saveStringToDisc(FILE_NAME, matrixToString(file));
    }

    private void saveWeightedResults() {
        Map<String, Float> output = new HashMap<String, Float>();

        for (String key : keys) {
            float tokenChosen = 0;
            float totalGames = 0;

            for (int i = 5; i <= 8; i++) {
                tokenChosen += weightedResults.get(i).get(key);
                totalGames += weightedResults.get(i).get(TOTAL_KEY);
            }

            output.put(key, tokenChosen / totalGames);
        }

        String[][] file = new String[output.size()][3]; // [y][x]

        // Fill in values
        Object[] keys = new ArrayList<String>(output.keySet()).toArray();
        Arrays.sort(keys);

        // Set labels
        for (int i = 0; i < keys.length; i++) {
            file[i][0] = Integer.toString(i + 1);
            file[i][1] = (String) keys[i];
            file[i][2] = Float.toString(output.get(keys[i]) * 100);
        }

        saveStringToDisc(WEIGHTED_FILE_NAME, matrixToString(file));
    }
}
