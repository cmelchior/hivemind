package dk.ilios.hivemind.ai.statistics

import dk.ilios.hivemind.ai.HiveAI
import java.util.*

/**
 * Utility class for keeping track of AI performance
 */
class AIStatistics(ai: HiveAI) {
    private val DEBUG = true

    private val ai: HiveAI

    // Result arrays
    var millisecondsPrMove: ArrayList<Long?> = ArrayList<Long?>() // Time in milliseconds pr. move of the game
    var gameStatesEvaluatedPrSecond: ArrayList<Int?> = ArrayList<Int?>() // Normalized "performance" value
    var positionsEvaluatedPrMove: ArrayList<Int?> =
        ArrayList<Int?>() // Number of positions looked at when finding a move.
    var cacheHits: ArrayList<Int?> = ArrayList<Int?>() // Number of cachehits

    var numberOfCutoffs: Int = 0
    var totalCutoffTurns: Int = 0 // Average number of moves before cutoffs is totalCutoffTurns/numberOfCutoffs

    var branches: Int = 0
    var nodes: Int = 0
    var cacheHit: Int = 0

    // Temporary data
    var currentKey: String? = null
    var aiDepth: Int = 3
    var positionsEvaluated: Int = 0 // How many moves has been considered when getting the next move.

    init {
        this.ai = ai
    }

    /**
     * A new move request has been made.
     */
    fun startCalculatingNextMove() {
        currentKey = UUID.randomUUID().toString()
        StopWatch.instance!!.start(currentKey)
        positionsEvaluated = 0
    }

    /**
     * The AI has returned a move
     */
    fun moveCalculated() {
        val time: Long = StopWatch.instance!!.stop(currentKey).elapsedTimeInMillis
        val movesPrSecond = (positionsEvaluated / (time / 1000.0)).toInt()

        millisecondsPrMove.add(time)
        gameStatesEvaluatedPrSecond.add(movesPrSecond)
        positionsEvaluatedPrMove.add(positionsEvaluated)
        cacheHits.add(cacheHit)

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
        get() {
            var result = 0
            for (i in millisecondsPrMove) {
                result += i!!.toInt()
            }

            return result / millisecondsPrMove.size.toDouble()
        }

    val meanTimePrMove: Long
        get() {
            if (millisecondsPrMove.size == 0) return 0
            val list = arrayOfNulls<Long>(millisecondsPrMove.size)
            millisecondsPrMove.toArray<Long?>(list)
            Arrays.sort(list)
            return list[list.size / 2]!!
        }

    val maxTimePrMove: Long
        get() {
            var result: Long = 0
            for (i in millisecondsPrMove) {
                if (i!! > result) {
                    result = i
                }
            }
            return result
        }

    val averagePositionsEvaluatedPrMove: Double
        get() {
            var result = 0
            for (i in positionsEvaluatedPrMove) {
                result += i!!
            }

            return result / positionsEvaluatedPrMove.size.toDouble()
        }

    val meanPositionsEvaluatedPrMove: Int
        get() {
            if (positionsEvaluatedPrMove.size == 0) return 0
            val list = arrayOfNulls<Int>(positionsEvaluatedPrMove.size)
            positionsEvaluatedPrMove.toArray<Int?>(list)
            Arrays.sort(list)
            return list[list.size / 2]!!
        }

    val maxPositionsEvaluatedPrMove: Int
        get() {
            var result = 0
            for (i in positionsEvaluatedPrMove) {
                if (i!! > result) {
                    result = i
                }
            }

            return result
        }

    fun getMillisecondsPrMove(): MutableList<Long?> {
        return millisecondsPrMove
    }

    fun getGameStatesEvaluatedPrSecond(): MutableList<Int?> {
        return gameStatesEvaluatedPrSecond
    }

    fun getPositionsEvaluatedPrMove(): MutableList<Int?> {
        return positionsEvaluatedPrMove
    }

    val averageBranchFactor: Int
        get() = if (nodes > 0) Math.round((branches / nodes).toFloat()) else 0

    fun getCacheHits(): MutableList<Int?> {
        return cacheHits
    }

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
