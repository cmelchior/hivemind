package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameStatus;
import dk.ilios.hivemind.parser.BoardspaceGameType;

import java.util.*;

/**
 * Tracks how many turns a player has before the game is over.
 */
public class GameDurationTurnsMetric extends Metric {

    public static final String FILE_NAME = "game_turns.data";
    private static final String ALL_KEY = "all";

    private int maxTurn = Integer.MIN_VALUE;
    private int minTurn = Integer.MAX_VALUE;
    private Map<String, ArrayList<Integer>> results = new HashMap<String, ArrayList<Integer>>();

    public GameDurationTurnsMetric() {
        results.put(ALL_KEY, new ArrayList<Integer>());
    }

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        String variantKey = ALL_KEY + "-" + type.getPrefix();
        String groupKey = type.getPrefix() + "-" + variant;

        if (!results.containsKey(variantKey)) {
            results.put(variantKey, new ArrayList<Integer>());
        }

        if (!results.containsKey(groupKey)) {
            results.put(groupKey, new ArrayList<Integer>());
        }

        // Only consider games we know has ended
        GameStatus status = game.getStatus();
        if (status == GameStatus.RESULT_WHITE_WINS || status == GameStatus.RESULT_BLACK_WINS || status == GameStatus.RESULT_DRAW) {

            int lastPlayerTurns = game.getOtherPlayer().getTurns(); // As we toggle player after each move. Toggle back to get last player to move.
            maxTurn = Math.max(lastPlayerTurns, maxTurn);
            minTurn = Math.min(lastPlayerTurns, minTurn);

            ensureSize(results.get(ALL_KEY), lastPlayerTurns + 1);
            ensureSize(results.get(variantKey), lastPlayerTurns + 1);
            ensureSize(results.get(groupKey), lastPlayerTurns + 1);

            int currentAllCounter = results.get(ALL_KEY).get(lastPlayerTurns);
            int currentVariantCounter = results.get(variantKey).get(lastPlayerTurns);
            int currentGroupCounter = results.get(groupKey).get(lastPlayerTurns);

            results.get(ALL_KEY).set(lastPlayerTurns, currentAllCounter + 1);
            results.get(variantKey).set(lastPlayerTurns, currentVariantCounter + 1);
            results.get(groupKey).set(lastPlayerTurns, currentGroupCounter + 1);
        }
    }

    @Override
    public void save() {
        String[][] file = new String[maxTurn - minTurn + 2][results.size() + 1]; // [y][x]

        // Set labels
        file[0][0] = "turn";
        for (int i = minTurn; i <= maxTurn; i++) {
            file[i - minTurn + 1][0] = Integer.toString(i);
        }

        // Fill in values
        Object[] keys = new ArrayList<String>(results.keySet()).toArray();
        Arrays.sort(keys);

        int col = 1;
        for (Object key : keys) {
            List<Integer> gameEndList = results.get(key);
            file[0][col] = (String) key;
            for (int turn = minTurn; turn <= maxTurn; turn++) {
                file[turn - minTurn + 1][col] = gameEndList.size() > turn ? Integer.toString(gameEndList.get(turn)) : "0";
            }
            col++;
        }

        saveStringToDisc(FILE_NAME, matrixToString(file));
    }
}
