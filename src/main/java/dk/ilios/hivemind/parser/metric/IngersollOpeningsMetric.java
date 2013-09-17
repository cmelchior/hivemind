package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.game.GameStatus;
import dk.ilios.hivemind.model.*;
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
    public static final String PERCENTAGE_FILE_NAME = "game_openings_vs_results_percentage.data";

    private Map<String, Result> results = new HashMap<String, Result>();

    public IngersollOpeningsMetric() {
        results.put("C", new Result());
        results.put("Z", new Result());
        results.put("I", new Result());
        results.put("J", new Result());
        results.put("F", new Result());
        results.put("X", new Result());
        results.put("?", new Result());
        results.put("%", new Result());
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

        // Forward game until both queens are placed.
        int i = 10;
        while ((game.getWhitePlayer().getQueen().inSupply() || game.getBlackPlayer().getQueen().inSupply()) && i > 0) {
            game.forward();
            i--;
        }

        if ((game.getWhitePlayer().getQueen().inSupply() || game.getBlackPlayer().getQueen().inSupply())) {
            return "%";
        }

        if (isC(game)) {
            return "C";
        } else if (isZ(game)) {
            return "Z";
        } else if (isI(game)) {
            return "I";
        } else if (isJ(game)) {
            return "J";
        } else if (isF(game)) {
            return "F";
        } else if (isX(game)) {
            return "X";
        } else {
           return "?";
        }
    }

    private boolean isX(Game game) {
        Hex startingWhiteHex = game.getMove(game.getWhitePlayer(), 1).getToken().getHex();
        Hex whiteQueenHex = game.getWhitePlayer().getQueen().getHex();
        Hex startingBlackHex = game.getMove(game.getBlackPlayer(), 1).getToken().getHex();
        Hex blackQueenHex = game.getBlackPlayer().getQueen().getHex();

        if (HexagonUtils.distance(startingWhiteHex.getQ(), startingWhiteHex.getR(), whiteQueenHex.getQ(), whiteQueenHex.getR()) > 1
                || HexagonUtils.distance(startingBlackHex.getQ(), startingBlackHex.getR(), blackQueenHex.getQ(), blackQueenHex.getR()) > 1) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isF(Game game) {
        return checkTokens(1,0, 2,-1, 3,-2, game.getBoard()) || checkTokens(1,-1, 2,-1, 3,-1, game.getBoard());
    }

    private boolean isJ(Game game) {
        return checkTokens(1,0, 2,0, 3,-1, game.getBoard()) || checkTokens(1,-1, 2,-2, 3,-2, game.getBoard());
    }

    private boolean isI(Game game) {
        return checkTokens(1,0, 2,0, 3,0, game.getBoard());
    }

    private boolean isZ(Game game) {
        return checkTokens(1,0, 2,-1, 3,-1, game.getBoard()) || checkTokens(1,-1, 2,-1, 3,-2, game.getBoard());
    }

    private boolean isC(Game game) {
        return checkTokens(1,-1, 2,-1, 2,0, game.getBoard()) || checkTokens(0,1, 1,1, 2,0, game.getBoard());
   }

    private boolean checkTokens(int q1, int r1, int q2, int r2, int q3, int r3, Board b) {
        Token t1 = b.getHexForStandardPosition(q1, r1).getTopToken();
        Token t2 = b.getHexForStandardPosition(q2, r2).getTopToken();
        Token t3 = b.getHexForStandardPosition(q3, r3).getTopToken();

        if (t1 == null || t2 == null || t3 == null) {
            return false;
        }

        if (!t1.getPlayer().isWhitePlayer()) {
            return false;
        }

        if (!t2.getPlayer().isBlackPlayer()) {
            return false;
        }

        if (!t3.getPlayer().isBlackPlayer() || t3.getOriginalType() != BugType.QUEEN_BEE) {
            return false;
        }

        return true;
    }

    @Override
    public void save() {
        results.remove("%");
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
