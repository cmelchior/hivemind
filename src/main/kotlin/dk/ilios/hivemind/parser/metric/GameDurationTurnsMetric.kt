package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameStatus
import dk.ilios.hivemind.parser.BoardspaceGameType
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Tracks how many turns a player has before the game is over.
 */
class GameDurationTurnsMetric : Metric() {
    private var maxTurn = Int.MIN_VALUE
    private var minTurn = Int.MAX_VALUE
    private val results: MutableMap<String?, ArrayList<Int?>> = HashMap<String?, ArrayList<Int?>>()

    init {
        results.put(ALL_KEY, ArrayList<Int?>())
    }

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        val variantKey: String = ALL_KEY + "-" + type.prefix
        val groupKey = type.prefix + "-" + variant

        if (!results.containsKey(variantKey)) {
            results.put(variantKey, ArrayList<Int?>())
        }

        if (!results.containsKey(groupKey)) {
            results.put(groupKey, ArrayList<Int?>())
        }

        // Only consider games we know has ended
        val status = game.status
        if (status == GameStatus.RESULT_WHITE_WINS || status == GameStatus.RESULT_BLACK_WINS || status == GameStatus.RESULT_DRAW) {
            val lastPlayerTurns = checkNotNull(game.otherPlayer).turns // Toggle back to the last player to move.
            maxTurn = max(lastPlayerTurns, maxTurn)
            minTurn = min(lastPlayerTurns, minTurn)

            ensureSize(results.getValue(ALL_KEY), lastPlayerTurns + 1)
            ensureSize(results.getValue(variantKey), lastPlayerTurns + 1)
            ensureSize(results.getValue(groupKey), lastPlayerTurns + 1)

            val currentAllCounter = results.get(ALL_KEY)!!.get(lastPlayerTurns)!!
            val currentVariantCounter = results.get(variantKey)!!.get(lastPlayerTurns)!!
            val currentGroupCounter = results.get(groupKey)!!.get(lastPlayerTurns)!!

            results.get(ALL_KEY)!!.set(lastPlayerTurns, currentAllCounter + 1)
            results.get(variantKey)!!.set(lastPlayerTurns, currentVariantCounter + 1)
            results.get(groupKey)!!.set(lastPlayerTurns, currentGroupCounter + 1)
        }
    }

    override fun save() {
        val file = Array<Array<String?>>(maxTurn - minTurn + 2) { arrayOfNulls<String>(results.size + 1) }  // [y][x]

        // Set labels
        file[0][0] = "turn"
        for (i in minTurn..maxTurn) {
            file[i - minTurn + 1][0] = i.toString()
        }

        // Fill in values
        val keys = ArrayList<String?>(results.keys).toTypedArray()
        Arrays.sort(keys)

        var col = 1
        for (key in keys) {
            val gameEndList: MutableList<Int?> = results.get(key)!!
            file[0][col] = key
            for (turn in minTurn..maxTurn) {
                file[turn - minTurn + 1][col] = if (gameEndList.size > turn) gameEndList.get(turn).toString() else "0"
            }
            col++
        }

        saveStringToDisc(FILE_NAME, matrixToString(file))
    }

    companion object {
        const val FILE_NAME: String = "game_turns.data"
        private const val ALL_KEY = "all"
    }
}
