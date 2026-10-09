package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.parser.BoardspaceGameType
import java.util.*

/**
 * Count how many high ranked games (1600+) exists in each category.
 */
class HighRankedGamesMetric : Metric() {
    private val results: MutableMap<String?, Result> = HashMap<String?, Result>()

    init {
        results.put("all", Result())
    }

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        val variantKey = "all-" + type.prefix
        val groupKey = type.prefix + "-" + variant

        if (!results.containsKey(variantKey)) {
            results.put(variantKey, Result())
        }

        if (!results.containsKey(groupKey)) {
            results.put(groupKey, Result())
        }

        val allResults: Result = results.get("all")!!
        val variantResults: Result = results.get(variantKey)!!
        val groupResults: Result = results.get(groupKey)!!

        if (game.getWhitePlayer().ranking >= 0 && game.getBlackPlayer().ranking >= 0) {
            allResults.games++
            variantResults.games++
            groupResults.games++
        }
    }

    override fun save() {
        val file = Array<Array<String?>>(results.size + 1) { arrayOfNulls<String>(3) }

        // Labels
        file[0] = arrayOf<String?>("Type", "Count")

        // Add game types in sorted alphabetical order
        val keys = ArrayList<String?>(results.keys).toTypedArray()
        Arrays.sort(keys)

        for (i in keys.indices) {
            file[i + 1][0] = (i + 1).toString()
            file[i + 1][1] = keys[i]
            file[i + 1][2] = results.get(keys[i])!!.games.toString()
        }

        saveStringToDisc(FILE_NAME, matrixToString(file))
    }

    private inner class Result {
        var games: Int = 0
    }

    companion object {
        const val FILE_NAME: String = "high_ranked_games.data"
    }
}
