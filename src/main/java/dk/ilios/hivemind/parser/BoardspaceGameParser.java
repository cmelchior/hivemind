package dk.ilios.hivemind.parser;

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Player;
import dk.ilios.hivemind.model.Token;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for games played at boardgames.net
 */
public class BoardspaceGameParser {

    private final static boolean DEBUG = false;
    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("hh:mm:ss");

    private File gameFile;          // Reference to game file
    private String gameFileContent; // Game file loaded as a string with new lines removed
    private String gameType;        // String describing the game: "hive", "hive-mlp", "hive-p" etc.
    private Game game;              // Reference to actual game object

    private String player1;
    private int player1Turns = 0;
    private long player1GameTime = -1;
    private int player1Ranking = -1;

    private String player2;
    private int player2Turns = 0;
    private long player2GameTime = -1;
    private int player2Ranking = -1;

    private HiveAsciiPrettyPrinter printer = new HiveAsciiPrettyPrinter();

    public BoardspaceGameParser(File game) {
		this.gameFile = game;
        try {
            loadGameData();
        } catch(IOException e) {
           e.printStackTrace(); /* Ignore */
        }
	}

    /**
     * Load basis data from the game file and prepare a game configuration.
     */
    private void loadGameData() throws IOException {

        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(gameFile));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line); // Remove linebreaks, so we later can match moved tokens properly (identified by dropb -> done)

            // Game type
            if (line.startsWith("SU[")) {
                gameType = line.substring(3, line.length() - 1);
                continue;
            }

            // Player 1 (white) name
            if (line.startsWith("P0[id")) {
                player1 = line.substring(7, line.length() - 2);
                continue;
            }

            // Player 2 (black) name
            if (line.startsWith("P1[id")) {
                player2 = line.substring(7, line.length() - 2);
                continue;
            }

            // Player 1 game time
            if (line.startsWith("P0[time")) {
                player1GameTime = parseTime(line.substring(8, line.length() - 2));
                continue;
            }

            // Player 2 game time
            if (line.startsWith("P1[time")) {
                player2GameTime = parseTime(line.substring(8, line.length() - 2));
                continue;
            }

            // Player 1 ranking
            if (line.startsWith("P0[ranking")) {
                player1Ranking = Integer.parseInt(line.substring(11, line.length() - 2));
                continue;
            }

            // Player 2 ranking
            if (line.startsWith("P1[ranking")) {
                player2Ranking = Integer.parseInt(line.substring(11, line.length() - 2));
                continue;
            }
        }
        br.close();

        // Verify that data is loaded
        if (player1 == null || player2 == null || player1GameTime ==  -1 || player2GameTime == -1 || gameType == null) {
            throw new IllegalArgumentException("Game could not be parsed properly: " + gameFile.getAbsolutePath());
        }

        gameFileContent = sb.toString();

        game = new Game();
        game.setName(gameFile.getName());

        // Setup players
        Player white = new Player(player1, Player.PlayerType.WHITE);
        Player black = new Player(player2, Player.PlayerType.BLACK);
        white.addPlayTime(player1GameTime);
        white.setRanking(player1Ranking);
        black.addPlayTime(player2GameTime);
        black.setRanking(player2Ranking);

        white.fillBaseSupply();
        black.fillBaseSupply();

        if (gameType.contains("m")) {
            white.useMosquitoExpansion();
            black.useMosquitoExpansion();
        }

        if (gameType.contains("l")) {
            white.useLadyBugExpansion();
            black.useLadyBugExpansion();
        }

        if (gameType.contains("p")) {
            white.usePillBugExpansion();
            black.usePillBugExpansion();
        }

        game.addPlayers(white, black);
        game.getBoard().setStandardPositionMode(true);
        game.setManualStepping(true);
        game.setPrintGameStateAfterEachMove(DEBUG);
        game.start();
    }

    private long parseTime(String str) {
        if (str.length() == 7) {
            str = "0" + str;
        }

        String[] parts = str.split(":");
        int hours = Integer.parseInt(parts[0]) * 60 * 60 * 1000;
        int minutes = Integer.parseInt(parts[1]) * 60 * 1000;
        int seconds = Integer.parseInt(parts[2]) * 1000;

        return hours + minutes + seconds;
    }

    public String getPlayer1Name() {
        return player1;
    }

    public String getPlayer2Name() {
        return player2;
    }

    public long getPlayer1GameTime() {
        return player1GameTime;
    }

    public long getPlayer2GameTime() {
        return player2GameTime;
    }

    public int getPlayer1Turns() {
        return player1Turns;
    }

    public int getPlayer2Turns() {
        return player2Turns;
    }

    /**
	 * Parse a Boardspace game and return the full game.
	 */
    public Game parse() {

        try {
            loadGameData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Patterns extracted from plays on Boardspace.net
        // No official description exists AFAIK.
        //
        // Old pattern:  ; P0[32 dropb B1 N 12 bG1]     // Both Dumbot / Player moves
        // New Patterns: ; P1[19 pdropb bq L 14 -bL1]   // Player move token / pillbug moves
        //               ; P0[11 move W L1 N 12 wG1\\]  // Dumbot move token
        //               ; P0[86 pmove B M1 O 10 bM1-]  // Pillbug moves
        //               ; P0[31 move W P O 11 wQ-]     // Apparently pillbug doesn't have a number?!?

        Pattern p = Pattern.compile("P(.)\\[\\d+ (p?(dropb|move)) (. )?(.{1,3}) [A-Z] \\d+ ([^\\]]*)\\]; P.\\[.{1,3} done\\]");
        Matcher m = p.matcher(gameFileContent);

        while (m.find()) {
            int player = Integer.parseInt(m.group(1));
            String command = m.group(2).toLowerCase();
            String color = m.group(4) != null ? m.group(4).trim().toLowerCase() : null;
            String token = m.group(5).toLowerCase();
            String position = m.group(6).toLowerCase();

            // For pdropb commands we need to extract the color from the tokenId
            if (command.equals("pdropb")) {
                color = token.substring(0,1);
                token = token.substring(1);
            }

            GameCommand move = parseCommand(player, command, color, token, position);
            if (DEBUG) System.out.println("P" + player + ": " + command + " " + color + "-" + token + " -> " + position);
            game.continueGame(move);
        }

        return game;
    }

    /**
     * Converts a Boardspace.net dropb instruction into a GameCommand object.
     *
     * @param player   Either "0" (starting player/White) or "1"
     * @param command Command used by Boardspace.net, "dropb, pdrob, move, pmove". Not currently used.
     * @param color Either "w","b" or null. If null, token being moved is players own.
     * @param tokenId  "Q", "B1, ", "A3" etc. Represents a unique token for the given player
     * @param moveTo Uses the boardspace notation defined here: http://www.boardspace.net/english/about_hive_notation.html
     *
     * @return Game command object
     */
    private GameCommand parseCommand(int player, String command, String color, String tokenId, String moveTo) {

        // Find player for token being moved
        Player p = (player == 0) ? game.getWhitePlayer() : game.getBlackPlayer(); // White always start
        if (color != null) {
            p = (color.equals("w")) ? game.getWhitePlayer() : game.getBlackPlayer();
        }

        // Find token being moved
        Token token = p.get(tokenId);

        if (token == null) {
            throw new IllegalStateException("Cannot find token with ID: " + tokenId);
        }

        // Find position
        int[] position = findPosition(game, moveTo);

        if (token.getHex() == null) {
            return GameCommand.addFromSupply(token, position[0], position[1]);
        } else {
            if (command.equals("pmove")) {
                return GameCommand.moveByPillbug(token, position[0], position[1]);
            } else {
                return GameCommand.move(token, position[0], position[1]);
            }
        }
    }

    /**
     * Converts a Boardspace.net relative move notation to specific board coordinates.
     * @param game Game state where move happens.
     * @param moveTo Relative move description.
     *
     * @return int[2] where int[0] : Q coordinate, int[1] : R coordinate.
     */
    private int[] findPosition(Game game, String moveTo) {
        // Examples:
        // . ( first piece), -wG1, wG1-, \\bG1, bG1\\, wQ/, /wQ

        if (moveTo == null || moveTo.equals("")) {
            throw new IllegalStateException("'" + moveTo + "' is not a valid move description.");
        }

        moveTo = moveTo.toLowerCase(); // normalize casing

        // Switch on types
        String player;
        String tokenId;
        int[] neighborAdjustment; // int[2] { +/- Q, +/- R }. Assume flat orientation. Make it easy to switch?

        if (moveTo.equals(".")) {
            return new int[] {0, 0};

        } else if (moveTo.startsWith("-")) {
            player = moveTo.substring(1,2);
            tokenId = moveTo.substring(2);
            neighborAdjustment = new int[] {-1, 1};

        } else if (moveTo.endsWith("-")) {
            player = moveTo.substring(0,1);
            tokenId = moveTo.substring(1, moveTo.length() - 1);
            neighborAdjustment = new int[] {+1, -1};

        } else if (moveTo.startsWith("\\\\")) {
            player = moveTo.substring(2,3);
            tokenId = moveTo.substring(3);
            neighborAdjustment = new int[] {-1, 0};

        } else if (moveTo.endsWith("\\\\")) {
            player = moveTo.substring(0,1);
            tokenId = moveTo.substring(1, moveTo.length() - 2);
            neighborAdjustment = new int[] {1, 0};

        } else if (moveTo.startsWith("/")) {
            player = moveTo.substring(1,2);
            tokenId = moveTo.substring(2);
            neighborAdjustment = new int[] {0, 1};

        } else if (moveTo.endsWith("/")) {
            player = moveTo.substring(0,1);
            tokenId = moveTo.substring(1, moveTo.length() - 1);
            neighborAdjustment = new int[] {0, -1};

        } else if ((moveTo.startsWith("w") || moveTo.startsWith("b")) && (moveTo.length() == 2 || moveTo.length() == 3)) {
            player = moveTo.substring(0, 1);
            tokenId = moveTo.substring(1);
            neighborAdjustment = new int[] {0, 0};

        } else {
            throw new IllegalStateException("'" + moveTo + "' is not a valid move description.");
        }

        Player p = (player.equals("w")) ? game.getWhitePlayer() : game.getBlackPlayer();
        Token t = p.get(tokenId);
        if (p.get(tokenId) == null) {
            printer.print(game.getBoard());
            throw new IllegalStateException("Cannot find token: '" + tokenId + "'");
        }

        Hex hex = t.getHex();
        if (hex == null) {
            printer.print(game.getBoard());
            throw new IllegalStateException("'" + moveTo + "' is not a valid move description. Token " + tokenId + " is not on the board.");
        }

        return new int[] { hex.getQ() + neighborAdjustment[0], hex.getR() + neighborAdjustment[1] };
    }

    /**
     * Returns a game summary nicely formatted.
     * @return
     */
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(player1 + " vs. " + player2 + "\n");
        sb.append(player1Turns + " (" + player1GameTime + ") vs. " + player2Turns + " (" + player2GameTime + ")");
        return sb.toString();
    }

    /**
     * Returns the game time in the format "mm:ss";
     */
    private String formatTime(long time) {
        return String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(time),
                TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
        );
    }

    public String getGameType() {
        return gameType;
    }
}
