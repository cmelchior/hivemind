package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.game.GameStatus;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.StandardPositionMode;
import dk.ilios.hivemind.parser.BoardspaceGameType;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Compare all opening variants when looking at the first 3 tokens for both sides. No moves are allowed.
 */
public class LinaelOpeningComparison extends Metric {

    private final static String FILE_NAME = "linael_opening_comparison.data";
    private final static String FILE_NAME_WHITE = "linael_white_opening_comparison.data";
    private final static String FILE_NAME_BLACK = "linael_black_opening_comparison.data";

    private HashMap<String, Result> allResults = new HashMap<String, Result>();
    private HashMap<String, Result> whiteResults = new HashMap<String, Result>();
    private HashMap<String, Result> blackResults = new HashMap<String, Result>();
    private int totalGames = 0;

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        if (game.getWhitePlayer().getTurns() < 3 || game.getBlackPlayer().getTurns() < 3) return; // Both players must have had 3 turns

        GameStatus status = game.getStatus();
        game.setReplayMode(true);
        game.setStandardPositionMode(StandardPositionMode.LIMITED);
        Hex[] hexes = new Hex[6];
        Hex[] whiteHexes = new Hex[3];
        Hex[] blackHexes = new Hex[3];
        for (int i = 0; i < 6; i++) {
            GameCommand gc = game.forward();
            if (gc.getFromQ() != Hex.SUPPLY) return; // We only care about games where players put tokens on the board for the first 3 turns.
            int[] spCoords = game.getBoard().getSPCoordinatesFor(gc.getToQ(), gc.getToR());
            hexes[i] = new Hex(spCoords[0], spCoords[1]);
            if (gc.getToken().getPlayer().isWhitePlayer()) {
                whiteHexes[i/2] = new Hex(spCoords[0], spCoords[1]);
            } else {
                blackHexes[i/2] = new Hex(spCoords[0], spCoords[1]);
            }
        }

        String allKey = createKey(hexes);
        String whiteKey = createKey(whiteHexes);
        String blackKey = createKey(blackHexes);

        saveResults(allKey, status, allResults, game);
        saveResults(whiteKey, status, whiteResults, game);
        saveResults(blackKey, status, blackResults, game);

        totalGames++;
    }

    private void saveResults(String key, GameStatus status, HashMap<String, Result> results, Game game) {
        Result result = results.get(key);
        if (result == null) {
            String example = printer.toString(game.getBoard());
            System.out.println((results.size() + 1) + ". " +  key);
            System.out.println(example);
            result = new Result(example);
            results.put(key, result);
        }

        switch(status) {
            case RESULT_NOT_STARTED:
            case RESULT_MATCH_IN_PROGRESS:
            case RESULT_TURN_LIMIT_REACHED:
            case RESULT_TIME_LIMIT_REACHED:
                result.other++;
                break;
            case RESULT_DRAW:
                result.draw++;
                break;
            case RESULT_BLACK_WINS:
                result.blackWin++;
                break;
            case RESULT_WHITE_WINS:
                result.whiteWin++;
                break;
        }
    }


    private String createKey(Hex[] hexes) {
        Arrays.sort(hexes);
        StringBuilder key = new StringBuilder();
        for (Hex hex : hexes) {
            key.append('(');
            key.append(hex.getQ());
            key.append(',');
            key.append(hex.getR());
            key.append(')');
        }
        return key.toString();
    }

    @Override
    public void save() {
        save(FILE_NAME, allResults);
        save(FILE_NAME_WHITE, whiteResults);
        save(FILE_NAME_BLACK, blackResults);
    }

    private void save(String fileName, HashMap<String, Result> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("Total games: " + totalGames + "\n");
        sb.append("WhiteWin\tBlackWin\tDraw\tOther\tTotal\n");
        sb.append("---------------------------\n");
        for (Result result : results.values()) {
            sb.append(result.example);
            sb.append('\n');
            sb.append(Integer.toString(result.whiteWin) + '\t' + Integer.toString(result.blackWin) + '\t' + Integer.toString(result.draw) + '\t' + Integer.toString(result.other) + '\t' + Integer.toString(((int) result.total()))  + '\n');

            String whiteWin = (result.total()) > 0 ? Float.toString(result.whiteWin*100/result.total()) : "0";
            String blackWin = (result.total()) > 0 ? Float.toString(result.blackWin*100/result.total()) : "0";
            String draw = (result.total()) > 0 ? Float.toString(result.draw*100/result.total()) : "0";
            String other = (result.total()) > 0 ? Float.toString(result.other*100/result.total()) : "0";
            sb.append(whiteWin + '\t' + blackWin + '\t' + draw + '\t' + other + '\t' + '\n');

            sb.append("---------------------------\n");
        }
        saveStringToDisc(fileName, sb.toString());
    }

    private class Result {
        public String example;
        public int whiteWin;
        public int blackWin;
        public int draw;
        public int other;

        public Result(String example) {
            this.example = example;
        }

        public float total() {
            float total = whiteWin + blackWin + draw + other;
            return total;
        }
    }
}
