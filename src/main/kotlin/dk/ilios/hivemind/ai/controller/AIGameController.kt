package dk.ilios.hivemind.ai.controller

import dk.ilios.hivemind.ai.HiveAI
import dk.ilios.hivemind.ai.statistics.GameStatistics
import dk.ilios.hivemind.game.CommandProvider
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.Player.PlayerType
import dk.ilios.hivemind.model.StandardPositionMode
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Controller class for executing AI battles.
 * Given x number of opponents, y number of battles are executed among all pairs.
 * Results are then printed to the screen.
 */
class AIGameController {
    private var turnLimit = 0
    private var numberOfMatches = 0

    private var duration: Long = 0
    private val opponents: MutableSet<HiveAI> = HashSet<HiveAI>()
    private val gameResults: MutableList<GameStatistics> =
        Collections.synchronizedList<GameStatistics>(ArrayList<GameStatistics>())

    fun addOpponent(opponent: HiveAI?) {
        opponents.add(opponent!!)
    }

    /**
     * Set turn limit pr. player
     */
    fun setTurnLimit(turnLimit: Int) {
        this.turnLimit = turnLimit
    }

    fun setNumberOfMatches(numberOfMatches: Int) {
        this.numberOfMatches = numberOfMatches
    }

    fun start() {
        val start = System.currentTimeMillis()
        val executor = Executors.newFixedThreadPool(THREADS)
        for (oppA in opponents) {
            for (oppB in opponents) {
                if (oppA == oppB) continue
                for (i in 0..<numberOfMatches) {
                    val a = oppA.copy()
                    val b = oppB.copy()
                    executor.execute(object : Runnable {
                        override fun run() {
                            runGame(a, b, false)
                        }
                    })
                }
            }
        }

        // Wait for tasks to finish
        executor.shutdown()
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
        } catch (e: InterruptedException) {
            /* Ignore */
        }

        duration = System.currentTimeMillis() - start
    }

    fun startSingleGame(whitePlayer: HiveAI, blackPlayer: HiveAI, printGameState: Boolean) {
        runGame(whitePlayer, blackPlayer, printGameState)
    }

    private fun runGame(whiteAI: HiveAI, blackAI: HiveAI, printGameState: Boolean) {
        val whitePlayer = Player(whiteAI.name, PlayerType.WHITE)
        whitePlayer.fillBaseSupply()
        whitePlayer.commandProvider = object : CommandProvider {
            override fun getCommand(currentState: Game, board: Board): GameCommand? {
                whiteAI.aiStats.startCalculatingNextMove()
                val command = whiteAI.nextMove(currentState, board)
                whiteAI.aiStats.moveCalculated()
                return command
            }
        }
        val blackPlayer = Player(blackAI.name, PlayerType.BLACK)
        blackPlayer.fillBaseSupply()
        blackPlayer.commandProvider = object : CommandProvider {
            override fun getCommand(currentState: Game, board: Board): GameCommand? {
                blackAI.aiStats.startCalculatingNextMove()
                val command = blackAI.nextMove(currentState, board)
                blackAI.aiStats.moveCalculated()
                return command
            }
        }
        val game = Game()
        game.turnLimit = turnLimit
        game.setPrintGameStateAfterEachMove(printGameState)
        game.addPlayers(whitePlayer, blackPlayer)
        game.setStandardPositionMode(if (whiteAI.maintainsStandardPosition() || blackAI.maintainsStandardPosition()) StandardPositionMode.ENABLED else StandardPositionMode.DISABLED)
        try {
            game.start()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }

        val statistics = game.statistics
        statistics.addWhiteAIStats(whiteAI.aiStats)
        statistics.addBlackAIStats(blackAI.aiStats)

        gameResults.add(game.statistics)
        println(game.statistics.shortSummary())
    }

    fun printLog(longSummary: Boolean) {
        val sb = StringBuilder("Games done: " + (duration / 1000f) + "s.\n")
        sb.append("================\n")
        for (stats in gameResults) {
            sb.append((if (longSummary) stats.longSummary() else stats.shortSummary()))
            sb.append('\n')
        }
        sb.append("================")
        println(sb.toString())
    }

    companion object {
        // How many games can run simultaneously. Max should be #CPUs - 1 to avoid CPU contention.
        // Look into if it is possible to force Java to use different CPU's. Right now we just cross fingers and hope.
        private const val THREADS = 3
    }
}
