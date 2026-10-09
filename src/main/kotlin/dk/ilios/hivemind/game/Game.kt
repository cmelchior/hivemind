package dk.ilios.hivemind.game

import dk.ilios.hivemind.ai.statistics.GameStatistics
import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter
import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.StandardPositionMode
import dk.ilios.hivemind.model.rules.Rules
import java.util.*

/**
 * General game state class for a game of Hive.
 *
 * A Zobrist key is maintained for the game state. It consists of zobrist key for the board + game state information.
 * See `Board` for further details.
 *
 * @see [1] Randy Ingersoll: Play Hive like a champion
 *
 * @see [2] http://en.wikipedia.org/wiki/Zobrist_hashing
 */
class Game {
    var name: String? = "HiveX"

    // Players
    private var whitePlayer: Player? = null
    private var blackPlayer: Player? = null

    // Board properties
    private var board: Board? = null // Reference to game board

    private var useZobristKey = false // If true, a Zobrist key is maintained for the board position and game state.
    var zobristKey: Long = 0 // Zobrist key for board + game state
        private set
    private val playerHash = LongArray(2)

    private var isRunning = false // Game is started and progressing
    private var manualStepping =
        false // If true, continue() must be called after every move to progress the game (for debugging/testing)
    private var replayMode =
        false // If true, the game cannot progress any futher, but forward(), backwards() can be called to navigate the game. When set to to false, game is forwarded to last position again.
    private var replayIndex = 0 // Pointer to current move (that has not been played).

    var turnLimit: Int = -1 // If above 0, the game ends in a draw after so many moves.
    var status: GameStatus = GameStatus.RESULT_NOT_STARTED
        set(status) {
            field = status
            this.statistics.setStatus(status)
        }
    private var activePlayer: Player? = null

    private val moves: MutableList<GameCommand> = ArrayList<GameCommand>()

    // Keeping track of draws
    private val enforceForcedDraw =
        true // If true, game is declared a draw after a set number of repeat moves by each player.
    private val repeatMovesBeforeForcedDraw =
        3 // Number of moves each player must make that are "the same" before forcing a draw.
    private var whiteDuplicateMoves = 0
    private var blackDuplicateMoves = 0

    // Debug properties
    private var printGameStateAfterEachMove = false
    val statistics: GameStatistics = GameStatistics()
    private val mapPrinter = HiveAsciiPrettyPrinter()


    fun addPlayers(white: Player, black: Player) {
        this.whitePlayer = white
        this.blackPlayer = black
        statistics.setWhiteName(whitePlayer!!.name)
        statistics.setBlackName(blackPlayer!!.name)
        board = Board(white, black)
    }

    /**
     * Resets the game and start a new game.
     */
    fun start() {
        if (isRunning) return
        board!!.clear()
        moves.clear()
        activePlayer = whitePlayer

        // Start the game loop
        statistics.startGame()
        this.status = GameStatus.RESULT_MATCH_IN_PROGRESS
        isRunning = true

        while (isRunning && !manualStepping) {
            continueGame()
        }
    }

    fun setReplayMode(enabled: Boolean) {
        if (replayMode == enabled) return

        if (enabled) {
            // Enable replay mode and put replay index at the start of the game
            isRunning = false
            replayMode = true
            replayIndex = moves.size
            while (replayIndex > 0) {
                backwards()
            }
        } else {
            // Disable replay mode and forward the game to last move.
            while (replayIndex < moves.size) {
                forward()
            }
            isRunning = true
            replayMode = false
        }
    }

    /**
     * Forward game state and return command that caused the state change
     * @return
     */
    fun forward(): GameCommand {
        check(replayMode) { "Replay mode not enabled" }
        check(replayIndex != moves.size) { "Cannot go forward any further, at end of game." }
        val command = moves.get(replayIndex)
        command.execute(this)
        replayIndex++
        return command
    }

    /**
     * Rollback game state and return command that caused the state change
     */
    fun backwards(): GameCommand {
        check(replayMode) { "Replay mode not enabled" }
        check(replayIndex != 0) { "Cannot go back any further, at beginning of game." }
        replayIndex--
        val command = moves.get(replayIndex)
        command.undo(this)
        return command
    }

    /**
     * Move the game forward one step with the given command.
     * This overrides the CommandProvider if any i set
     * @param command
     */
    /**
     * Move the game forward one step.
     * Only call this if manual stepping is enabled.
     * Doesn't work in replay mode,
     */
    @JvmOverloads
    fun continueGame(command: GameCommand? = null) {
        if (!isRunning || replayMode) throw RuntimeException("Not in replay mode.")

        // Use CommandProvider if command is not provided
        val command = command ?: checkNotNull(requireActivePlayer().commandProvider) {
            "Active player has no command provider"
        }.getCommand(this, getBoard())
        checkNotNull(command) { "Command must not be null" }

        if (isLegalCommand(command)) {
            executeCommand(command)
            if (printGameStateAfterEachMove) {
                printBoardState(command)
            }

            if (this.isEndOfGame) {
                if (DEBUG) println("Game over!")
                isRunning = false
                statistics.stopGame()
            }
        } else {
            throw IllegalStateException("Illegal command: " + activePlayer!!.name + " trying to execute " + command.toString())
        }
    }

    /**
     * Output the current board state to the screen
     */
    private fun printBoardState(command: GameCommand) {
        println(this.otherPlayer!!.name + ": Turn " + this.otherPlayer!!.turns)
        println("DuplicateMoves: " + whiteDuplicateMoves + " vs. " + blackDuplicateMoves)
        println(command.toString())
        mapPrinter.print(getBoard())
    }

