package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameCommand
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.parser.BoardspaceGameType
import java.util.*

class LeastMovedTokenMetric : Metric() {
    private var results: MutableMap<String?, Int?> = HashMap<String?, Int?>()
    private val weightedResults: MutableMap<Int?, MutableMap<String?, Int?>?> =
        HashMap<Int?, MutableMap<String?, Int?>?>()

    init {
        results = constructTokenMap()
        for (i in 5..8) {
            weightedResults.put(i, constructTokenMap())
        }
    }

    private fun constructTokenMap(): MutableMap<String?, Int?> {
        val map = HashMap<String?, Int?>()
        for (key in keys) {
            map.put(key, 0)
        }

        return map
    }

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        game.setReplayMode(true)
        while (true) {
            val command = game.forward()
            if (command.fromQ == Hex.SUPPLY) continue

            val tokenType = command.token!!.id.substring(0, 1)
            val tokenTypesNo = getNumberOfDifferentTokens(variant)

            results.put(tokenType, results.get(tokenType)!! + 1)
            results.put(TOTAL_KEY, results.get(TOTAL_KEY)!! + 1)
            weightedResults.get(tokenTypesNo)!!.put(tokenType, weightedResults.get(tokenTypesNo)!!.get(tokenType)!! + 1)
            weightedResults.get(tokenTypesNo)!!.put(TOTAL_KEY, weightedResults.get(tokenTypesNo)!!.get(TOTAL_KEY)!! + 1)
        }
    }

    private fun getNumberOfDifferentTokens(variant: String): Int {
        val parts: Array<String?> = variant.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size == 1) {
            return 5
        } else {
            return 5 + parts[1]!!.length
        }
    }

    override fun save() {
        saveNormalResults()
        saveWeightedResults()
    }

    private fun saveNormalResults() {
        val file = Array<Array<String?>>(results.size) { arrayOfNulls<String>(3) }  // [y][x]

        // Fill in values
        val keys = ArrayList<String?>(results.keys).toTypedArray()
        Arrays.sort(keys)

        // Set labels
        for (i in keys.indices) {
            file[i][0] = (i + 1).toString()
            file[i][1] = keys[i]
            file[i][2] = results.get(keys[i]).toString()
        }

        saveStringToDisc(FILE_NAME, matrixToString(file))
    }

    private fun saveWeightedResults() {
        val output: MutableMap<String?, Float?> = HashMap<String?, Float?>()

        for (key in keys) {
            var tokenChosen = 0f
            var totalGames = 0f

            for (i in 5..8) {
                tokenChosen += weightedResults.get(i)!!.get(key)!!.toFloat()
                totalGames += weightedResults.get(i)!!.get(TOTAL_KEY)!!.toFloat()
            }

            output.put(key, tokenChosen / totalGames)
        }

        val file = Array<Array<String?>>(output.size) { arrayOfNulls<String>(3) }  // [y][x]

        // Fill in values
        val keys = ArrayList<String?>(output.keys).toTypedArray()
        Arrays.sort(keys)

        // Set labels
        for (i in keys.indices) {
            file[i][0] = (i + 1).toString()
            file[i][1] = keys[i]
            file[i][2] = (output.get(keys[i])!! * 100).toString()
        }

        saveStringToDisc(WEIGHTED_FILE_NAME, matrixToString(file))
    }

    companion object {
        private const val DEBUG = false

        const val FILE_NAME: String = "least_moved_token.data"
        const val WEIGHTED_FILE_NAME: String = "weighted_least_moved_token.data"
        const val TOTAL_KEY: String = "Total"
        val keys = arrayOf<String>("A", "G", "B", "S", "Q", "M", "L", "P", TOTAL_KEY)
    }
}
