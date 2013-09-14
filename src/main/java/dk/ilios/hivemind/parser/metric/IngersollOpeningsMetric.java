package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.game.GameStatus;
import dk.ilios.hivemind.model.BugType;
import dk.ilios.hivemind.parser.BoardspaceGameType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Staticstics for openings defined by Randy Ingersoll in "Play Hive like a Champion"
 *
 * Win/Loose/Draw/Other is tracked for each opening type: C, Z, I, J, F, X
 *
 */
public class IngersollOpeningsMetric extends Metric {

    public static final String COUNT_FILE_NAME = "game_openings_vs_results_count.data";
    public static final String PERCENTAGE_FILE_NAME = "game_openings_vs_results_type.data";

    private Map<String, Result> results = new HashMap<String, Result>();

    public IngersollOpeningsMetric() {
        results.put("C", new Result());
        results.put("Z", new Result());
        results.put("I", new Result());
        results.put("J", new Result());
        results.put("F", new Result());
        results.put("X", new Result());
        results.put("?", new Result());
    }

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        game.setReplayMode(true);

        String openingType = getOpeningType(game);
        GameStatus status = game.getStatus();
        switch (status) {
            case RESULT_DRAW:
                results.get(openingType).draw++;
                break;

            case RESULT_BLACK_WINS:
                results.get(openingType).blackWin++;
                break;

            case RESULT_WHITE_WINS:
                results.get(openingType).whiteWin++;
                break;

            default:
                results.get(openingType).other++;
        }

    }

    private String getOpeningType(Game game) {
        game.forward();
        game.forward();
        GameCommand whiteTurn2 = game.forward();
        GameCommand blackTurn2 = game.forward();

        if (whiteTurn2 == null || blackTurn2 == null) {
            return "?";

        } else if (whiteTurn2.getToken().getOriginalType() == BugType.QUEEN_BEE && blackTurn2.getToken().getOriginalType() == BugType.QUEEN_BEE) {
            int[] wqCoords = game.getBoard().getSPCoordinatesFor(whiteTurn2.getToken().getHex());
            int[] bqCoords = game.getBoard().getSPCoordinatesFor(blackTurn2.getToken().getHex());

            if (bqCoords[0] == 2 && bqCoords[1] == 0) {
                return "C";
            } else if ((bqCoords[0] == 1 && bqCoords[1] == 2) || (bqCoords[0] == 3 && bqCoords[1] == -2)) {
                return "Z";
            } else if (bqCoords[0] == 3 && bqCoords[1] == 0) {
                return "I";
            } else if (bqCoords[0] == 3 && bqCoords[1] == -1) {
                return "J";
            } else if (bqCoords[0] == 2 && bqCoords[1] == 1) {
                return "F";
            } else {
                System.out.println("Could not determine opening: " + Arrays.toString(wqCoords) + "/" + Arrays.toString(bqCoords) + "\n" + printer.toString(game.getBoard()));
                return "?";
            }
        } else {
            return "X";
        }
    }

    @Override
    public void save() {
        savePercentage();
        saveCount();
    }

    private void saveCount() {
        StringBuilder sb = new StringBuilder();
        StringBuilder labels = new StringBuilder("label");
        StringBuilder white = new StringBuilder("white-wins");
        StringBuilder black = new StringBuilder("black-wins");
        StringBuilder draw = new StringBuilder("draw");
        StringBuilder other = new StringBuilder("other");

        Object[] keys = new ArrayList<String>(results.keySet()).toArray();
        Arrays.sort(keys);

        for (Object key : keys) {
            Result r = results.get(key);
            labels.append('\t').append(key);
            white.append('\t').append(r.whiteWin);
            black.append('\t').append(r.blackWin);
            draw.append('\t').append(r.draw);
            other.append('\t').append(r.other);
        }

        sb.append(labels.toString()).append('\n');
        sb.append(white.toString()).append('\n');
        sb.append(black.toString()).append('\n');
        sb.append(draw.toString()).append('\n');
        sb.append(other.toString()).append('\n');

        saveStringToDisc(COUNT_FILE_NAME, sb.toString());
    }

    private void savePercentage() {
        StringBuilder sb = new StringBuilder();
        StringBuilder labels = new StringBuilder("label");
        StringBuilder white = new StringBuilder("white-wins");
        StringBuilder black = new StringBuilder("black-wins");
        StringBuilder draw = new StringBuilder("draw");
        StringBuilder other = new StringBuilder("other");

        Object[] keys = new ArrayList<String>(results.keySet()).toArray();
        Arrays.sort(keys);

        for (Object key : keys) {
            Result r = results.get(key);
            labels.append('\t').append(key);
            white.append('\t').append((r.getTotal() > 0) ? r.whiteWin*100/r.getTotal() : "0.0");
            black.append('\t').append((r.getTotal() > 0) ? r.blackWin*100/r.getTotal() : "0.0");
            draw.append('\t').append((r.getTotal() > 0) ? r.draw*100/r.getTotal() : "0.0");
            other.append('\t').append((r.getTotal() > 0) ? r.other*100/r.getTotal() : "0.0");
        }

        sb.append(labels.toString()).append('\n');
        sb.append(white.toString()).append('\n');
        sb.append(black.toString()).append('\n');
        sb.append(draw.toString()).append('\n');
        sb.append(other.toString()).append('\n');

        saveStringToDisc(PERCENTAGE_FILE_NAME, sb.toString());
    }

    private class Result {
        public int whiteWin = 0;
        public int blackWin = 0;
        public int draw = 0;
        public int other = 0;

        public float getTotal() {
            return (float) whiteWin + blackWin + draw + other;
        }
    }


}
