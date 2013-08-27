package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameStatus;
import dk.ilios.hivemind.model.Player;
import dk.ilios.hivemind.model.rules.Rules;
import dk.ilios.hivemind.parser.BoardspaceGameType;

/**
 * Keeps track of tokens around queens. Number if counted after player has moved.
 */
public class TokensAroundQueenMetric extends Metric {

    public static final String FILE_NAME = "tokens_around_queen_%s_turns.data";
    public static final String WINNER_FILE_NAME = "winner_tokens_around_queen_%s_turns.data";
    public static final String LOOSER_FILE_NAME = "looser_tokens_around_queen_%s_turns.data";

    int[][] winnerData; // [y][x]
    int[][] looserData; // [y][x]
    int gamesWithWinnerTurns; // Only look at games where winner had that many turns

    public TokensAroundQueenMetric(int winnerTurns) {
        gamesWithWinnerTurns = winnerTurns;
        winnerData = new int[gamesWithWinnerTurns][7]; // [y][x] : from -14 to + 14 and and 1 - 16
        looserData = new int[gamesWithWinnerTurns ][7]; // [y][x] : from -14 to + 14 and and 1 - 16
    }

    @Override
    public void analyzeGame(BoardspaceGameType type, String variant, Game game) {
        GameStatus status = game.getStatus();
        Player winner;
        Player looser;
        if (status == GameStatus.RESULT_BLACK_WINS && game.getBlackPlayer().getTurns() == gamesWithWinnerTurns) {
            winner = game.getBlackPlayer();
            looser = game.getWhitePlayer();
        } else if (status == GameStatus.RESULT_WHITE_WINS && game.getWhitePlayer().getTurns() == gamesWithWinnerTurns) {
            winner = game.getWhitePlayer();
            looser = game.getBlackPlayer();
        } else {
            return;
        }

        game.setReplayMode(true);
        Player lastActive = null;
        for (int i = 0; i < gamesWithWinnerTurns * 2; i++) {
            int turn = i / 2;
            game.forward();
            if (lastActive != null && game.getActivePlayer() == lastActive) {
                game.togglePlayer(); // End of game
            }

            if (game.getActivePlayer() == looser) {
                int winnerTokensAroundQueen = game.getBoard().getNeighborTokens(looser.getQueen().getHex()).size();
                winnerData[turn][winnerTokensAroundQueen] += 1;
            } else if (game.getActivePlayer() == winner) {
                int looserTokensAroundQueen = game.getBoard().getNeighborTokens(winner.getQueen().getHex()).size();
                looserData[turn][looserTokensAroundQueen] += 1;
            }
            lastActive = game.getActivePlayer();
        }
    }

    @Override
    public void save() {
        save(WINNER_FILE_NAME, winnerData);
        save(LOOSER_FILE_NAME, looserData);
        saveAverages();
    }

    private void saveAverages() {
        String file[][] = new String[winnerData.length + 1][3];
        file[0] = new String[] { "Turns", "Winner", "Looser" };

        for (int row = 0; row < winnerData.length; row++) {
            file[row + 1][0] = Integer.toString(row + 1);

            float total = 0;
            float games = 0;
            for (int x = 0; x < winnerData[row].length; x++) {
                games += winnerData[row][x];
                total += x * winnerData[row][x];
            }
            file[row + 1][1] = Float.toString(total/games);

            total = 0;
            games = 0;
            for (int x = 0; x < looserData[row].length; x++) {
                games += looserData[row][x];
                total += x * looserData[row][x];
            }
            file[row + 1][2] = Float.toString(total/games);
        }

        saveStringToDisc(String.format(FILE_NAME, gamesWithWinnerTurns), matrixToString(file));
    }

    private void save(String fileName, int[][] data) {
        String file[][] = new String[data.length + 1][8];

        // Labels
        file[0][0] = "Turns";
        for (int i = 1; i < file[0].length; i++) {
            file[0][i] = Integer.toString(i - 1);
        }

        for (int y = 0; y < data.length; y++) {
            file[y + 1][0] = Integer.toString(y + 1);
            for (int x = 0; x < data[y].length; x++) {
                file[y + 1][x + 1] = Integer.toString(data[y][x]);
            }
        }

        saveStringToDisc(String.format(fileName, gamesWithWinnerTurns), matrixToString(file));
    }
}
