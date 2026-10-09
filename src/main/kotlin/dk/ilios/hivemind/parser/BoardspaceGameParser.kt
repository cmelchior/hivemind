package dk.ilios.hivemind.parser

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.Player.PlayerType
import dk.ilios.hivemind.model.StandardPositionMode
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Parser for games played at boardgames.net
 */
class BoardspaceGameParser(game: File) {
    private val gameFile: File // Reference to game file
    private var gameFileContent: String? = null // Game file loaded as a string with new lines removed
    private var gameType: String? = null // String describing the game: "hive", "hive-mlp", "hive-p" etc.
    private var game: Game? = null // Reference to actual game object

    private var player1: String? = null
    val player1Turns: Int = 0
    var player1GameTime: Long = -1
        private set
    private var player1Ranking = -1

    private var player2: String? = null
    val player2Turns: Int = 0
    var player2GameTime: Long = -1
        private set
    private var player2Ranking = -1

    private val printer = HiveAsciiPrettyPrinter()

    init {
        this.gameFile = game
        try {
            loadGameData()
        } catch (e: IOException) {
            e.printStackTrace() /* Ignore */
        }
    }

    /**
     * Load basis data from the game file and prepare a game configuration.
     */
    @Throws(IOException::class)
    private fun loadGameData() {
        val sb = StringBuilder()
        val br = BufferedReader(FileReader(gameFile))
        var line: String?
        while ((br.readLine().also { line = it }) != null) {
            sb.append(line) // Remove linebreaks, so we later can match moved tokens properly (identified by dropb -> done)

            // Game type
            if (line!!.startsWith("SU[")) {
                gameType = line.substring(3, line.length - 1)
                continue
            }

            // Player 1 (white) name
            if (line.startsWith("P0[id")) {
                player1 = line.substring(7, line.length - 2)
                continue
            }

            // Player 2 (black) name
            if (line.startsWith("P1[id")) {
                player2 = line.substring(7, line.length - 2)
                continue
            }

            // Player 1 game time
            if (line.startsWith("P0[time")) {
                player1GameTime = parseTime(line.substring(8, line.length - 2))
                continue
            }

            // Player 2 game time
            if (line.startsWith("P1[time")) {
                player2GameTime = parseTime(line.substring(8, line.length - 2))
                continue
            }

            // Player 1 ranking
            if (line.startsWith("P0[ranking")) {
                player1Ranking = line.substring(11, line.length - 2).toInt()
                continue
            }

            // Player 2 ranking
            if (line.startsWith("P1[ranking")) {
                player2Ranking = line.substring(11, line.length - 2).toInt()
                continue
            }
        }
        br.close()

        // Verify that data is loaded
        require(!(player1 == null || player2 == null || player1GameTime == -1L || player2GameTime == -1L || gameType == null)) { "Game could not be parsed properly: " + gameFile.getAbsolutePath() }

        gameFileContent = sb.toString()

        game = Game()
        game!!.name = gameFile.getName()

        // Setup players
        val white = Player(player1, PlayerType.WHITE)
        val black = Player(player2, PlayerType.BLACK)
        white.addPlayTime(player1GameTime)
        white.ranking = player1Ranking
        black.addPlayTime(player2GameTime)
        black.ranking = player2Ranking

        white.fillBaseSupply()
        black.fillBaseSupply()

        if (gameType!!.contains("m")) {
            white.useMosquitoExpansion()
            black.useMosquitoExpansion()
        }

        if (gameType!!.contains("l")) {
            white.useLadyBugExpansion()
            black.useLadyBugExpansion()
        }

        if (gameType!!.contains("p")) {
            white.usePillBugExpansion()
            black.usePillBugExpansion()
        }

        game!!.addPlayers(white, black)
        game!!.getBoard().setStandardPositionMode(StandardPositionMode.ENABLED)
        game!!.setManualStepping(true)
        game!!.setPrintGameStateAfterEachMove(DEBUG)
        game!!.start()
    }

    private fun parseTime(str: String): Long {
        var str = str
        if (str.length == 7) {
            str = "0" + str
        }

        val parts: Array<String?> = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val hours = parts[0]!!.toInt() * 60 * 60 * 1000
        val minutes = parts[1]!!.toInt() * 60 * 1000
        val seconds = parts[2]!!.toInt() * 1000

        return (hours + minutes + seconds).toLong()
    }

    val player1Name: String
        get() = player1!!

    val player2Name: String
        get() = player2!!

