package dk.ilios.hivemind.ai.statistics

import dk.ilios.hivemind.ai.HiveAI
import java.util.UUID

/**
 * Utility class for keeping track of AI performance
 */
class AIStatistics(private val ai: HiveAI) {
    private val DEBUG = true

    private val moveTimes = mutableListOf<Long>()
    private val evaluationRates = mutableListOf<Int>()
    private val evaluations = mutableListOf<Int>()
    private val hits = mutableListOf<Int>()

    val millisecondsPrMove: List<Long>
        get() = moveTimes
    val gameStatesEvaluatedPrSecond: List<Int>
        get() = evaluationRates
    val positionsEvaluatedPrMove: List<Int>
        get() = evaluations
    val cacheHits: List<Int>
        get() = hits

    var numberOfCutoffs: Int = 0
    var totalCutoffTurns: Int = 0 // Average number of moves before cutoffs is totalCutoffTurns/numberOfCutoffs

    var branches: Int = 0
    var nodes: Int = 0
    var cacheHit: Int = 0

    // Temporary data
    var currentKey: String? = null
    var aiDepth: Int = 3
    var positionsEvaluated: Int = 0 // How many moves has been considered when getting the next move.

    /**
     * A new move request has been made.
     */
    fun startCalculatingNextMove() {
        currentKey = UUID.randomUUID().toString()
        StopWatch.instance.start(currentKey)
        positionsEvaluated = 0
    }

    /**
     * The AI has returned a move
     */
    fun moveCalculated() {
        val time: Long = StopWatch.instance.stop(currentKey).elapsedTimeInMillis
        val movesPrSecond = (positionsEvaluated / (time / 1000.0)).toInt()

        moveTimes.add(time)
        evaluationRates.add(movesPrSecond)
        evaluations.add(positionsEvaluated)
        hits.add(cacheHit)

        if (DEBUG) {
            println("(" + millisecondsPrMove.size + ") " + ai.name + " : " + (time / 1000.0) + " s.")
        }
    }

    /**
     * The AI has evaluated a game state using the heuristic function
     */
    fun boardEvaluated() {
        positionsEvaluated++
    }

    val averageTimePrMove: Double
        get() = moveTimes.sumOf { it.toInt() } / moveTimes.size.toDouble()

    val meanTimePrMove: Long
        get() = if (moveTimes.isEmpty()) 0 else moveTimes.sorted()[moveTimes.size / 2]

    val maxTimePrMove: Long
        get() = (moveTimes.maxOrNull() ?: 0).coerceAtLeast(0)

    val averagePositionsEvaluatedPrMove: Double
        get() = evaluations.sum() / evaluations.size.toDouble()

    val meanPositionsEvaluatedPrMove: Int
        get() = if (evaluations.isEmpty()) 0 else evaluations.sorted()[evaluations.size / 2]

    val maxPositionsEvaluatedPrMove: Int
        get() = (evaluations.maxOrNull() ?: 0).coerceAtLeast(0)

    val averageBranchFactor: Int
        get() = if (nodes > 0) branches / nodes else 0

    fun nodeBranched(size: Int) {
        nodes++
        branches += size
    }

    fun cacheHit() {
        cacheHit++
    }

    fun cutOffAfter(moveEvaluated: Int) {
        numberOfCutoffs++
        totalCutoffTurns += moveEvaluated
    }

    val averageMovesEvaluatedBeforeCutoff: Double
        get() {
            if (totalCutoffTurns == 0) return -1.0
            return totalCutoffTurns / numberOfCutoffs.toDouble()
        }
}