    private fun executeCommand(command: GameCommand) {
        if (command == GameCommand.PASS) {
            statistics.playerPasses(requireActivePlayer())
        } else {
            statistics.playerMoves(requireActivePlayer())
        }

        keepTrackOfDuplicateMoves(command)
        command.execute(this)
        moves.add(command)
    }

    private fun getColorIndex(player: Player): Int {
        return if (player.isWhitePlayer) 0 else 1
    }


    /**
     * Keeps track of players executing duplicate moves.
     * Should be called before the move is actually executed.
     */
    private fun keepTrackOfDuplicateMoves(command: GameCommand) {
        if (moves.size >= 4) {
            val lastMove = moves.get(moves.size - 4).targetSquareDesc
            if (command.targetSquareDesc == lastMove) {
                if (activePlayer!!.isWhitePlayer) {
                    whiteDuplicateMoves++
                } else {
                    blackDuplicateMoves++
                }
            } else {
                if (activePlayer!!.isWhitePlayer) {
                    whiteDuplicateMoves = 0
                } else {
                    blackDuplicateMoves = 0
                }
            }
        }
    }

    private fun isLegalCommand(command: GameCommand): Boolean {
        checkNotNull(command) { "Command must not be null" }

        // Queen must be played at the 4 turn at the latest
        if (activePlayer!!.moves == 3) {
            val queenOnBoard = activePlayer!!.hasPlacedQueen()
            if (!queenOnBoard) {
                val ok = activePlayer!!.queen == command.token
                if (!ok) {
                    if (DEBUG) println("Queen must be moved now, was: " + command)
                }
                return ok
            }
        }

        // Hive must not move before queen has been placed
        if (!activePlayer!!.hasPlacedQueen()) {
            if (command != GameCommand.PASS && command.fromQ != Hex.SUPPLY) {
                if (DEBUG) println("Queen must be moved before placing queen: " + command)
                return false
            }
        }

        return true
    }

    val isEndOfGame: Boolean
        get() {
            val whiteQueenSurronded: Boolean =
                Rules.instance.isQueenSurrounded(getWhitePlayer(), getBoard())
            val blackQueenSurronded: Boolean =
                Rules.instance.isQueenSurrounded(getBlackPlayer(), getBoard())
            val forcedDraw = this.isForcedDraw

            // End game conditions
            if (whitePlayer!!.turns + blackPlayer!!.turns == 2 * turnLimit && turnLimit > 0) {
                this.status = GameStatus.RESULT_TURN_LIMIT_REACHED
                if (DEBUG) println("Move limit reached: " + turnLimit)
                return true
            } else if (whiteQueenSurronded && blackQueenSurronded) {
                this.status = GameStatus.RESULT_DRAW
                if (DEBUG) println("Game ended in a draw!")
                return true
            } else if (whiteQueenSurronded) {
                this.status = GameStatus.RESULT_BLACK_WINS
                if (DEBUG) println("Black wins!")
                return true
            } else if (blackQueenSurronded) {
                this.status = GameStatus.RESULT_WHITE_WINS
                if (DEBUG) println("White wins")
                return true
            } else if (forcedDraw) {
                this.status = GameStatus.RESULT_DRAW
                if (DEBUG) println("Game ended in a draw!")
                return true
            }

            // Continue game
            return false
        }

    fun getActivePlayer(): Player? {
        return activePlayer
    }

    fun requireActivePlayer(): Player = checkNotNull(activePlayer) { "No active player" }

    fun getWhitePlayer(): Player {
        return whitePlayer!!
    }

    fun getBlackPlayer(): Player {
        return blackPlayer!!
    }

    fun getBoard(): Board {
        checkNotNull(board) { "Add players before board is available" }
        return board!!
    }

    fun togglePlayer() {
        if (activePlayer == null) {
            activePlayer = whitePlayer
        } else {
            activePlayer = if (activePlayer === whitePlayer) blackPlayer else whitePlayer
        }
    }

    fun setActivePlayer(player: Player) {
        check(!(player != whitePlayer && player != blackPlayer)) { "Active player must eiter be white or black: " + player.name }
        activePlayer = player
    }

    val otherPlayer: Player?
        get() = if (activePlayer === whitePlayer) blackPlayer else whitePlayer

    fun setManualStepping(manualStepping: Boolean) {
        this.manualStepping = manualStepping
    }

    fun setPrintGameStateAfterEachMove(print: Boolean) {
        this.printGameStateAfterEachMove = print
    }

    /**
     * Return the move for the player at the given turn (for that player)
     * @param player    Black or white player
     * @param turn      1 - GameEnd
     * @returns GameCommand for that move or null if it doesn't exists.
     */
    fun getMove(player: Player, turn: Int): GameCommand? {
        if (turn <= 0) return null
        val turnIndex = (turn - 1) * 2 + (if (player.isBlack) 1 else 0)
        return if (moves.size > turnIndex) moves.get(turnIndex) else null
    }

    private val isForcedDraw: Boolean
        get() {
            if (!enforceForcedDraw) return false
            val maxTurns = repeatMovesBeforeForcedDraw
            return whiteDuplicateMoves >= maxTurns && blackDuplicateMoves >= maxTurns
        }

    /**
     * Enable the calculation of Zobrist keys for the board. Enabling Zobrist key also enabled Standard Position.
     */
    fun setStandardPositionMode(mode: StandardPositionMode?) {
        board!!.setStandardPositionMode(mode)
        useZobristKey = (mode != StandardPositionMode.DISABLED)

        // Initialize Game state hashes
        val random = Random()
        playerHash[0] = random.nextLong()
        playerHash[1] = random.nextLong()
    }

    fun updateZobristKey() {
        if (useZobristKey) {
            zobristKey = board!!.zobristKey xor playerHash[getColorIndex(getActivePlayer()!!)]
        }
    }

    companion object {
        private const val DEBUG = false
    }
}
