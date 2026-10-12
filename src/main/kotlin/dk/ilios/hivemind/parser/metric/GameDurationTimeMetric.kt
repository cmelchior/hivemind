package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameStatus
import dk.ilios.hivemind.parser.BoardspaceGameType


/**
 * Tracks how long games last.
 */
class GameDurationTimeMetric : Metric() {
    private val maxTurn = Int.MIN_VALUE
    private val minTurn = Int.MAX_VALUE
    private val games: MutableMap<String, Result> = hashMapOf<String, Result>()

    init {
        games[ALL_KEY] = Result()
    }

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        val variantKey: String = ALL_KEY + "-" + type.prefix
        val groupKey = type.prefix + "-" + variant

        if (!games.containsKey(variantKey)) {
            games[variantKey] = Result()
        }

        if (!games.containsKey(groupKey)) {
            games[groupKey] = Result()
        }

        // Only consider games we know has ended
        val status = game.status
        if (status == GameStatus.RESULT_WHITE_WINS || status == GameStatus.RESULT_BLACK_WINS || status == GameStatus.RESULT_DRAW) {
            val gameTime = game.whitePlayer.playTime + game.blackPlayer.playTime
            val turns = (game.whitePlayer.turns + game.blackPlayer.turns).toLong()

            games[ALL_KEY]!!.games += 1
            games[variantKey]!!.games += 1
            games[groupKey]!!.games += 1

            games[ALL_KEY]!!.time += gameTime
            games[variantKey]!!.time += gameTime
            games[groupKey]!!.time += gameTime

            games[ALL_KEY]!!.turns += turns
            games[variantKey]!!.turns += turns
            games[groupKey]!!.turns += turns
        }
    }

    override fun save() {
        saveStandard()
        savePrTurn()
    }

    private fun savePrTurn() {
        val file = Array(games.size + 1) { arrayOfNulls<String>(2) }  // [y][x]

        // Set labels
        file[0] = arrayOf<String?>("Variant", "Time (s.)")

        // Fill in values
        val keys = games.keys.sorted()

        var row = 1
        for (key in keys) {
            val r: Result = games[key]!!
            file[row][0] = key
            file[row][1] = (r.time / (r.turns * 1000)).toString()
            row++
        }

        saveStringToDisc(TURN_FILE_NAME, matrixToString(file))
    }

    private fun saveStandard() {
        val file = Array(games.size + 1) { arrayOfNulls<String>(2) }  // [y][x]

        // Set labels
        file[0] = arrayOf<String?>("Variant", "Time (s.)")

        // Fill in values
        val keys = games.keys.sorted()

        var row = 1
        for (key in keys) {
            val r: Result = games[key]!!
            file[row][0] = key
            file[row][1] = (r.time / (r.games * 1000)).toString()
            row++
        }

        saveStringToDisc(FILE_NAME, matrixToString(file))
    }

    private inner class Result {
        var games: Long = 0
        var time: Long = 0
        var turns: Long = 0
    }

    companion object {
        const val FILE_NAME: String = "game_time.data"
        const val TURN_FILE_NAME: String = "game_time_pr_turn.data"
        private const val ALL_KEY = "all"
    }
}
