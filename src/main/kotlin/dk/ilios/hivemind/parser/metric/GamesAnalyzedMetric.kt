package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.parser.BoardspaceGameType


/**
 * Keeps track of the number of games analyzed in each group.
 */
class GamesAnalyzedMetric : Metric() {
    private val results: MutableMap<String, Int> = hashMapOf<String, Int>()

    private var totalGames = 0

    init {
        results["all"] = 0
    }

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        val variantKey = "all-" + type.prefix
        val groupKey = type.prefix + "-" + variant

        if (!results.containsKey(variantKey)) {
            results[variantKey] = 0
        }

        if (!results.containsKey(groupKey)) {
            results[groupKey] = 0
        }

        results["all"] = results["all"]!! + 1
        results[variantKey] = results[variantKey]!! + 1
        results[groupKey] = results[groupKey]!! + 1

        totalGames++
    }

    override fun save() {
        val file = Array(results.size + 1) { arrayOfNulls<String>(4) }

        // Labels
        file[0] = arrayOf<String?>("Type", "Count", "%")

        // Add game types in sorted alphabetical order
        val keys = results.keys.sorted()

        for (i in keys.indices) {
            file[i + 1][0] = (i + 1).toString()
            file[i + 1][1] = keys[i]
            file[i + 1][2] = results[keys[i]].toString()
            file[i + 1][3] = (results[keys[i]]!! * 100 / totalGames.toFloat()).toString()
        }

        saveStringToDisc(FILE_NAME, matrixToString(file))
    }

    companion object {
        const val FILE_NAME: String = "games_analyzed.data"
    }
}