    /**
     * Parse a Boardspace game and return the full game.
     */
    fun parse(): Game {
        try {
            loadGameData()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        // Patterns extracted from plays on Boardspace.net
        // No official description exists AFAIK.
        //
        // Old pattern:  ; P0[32 dropb B1 N 12 bG1]     // Both Dumbot / Player moves
        // New Patterns: ; P1[19 pdropb bq L 14 -bL1]   // Player move token / pillbug moves
        //               ; P0[11 move W L1 N 12 wG1\\]  // Dumbot move token
        //               ; P0[86 pmove B M1 O 10 bM1-]  // Pillbug moves
        //               ; P0[31 move W P O 11 wQ-]     // Apparently pillbug doesn't have a number?!?
        val p =
            Pattern.compile("P(.)\\[\\d+ (p?(dropb|move)) (. )?(.{1,3}) [A-Z] \\d+ ([^\\]]*)\\]; P.\\[.{1,3} done\\]")
        val m = p.matcher(gameFileContent)

        while (m.find()) {
            val player = m.group(1).toInt()
            val command = m.group(2).lowercase(Locale.getDefault())
            var color = if (m.group(4) != null) m.group(4).trim { it <= ' ' }.lowercase(Locale.getDefault()) else null
            var token = m.group(5).lowercase(Locale.getDefault())
            val position = m.group(6).lowercase(Locale.getDefault())

            // For pdropb commands we need to extract the color from the tokenId
            if (command == "pdropb") {
                color = token.substring(0, 1)
                token = token.substring(1)
            }

            val move = parseCommand(player, command, color, token, position)
            if (DEBUG) println("P" + player + ": " + command + " " + color + "-" + token + " -> " + position)
            game!!.continueGame(move)
        }

        return game!!
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
    private fun parseCommand(
        player: Int,
        command: String,
        color: String?,
        tokenId: String,
        moveTo: String
    ): GameCommand {
        // Find player for token being moved

        var p = if (player == 0) game!!.getWhitePlayer() else game!!.getBlackPlayer() // White always start
        if (color != null) {
            p = if (color == "w") game!!.getWhitePlayer() else game!!.getBlackPlayer()
        }

        // Find token being moved
        val token = p.get(tokenId)

        checkNotNull(token) { "Cannot find token with ID: " + tokenId }

        // Find position
        val position = findPosition(game!!, moveTo)

        if (token.hex == null) {
            return GameCommand.addFromSupply(token, position[0], position[1])
        } else {
            if (command == "pmove") {
                return GameCommand.moveByPillbug(token, position[0], position[1])
            } else {
                return GameCommand.move(token, position[0], position[1])
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
    private fun findPosition(game: Game, moveTo: String): IntArray {
        // Examples:
        // . ( first piece), -wG1, wG1-, \\bG1, bG1\\, wQ/, /wQ

        var moveTo = moveTo
        check(moveTo.isNotEmpty()) { "'" + moveTo + "' is not a valid move description." }

        moveTo = moveTo.lowercase(Locale.getDefault()) // normalize casing

        // Switch on types
        val player: String?
        val tokenId: String?
        val neighborAdjustment: IntArray? // int[2] { +/- Q, +/- R }. Assume flat orientation. Make it easy to switch?

        if (moveTo == ".") {
            return intArrayOf(0, 0)
        } else if (moveTo.startsWith("-")) {
            player = moveTo.substring(1, 2)
            tokenId = moveTo.substring(2)
            neighborAdjustment = intArrayOf(-1, 1)
        } else if (moveTo.endsWith("-")) {
            player = moveTo.substring(0, 1)
            tokenId = moveTo.substring(1, moveTo.length - 1)
            neighborAdjustment = intArrayOf(+1, -1)
        } else if (moveTo.startsWith("\\\\")) {
            player = moveTo.substring(2, 3)
            tokenId = moveTo.substring(3)
            neighborAdjustment = intArrayOf(-1, 0)
        } else if (moveTo.endsWith("\\\\")) {
            player = moveTo.substring(0, 1)
            tokenId = moveTo.substring(1, moveTo.length - 2)
            neighborAdjustment = intArrayOf(1, 0)
        } else if (moveTo.startsWith("/")) {
            player = moveTo.substring(1, 2)
            tokenId = moveTo.substring(2)
            neighborAdjustment = intArrayOf(0, 1)
        } else if (moveTo.endsWith("/")) {
            player = moveTo.substring(0, 1)
            tokenId = moveTo.substring(1, moveTo.length - 1)
            neighborAdjustment = intArrayOf(0, -1)
        } else if ((moveTo.startsWith("w") || moveTo.startsWith("b")) && (moveTo.length == 2 || moveTo.length == 3)) {
            player = moveTo.substring(0, 1)
            tokenId = moveTo.substring(1)
            neighborAdjustment = intArrayOf(0, 0)
        } else {
            throw IllegalStateException("'" + moveTo + "' is not a valid move description.")
        }

        val p = if (player == "w") game.getWhitePlayer() else game.getBlackPlayer()
        val t = p.get(tokenId)
        if (p.get(tokenId) == null) {
            printer.print(game.getBoard())
            throw IllegalStateException("Cannot find token: '" + tokenId + "'")
        }

        val hex = t!!.hex
        if (hex == null) {
            printer.print(game.getBoard())
            throw IllegalStateException("'" + moveTo + "' is not a valid move description. Token " + tokenId + " is not on the board.")
        }

        return intArrayOf(hex.q + neighborAdjustment[0], hex.r + neighborAdjustment[1])
    }

    /**
     * Returns a game summary nicely formatted.
     * @return
     */
    fun summary(): String {
        val sb = StringBuilder()
        sb.append(player1 + " vs. " + player2 + "\n")
        sb.append(player1Turns.toString() + " (" + player1GameTime + ") vs. " + player2Turns + " (" + player2GameTime + ")")
        return sb.toString()
    }

    /**
     * Returns the game time in the format "mm:ss";
     */
    private fun formatTime(time: Long): String {
        return String.format(
            "%d:%d",
            TimeUnit.MILLISECONDS.toMinutes(time),
            TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
        )
    }

    fun getGameType(): String {
        return gameType!!
    }

    companion object {
        private const val DEBUG = false
        private val TIME_FORMATTER = SimpleDateFormat("hh:mm:ss")
    }
}
