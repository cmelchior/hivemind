package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameStatus;
import dk.ilios.hivemind.parser.BoardspaceGameType;

import java.util.*;

/**
 * Tracks how long games last.
 */
public class GameDurationTimeMetric extends Metric {

    public static final String FILE_NAME = "game_time.data";
    public static final String TURN_FILE_NAME = "game_time_pr_turn.data";
    private static final String ALL_KEY = "all";

    private int maxTurn = Integer.MIN_VALUE;
    private int minTurn = Integer.MAX_VALUE;
    private Map<String, Result> games = new HashMap<String, Result>();

    public GameDurationTimeMetric() {
        games.put(ALL_KEY, new Result());
    }

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        String variantKey = ALL_KEY + "-" + type.getPrefix();
        String groupKey = type.getPrefix() + "-" + variant;

        if (!games.containsKey(variantKey)) {
            games.put(variantKey, new Result());
        }

        if (!games.containsKey(groupKey)) {
            games.put(groupKey, new Result());
        }

        // Only consider games we know has ended
        GameStatus status = game.getStatus();
        if (status == GameStatus.RESULT_WHITE_WINS || status == GameStatus.RESULT_BLACK_WINS || status == GameStatus.RESULT_DRAW) {

            long gameTime = game.getWhitePlayer().getPlayTime() + game.getBlackPlayer().getPlayTime();
            long turns = game.getWhitePlayer().getTurns() + game.getBlackPlayer().getTurns();

            games.get(ALL_KEY).games += 1;
            games.get(variantKey).games += 1;
            games.get(groupKey).games += 1;

            games.get(ALL_KEY).time += gameTime;
            games.get(variantKey).time += gameTime;
            games.get(groupKey).time += gameTime;

            games.get(ALL_KEY).turns += turns;
            games.get(variantKey).turns += turns;
            games.get(groupKey).turns += turns;
        }
    }

    @Override
    public void save() {
        saveStandard();
        savePrTurn();
    }

    private void savePrTurn() {
        String[][] file = new String[games.size() + 1][2]; // [y][x]

        // Set labels
        file[0] = new String[] {"Variant" , "Time (s.)" };

        // Fill in values
        Object[] keys = new ArrayList<String>(games.keySet()).toArray();
        Arrays.sort(keys);

        int row = 1;
        for (Object key : keys) {
            Result r = games.get(key);
            file[row][0] = (String) key;
            file[row][1] = Long.toString((r.time/(r.turns * 1000)));
            row++;
        }

        saveStringToDisc(TURN_FILE_NAME, matrixToString(file));
    }

    private void saveStandard() {
        String[][] file = new String[games.size() + 1][2]; // [y][x]

        // Set labels
        file[0] = new String[] {"Variant" , "Time (s.)" };

        // Fill in values
        Object[] keys = new ArrayList<String>(games.keySet()).toArray();
        Arrays.sort(keys);

        int row = 1;
        for (Object key : keys) {
            Result r = games.get(key);
            file[row][0] = (String) key;
            file[row][1] = Long.toString((r.time/(r.games * 1000)));
            row++;
        }

        saveStringToDisc(FILE_NAME, matrixToString(file));
    }

    private class Result {
        public long games = 0;
        public long time = 0;
        public long turns = 0;
    }
}
