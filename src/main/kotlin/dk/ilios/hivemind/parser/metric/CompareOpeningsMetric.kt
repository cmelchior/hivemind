package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameStatus
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.parser.BoardspaceGameType


/**
 * Show openings for both black and white and their respective win rates
 */
class CompareOpeningsMetric : Metric() {
    private val results: MutableMap<String, Result> = hashMapOf<String, Result>()

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        if (game.whitePlayer.turns < 4 || game.blackPlayer.turns < 4) return

        val whiteOpening = getOpening(game.whitePlayer, game)
        val blackOpening = getOpening(game.blackPlayer, game)

        val key = whiteOpening + blackOpening

        if (!results.containsKey(key)) {
            val result: Result = Result()
            result.whiteOpening = whiteOpening
            result.blackOpening = blackOpening
            results[key] = result
        }

        val r: Result = results[key]!!

        val status = game.status
        when (status) {
            GameStatus.RESULT_MATCH_IN_PROGRESS, GameStatus.RESULT_TURN_LIMIT_REACHED, GameStatus.RESULT_TIME_LIMIT_REACHED, GameStatus.RESULT_NOT_STARTED -> r.others++
            GameStatus.RESULT_DRAW -> r.draws++
            GameStatus.RESULT_BLACK_WINS -> r.blackWins++
            GameStatus.RESULT_WHITE_WINS -> r.whiteWins++
        }
    }

    private fun getOpening(player: Player, game: Game): String {
        val sb = StringBuilder()

        // Collect first 4 moves from player
        for (i in 1..4) {
            val command = game.getMove(player, i)
            if (i > 1) {
                sb.append("-")
            }

            if (command!!.token != null) {
                sb.append(command.token.id)
            } else {
                sb.append("PASS")
            }
        }

        return sb.toString()
    }

    override fun save() {
        saveCount()
        savePercentage()
    }

    private fun saveCount() {
        val file = Array(results.size + 1) { arrayOfNulls<String>(5) }
        file[0] = arrayOf<String?>("White", "WhiteWin", "Draw", "BlackWin", "Black")

        val keys = results.values.sorted()

        var row = 1
        for (i in keys.indices.reversed()) {
            val r = keys[i]
            file[row][0] = r.whiteOpening
            file[row][1] = r.whiteWins.toString()
            file[row][2] = r.draws.toString()
            file[row][3] = r.blackWins.toString()
            file[row][4] = r.blackOpening
            row++
        }

        saveStringToDisc(COUNT_FILE_NAME, matrixToString(file))
    }

    private fun savePercentage() {
        val file = Array(results.size + 1) { arrayOfNulls<String>(5) }
        file[0] = arrayOf<String?>("White", "WhiteWin", "Draw", "BlackWin", "Black")

        val keys = results.values.sorted()

        var row = 1
        for (i in keys.indices.reversed()) {
            val r = keys[i]
            file[row][0] = r.whiteOpening
            file[row][1] = if (r.total() > 0) (100 * r.whiteWins / r.total().toFloat()).toString() else "0.0"
            file[row][2] = if (r.total() > 0) (100 * r.draws / r.total().toFloat()).toString() else "0.0"
            file[row][3] = if (r.total() > 0) (100 * r.blackWins / r.total().toFloat()).toString() else "0.0"
            file[row][4] = r.blackOpening
            row++
        }

        saveStringToDisc(PERCENTAGE_FILE_NAME, matrixToString(file))
    }

    private inner class Result : Comparable<Any?> {
        var whiteOpening: String? = null
        var blackOpening: String? = null
        var blackWins: Int = 0
        var whiteWins: Int = 0
        var draws: Int = 0
        var others: Int = 0

        fun total(): Int = whiteWins + blackWins + draws

        fun key(): String = whiteOpening + blackOpening

        override fun compareTo(other: Any?): Int {
            if (other !is Result) return 1

            val r2 = other
            if (total() == r2.total()) {
                return key().compareTo(r2.key())
            } else {
                if (total() < r2.total()) {
                    return -1
                } else {
                    return 1
                }
            }
        }
    }

    companion object {
        const val COUNT_FILE_NAME: String = "compare_openings_count.data"
        const val PERCENTAGE_FILE_NAME: String = "compare_openings_percentage.data"
    }
}
