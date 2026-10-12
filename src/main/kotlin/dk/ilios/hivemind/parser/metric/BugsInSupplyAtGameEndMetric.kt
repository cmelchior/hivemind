package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameStatus
import dk.ilios.hivemind.parser.BoardspaceGameType


/**
 * How many bugs are still left in the supply at the end of the game?
 */
class BugsInSupplyAtGameEndMetric : Metric() {
    private val results: MutableMap<Int, Result> = hashMapOf<Int, Result>()
    private val diffResults: MutableMap<Int, Int> = hashMapOf<Int, Int>()
    private var diffTotal = 0

    init {
        results[11] = Result(11)
        results[12] = Result(12)
        results[13] = Result(13)
        results[14] = Result(14)
    }

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        val status = game.status
        if (!status.winnerFound()) return
        var winSupply = -1
        var looseSupply = -1
        if (status == GameStatus.RESULT_BLACK_WINS) {
            winSupply = game.blackPlayer.supply.size
            looseSupply = game.whitePlayer.supply.size
        } else if (status == GameStatus.RESULT_WHITE_WINS) {
            winSupply = game.whitePlayer.supply.size
            looseSupply = game.blackPlayer.supply.size
        }

        val startingBugs = game.requireActivePlayer().noStartingBugs
        results[startingBugs]!!.winner[winSupply] = results[startingBugs]!!.winner[winSupply] + 1
        results[startingBugs]!!.looser[looseSupply] = results[startingBugs]!!.looser[looseSupply] + 1

        val diff = winSupply - looseSupply
        if (diffResults.containsKey(diff)) {
            diffResults[diff] = diffResults[diff]!! + 1
        } else {
            diffResults[diff] = 1
        }
        diffTotal++
    }

    override fun save() {
        saveStandard()
        saveDiff()
    }

    private fun saveStandard() {
        val file = Array(16) { arrayOfNulls<String>(9) }  // [y][x]

        // Set labels
        file[0] = arrayOf<String?>(
            "Supply",
            "Winner(11)",
            "Looser(11)",
            "Winner(12)",
            "Looser(12)",
            "Winner(13)",
            "Looser(13)",
            "Winner(14)",
            "Looser(14)"
        )

        for (i in 11..14) {
            val r: Result = results[i]!!
            val winner = r.winner
            val looser = r.looser

            for (j in winner.indices) {
                file[j + 1][0] = j.toString() // Just override for now
                file[j + 1][(i - 11) * 2 + 1] = winner[j].toString()
                file[j + 1][(i - 11) * 2 + 2] = looser[j].toString()
            }
        }

        saveStringToDisc(FILE_NAME, matrixToString(file))
    }

    private fun saveDiff() {
        val file = Array(diffResults.size + 2) { arrayOfNulls<String>(3) }  // [y][x]

        // Set labels
        file[0] = arrayOf<String?>("Diff", "Count", "Percent")

        // Fill in values
        val keys = diffResults.keys.sorted()

        for (i in keys.indices) {
            file[i + 1][0] = keys[i].toString()
            file[i + 1][1] = diffResults[keys[i]].toString()
            file[i + 1][2] = (diffResults[keys[i]]!! * 100 / diffTotal.toFloat()).toString()
        }

        saveStringToDisc(DIFF_FILE_NAME, matrixToString(file))
    }

    private inner class Result(maxNumber: Int) {
        var winner: IntArray = IntArray(15)
        var looser: IntArray = IntArray(15)
    }

    companion object {
        const val FILE_NAME: String = "bugs_in_supply_at_end.data"
        const val DIFF_FILE_NAME: String = "bugs_in_supply_at_end_diff.data"
    }
}
