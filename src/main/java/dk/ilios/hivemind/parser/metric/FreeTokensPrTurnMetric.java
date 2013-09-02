package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameStatus;
import dk.ilios.hivemind.model.Player;
import dk.ilios.hivemind.model.rules.Rules;
import dk.ilios.hivemind.parser.BoardspaceGameType;

/**
 * Keeps track of the difference between free tokens between winner and looser.
 * Free tokens are counted at the beginning of each of the winners turns.
 */
public class FreeTokensPrTurnMetric extends Metric {

    public static final String WHITE_FILE_NAME = "white_winner_free_tokens_during_games_%s_turns.data";
    public static final String BLACK_FILE_NAME = "black_winner_free_tokens_during_games_%s_turns.data";

    int[][] whiteData; // [y][x]
    int[][] blackData; // [y][x]
    int gamesWithWinnerTurns; // Only look at games where winner had that many turns

    public FreeTokensPrTurnMetric(int winnerTurns) {
        gamesWithWinnerTurns = winnerTurns;
        whiteData = new int[gamesWithWinnerTurns][29]; // [y][x] : from -14 to + 14 and and 1 - 16
        blackData = new int[gamesWithWinnerTurns][29]; // [y][x] : from -14 to + 14 and and 1 - 16
    }

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        GameStatus status = game.getStatus();
        Player p = null;
        int[][] data = null;
        if (status == GameStatus.RESULT_BLACK_WINS && game.getBlackPlayer().getTurns() == gamesWithWinnerTurns) {
            data = blackData;
            p = game.getBlackPlayer();
            game.setReplayMode(true);
            game.forward();
        } else if (status == GameStatus.RESULT_WHITE_WINS && game.getWhitePlayer().getTurns() == gamesWithWinnerTurns) {
            data = whiteData;
            p = game.getWhitePlayer();
            game.setReplayMode(true);
        } else {
            return;
        }

        for (int i = 0; i < gamesWithWinnerTurns; i++) {
            int winnerFreeTokens = Rules.getInstance().getFreeTokens(game.getActivePlayer(), game.getBoard()).size();
            int looserFreeTokens = Rules.getInstance().getFreeTokens(game.getOtherPlayer(), game.getBoard()).size();
            int diff = winnerFreeTokens - looserFreeTokens;
            data[i][diff + 14] = data[i][diff + 14] + 1;

            if (i < gamesWithWinnerTurns - 1) {
                game.forward();
                game.forward();
            }
        }
    }

    @Override
    public void save() {
        save(WHITE_FILE_NAME, whiteData);
        save(BLACK_FILE_NAME, blackData);
    }

    private void save(String fileName, int[][] data) {
        int leftBound = data[0].length - 1;
        int rightBound = 0;

        for(int y = 0; y < data.length; y++) {
            for(int x = 0; x < data[y].length; x++) {
                if (data[y][x] > 0) {
                    leftBound = Math.min(leftBound, x);
                    rightBound = Math.max(rightBound, x);
                }
            }
        }

        String file[][] = new String[data.length + 1][rightBound - leftBound + 2];

        // Labels
        file[0][0] = "Turns";
        for (int i = leftBound; i <= rightBound; i++) {
            file[0][i - leftBound + 1] = Integer.toString(i - 14);
        }

        for (int y = 0; y < data.length; y++) {
            file[y + 1][0] = Integer.toString(y + 1);
            for (int x = leftBound; x <= rightBound; x++) {
                file[y + 1][x - leftBound + 1] = Integer.toString(data[y][x]);
            }
        }

        saveStringToDisc(String.format(fileName, gamesWithWinnerTurns), matrixToString(file));
    }
}
