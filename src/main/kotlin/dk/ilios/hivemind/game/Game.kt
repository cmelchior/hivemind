package dk.ilios.hivemind.game

import dk.ilios.hivemind.ai.statistics.GameStatistics
import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter
import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.StandardPositionMode
import dk.ilios.hivemind.model.rules.Rules
import kotlin.random.Random

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
    private var whitePlayerState: Player? = null
    private var blackPlayerState: Player? = null

    // Board properties
    private var boardState: Board? = null // Reference to game board

    private var useZobristKey =
        false // If true, a Zobrist key is maintained for the board position and game state.
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
    private var activePlayerState: Player? = null

    private val moves: MutableList<GameCommand> = mutableListOf<GameCommand>()

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
        this.whitePlayerState = white
        this.blackPlayerState = black
        statistics.setWhiteName(whitePlayerState!!.name)
        statistics.setBlackName(blackPlayerState!!.name)
        boardState = Board(white, black)
    }

    /**
     * Resets the game and start a new game.
     */
    fun start() {
        if (isRunning) return
        boardState!!.clear()
        moves.clear()
        activePlayerState = whitePlayerState

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
        val command = moves[replayIndex]
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
        val command = moves[replayIndex]
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
        }.getCommand(this, board)
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
            throw IllegalStateException("Illegal command: " + activePlayerState!!.name + " trying to execute " + command.toString())
        }
    }

    /**
     * Output the current board state to the screen
     */
    private fun printBoardState(command: GameCommand) {
        println(this.otherPlayer!!.name + ": Turn " + this.otherPlayer!!.turns)
        println("DuplicateMoves: " + whiteDuplicateMoves + " vs. " + blackDuplicateMoves)
        println(command.toString())
        mapPrinter.print(board)
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

    private fun getColorIndex(player: Player): Int = if (player.isWhitePlayer) 0 else 1


    /**
     * Keeps track of players executing duplicate moves.
     * Should be called before the move is actually executed.
     */
    private fun keepTrackOfDuplicateMoves(command: GameCommand) {
        if (moves.size >= 4) {
            val lastMove = moves[moves.size - 4].targetSquareDesc
            if (command.targetSquareDesc == lastMove) {
                if (activePlayerState!!.isWhitePlayer) {
                    whiteDuplicateMoves++
                } else {
                    blackDuplicateMoves++
                }
            } else {
                if (activePlayerState!!.isWhitePlayer) {
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
        if (activePlayerState!!.moves == 3) {
            val queenOnBoard = activePlayerState!!.hasPlacedQueen()
            if (!queenOnBoard) {
                val ok = activePlayerState!!.queen == command.token
                if (!ok) {
                    if (DEBUG) println("Queen must be moved now, was: " + command)
                }
                return ok
            }
        }

        // Hive must not move before queen has been placed
        if (!activePlayerState!!.hasPlacedQueen()) {
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
                Rules.instance.isQueenSurrounded(whitePlayer, board)
            val blackQueenSurronded: Boolean =
                Rules.instance.isQueenSurrounded(blackPlayer, board)
            val forcedDraw = this.isForcedDraw

            // End game conditions
            if (whitePlayerState!!.turns + blackPlayerState!!.turns == 2 * turnLimit && turnLimit > 0) {
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

    val activePlayer: Player?
        get() = activePlayerState

    fun requireActivePlayer(): Player = checkNotNull(activePlayer) { "No active player" }

    val whitePlayer: Player
        get() = checkNotNull(whitePlayerState) { "Add players before accessing white player" }

    val blackPlayer: Player
        get() = checkNotNull(blackPlayerState) { "Add players before accessing black player" }

    val board: Board
        get() = checkNotNull(boardState) { "Add players before board is available" }

    fun togglePlayer() {
        if (activePlayerState == null) {
            activePlayerState = whitePlayerState
        } else {
            activePlayerState = if (activePlayerState === whitePlayerState) blackPlayerState else whitePlayerState
        }
    }

    fun setActivePlayer(player: Player) {
        check(!(player != whitePlayerState && player != blackPlayerState)) { "Active player must eiter be white or black: " + player.name }
        activePlayerState = player
    }

    val otherPlayer: Player?
        get() = if (activePlayerState === whitePlayerState) blackPlayerState else whitePlayerState

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
        return if (moves.size > turnIndex) moves[turnIndex] else null
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
        boardState!!.setStandardPositionMode(mode)
        useZobristKey = (mode != StandardPositionMode.DISABLED)

        // Initialize Game state hashes
        val random = Random.Default
        playerHash[0] = random.nextLong()
        playerHash[1] = random.nextLong()
    }

    fun updateZobristKey() {
        if (useZobristKey) {
            zobristKey = boardState!!.zobristKey xor playerHash[getColorIndex(activePlayer!!)]
        }
    }

    companion object {
        private const val DEBUG = false
    }
}
