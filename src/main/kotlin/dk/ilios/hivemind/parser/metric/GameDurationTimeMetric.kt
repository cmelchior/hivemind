package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameStatus
import dk.ilios.hivemind.parser.BoardspaceGameType
import java.util.*

/**
 * Tracks how long games last.
 */
class GameDurationTimeMetric : Metric() {
    private val maxTurn = Int.MIN_VALUE
    private val minTurn = Int.MAX_VALUE
    private val games: MutableMap<String?, Result> = HashMap<String?, Result>()

    init {
        games.put(ALL_KEY, Result())
    }

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        val variantKey: String = ALL_KEY + "-" + type.prefix
        val groupKey = type.prefix + "-" + variant

        if (!games.containsKey(variantKey)) {
            games.put(variantKey, Result())
        }

        if (!games.containsKey(groupKey)) {
            games.put(groupKey, Result())
        }

        // Only consider games we know has ended
        val status = game.status
        if (status == GameStatus.RESULT_WHITE_WINS || status == GameStatus.RESULT_BLACK_WINS || status == GameStatus.RESULT_DRAW) {
            val gameTime = game.getWhitePlayer().playTime + game.getBlackPlayer().playTime
            val turns = (game.getWhitePlayer().turns + game.getBlackPlayer().turns).toLong()

            games.get(ALL_KEY)!!.games += 1
            games.get(variantKey)!!.games += 1
            games.get(groupKey)!!.games += 1

            games.get(ALL_KEY)!!.time += gameTime
            games.get(variantKey)!!.time += gameTime
            games.get(groupKey)!!.time += gameTime

            games.get(ALL_KEY)!!.turns += turns
            games.get(variantKey)!!.turns += turns
            games.get(groupKey)!!.turns += turns
        }
    }

    override fun save() {
        saveStandard()
        savePrTurn()
    }

    private fun savePrTurn() {
        val file = Array<Array<String?>>(games.size + 1) { arrayOfNulls<String>(2) }  // [y][x]

        // Set labels
        file[0] = arrayOf<String?>("Variant", "Time (s.)")

        // Fill in values
        val keys = ArrayList<String?>(games.keys).toTypedArray()
        Arrays.sort(keys)

        var row = 1
        for (key in keys) {
            val r: Result = games.get(key)!!
            file[row][0] = key
            file[row][1] = (r.time / (r.turns * 1000)).toString()
            row++
        }

        saveStringToDisc(TURN_FILE_NAME, matrixToString(file))
    }

    private fun saveStandard() {
        val file = Array<Array<String?>>(games.size + 1) { arrayOfNulls<String>(2) }  // [y][x]

        // Set labels
        file[0] = arrayOf<String?>("Variant", "Time (s.)")

        // Fill in values
        val keys = ArrayList<String?>(games.keys).toTypedArray()
        Arrays.sort(keys)

        var row = 1
        for (key in keys) {
            val r: Result = games.get(key)!!
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
