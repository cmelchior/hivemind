package dk.ilios.hivemind.ai.statistics

import dk.ilios.hivemind.game.GameStatus
import dk.ilios.hivemind.model.Player


/**
 * Class that keeps track of a game
 */
class GameStatistics {
    private var start: Long = 0 // Start time in milliseconds from epox
    private var durartionInMillis: Long = 0 // Duration of game in milliseconds

    private var whiteName: String? = "?"
    private var whiteTurns = 0
    private var whiteMoves = 0
    private var whitePasses = 0

    private var blackName: String? = "?"
    private var blackTurns = 0
    private var blackMoves = 0
    private var blackPasses = 0

    private var status: GameStatus? = GameStatus.RESULT_NOT_STARTED

    private var whiteAI: AIStatistics? = null
    private var blackAI: AIStatistics? = null

    fun addWhiteAIStats(whiteAIStats: AIStatistics) {
        this.whiteAI = whiteAIStats
    }

    fun addBlackAIStats(blackAIStats: AIStatistics) {
        this.blackAI = blackAIStats
    }

    fun shortSummary(): String {
        return String.format(
            "%s (%s) vs. %s (%s) - %s ms.: %s",
            whiteName,
            whiteTurns,
            blackName,
            blackTurns,
            durartionInMillis,
            status
        )
    }

    fun longSummary(): String {
        val sb = StringBuilder(shortSummary())
        sb.append('\n')
        sb.append("Branching: " + whiteAI!!.averageBranchFactor + " vs. " + blackAI!!.averageBranchFactor)
        sb.append('\n')
        sb.append("Time pr move (max.): " + whiteAI!!.maxTimePrMove + " vs. " + blackAI!!.maxTimePrMove)
        sb.append('\n')
        sb.append("Time pr move (avg.): " + whiteAI!!.averageTimePrMove + " vs. " + blackAI!!.averageTimePrMove)
        sb.append('\n')
        sb.append("Time pr move (mean): " + whiteAI!!.meanTimePrMove + " vs. " + blackAI!!.meanTimePrMove)
        sb.append('\n')
        sb.append("Positions pr move (max.): " + whiteAI!!.maxPositionsEvaluatedPrMove + " vs. " + blackAI!!.maxPositionsEvaluatedPrMove)
        sb.append('\n')
        sb.append("Positions pr move (avg.): " + whiteAI!!.averagePositionsEvaluatedPrMove + " vs. " + blackAI!!.averagePositionsEvaluatedPrMove)
        sb.append('\n')
        sb.append("Positions pr move (mean): " + whiteAI!!.meanPositionsEvaluatedPrMove + " vs. " + blackAI!!.meanPositionsEvaluatedPrMove)
        sb.append('\n')
        sb.append("------------------")
        sb.append('\n')
        sb.append(whiteName + " time pr. move: " + whiteAI!!.millisecondsPrMove.toString())
        sb.append('\n')
        sb.append(
            whiteName + " game states pr. sec.: " + whiteAI!!.gameStatesEvaluatedPrSecond.toString()
        )
        sb.append('\n')
        sb.append(
            whiteName + " game states pr. move: " + whiteAI!!.positionsEvaluatedPrMove.toString()
        )
        sb.append('\n')
        sb.append(whiteName + " cache hits pr. move: " + whiteAI!!.cacheHits.toString())
        sb.append('\n')
        sb.append(whiteName + " average cutoff at move: " + whiteAI!!.averageMovesEvaluatedBeforeCutoff)
        sb.append('\n')
        sb.append(blackName + " time pr. move: " + blackAI!!.millisecondsPrMove.toString())
        sb.append('\n')
        sb.append(
            blackName + " game states pr. sec.: " + blackAI!!.gameStatesEvaluatedPrSecond.toString()
        )
        sb.append('\n')
        sb.append(
            blackName + " game states pr. move: " + blackAI!!.positionsEvaluatedPrMove.toString()
        )
        sb.append('\n')
        sb.append(blackName + " cache hits pr. move: " + blackAI!!.cacheHits.toString())
        sb.append('\n')
        sb.append(blackName + " average cutoff at move: " + blackAI!!.averageMovesEvaluatedBeforeCutoff)
        sb.append('\n')
        sb.append("------------------")

        return sb.toString()
    }

    fun setStatus(status: GameStatus?) {
        this.status = status
    }

    fun setWhiteName(name: String?) {
        this.whiteName = name
    }

    fun setBlackName(name: String?) {
        this.blackName = name
    }

    private fun blackMoves() {
        blackMoves++
        blackTurns++
    }

    private fun blackPasses() {
        blackPasses++
        blackTurns++
    }

    private fun whiteMoves() {
        whiteMoves++
        whiteTurns++
    }

    private fun whitePasses() {
        whitePasses++
        whiteTurns++
    }

    fun playerMoves(player: Player) {
        if (player.name == whiteName) {
            whiteMoves()
        } else {
            blackMoves()
        }
    }

    fun playerPasses(player: Player) {
        if (player.name == whiteName) {
            whitePasses()
        } else {
            blackPasses()
        }
    }

    fun stopGame() {
        durartionInMillis = System.currentTimeMillis() - start
    }

    fun startGame() {
        start = System.currentTimeMillis()
    }
}
