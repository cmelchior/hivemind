package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.parser.BoardspaceGameType


/**
 * What token is the first to be placed by the starting (white) player.
 */
class OpeningTokenMetric : Metric() {
    private val results = constructTokenMap()
    private val weightedResults: Map<Int, MutableMap<String, Int>> =
        (5..8).associateWith { constructTokenMap() }

    private fun constructTokenMap(): MutableMap<String, Int> =
        keys.associateWithTo(hashMapOf()) { 0 }

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        val c = game.getMove(game.whitePlayer, 1)
        if (c != null) {
            val tokenType = c.token!!.id.substring(0, 1)
            val tokenTypesNo = getNumberOfDifferentTokens(variant)

            results[tokenType] = results[tokenType]!! + 1
            results[TOTAL_KEY] = results[TOTAL_KEY]!! + 1
            weightedResults[tokenTypesNo]!![tokenType] = weightedResults[tokenTypesNo]!![tokenType]!! + 1
            weightedResults[tokenTypesNo]!![TOTAL_KEY] = weightedResults[tokenTypesNo]!![TOTAL_KEY]!! + 1
        } else {
            if (DEBUG) println(
                "No move found: " + game.name + ", turns: " + (game.whitePlayer
                    .turns + game.blackPlayer.turns)
            )
        }
    }

    private fun getNumberOfDifferentTokens(variant: String): Int {
        val parts = variant.split("-").dropLastWhile { it.isEmpty() }
        if (parts.size == 1) {
            return 5
        } else {
            return 5 + parts[1].length
        }
    }

    override fun save() {
        saveNormalResults()
        saveWeightedResults()
    }

    private fun saveNormalResults() {
        val file = Array(results.size) { arrayOfNulls<String>(3) }  // [y][x]

        // Fill in values
        val keys = results.keys.sorted()

        // Set labels
        for (i in keys.indices) {
            file[i][0] = (i + 1).toString()
            file[i][1] = keys[i]
            file[i][2] = results[keys[i]].toString()
        }

        saveStringToDisc(FILE_NAME, matrixToString(file))
    }

    private fun saveWeightedResults() {
        val output: MutableMap<String, Float> = hashMapOf<String, Float>()

        for (key in keys) {
            var tokenChosen = 0f
            var totalGames = 0f

            for (i in 5..8) {
                tokenChosen += weightedResults[i]!![key]!!.toFloat()
                totalGames += weightedResults[i]!![TOTAL_KEY]!!.toFloat()
            }

            output[key] = tokenChosen / totalGames
        }

        val file = Array(output.size) { arrayOfNulls<String>(3) }  // [y][x]

        // Fill in values
        val keys = output.keys.sorted()

        // Set labels
        for (i in keys.indices) {
            file[i][0] = (i + 1).toString()
            file[i][1] = keys[i]
            file[i][2] = (output[keys[i]]!! * 100).toString()
        }

        saveStringToDisc(WEIGHTED_FILE_NAME, matrixToString(file))
    }

    companion object {
        private const val DEBUG = false

        const val FILE_NAME: String = "opening_token.data"
        const val WEIGHTED_FILE_NAME: String = "weighted_opening_token.data"
        const val TOTAL_KEY: String = "Total"
        val keys = listOf("A", "G", "B", "S", "Q", "M", "L", "P", TOTAL_KEY)
    }
}
