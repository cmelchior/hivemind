package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameStatus
import dk.ilios.hivemind.parser.BoardspaceGameType
import java.util.*

/**
 * How many bugs are on the board at the end of the game?
 */
class BugsOnBoardAtGameEndMetric : Metric() {
    private val results: MutableMap<Int?, Result> = HashMap<Int?, Result>()
    private val diffResults: MutableMap<Int?, Int?> = HashMap<Int?, Int?>()
    private var diffTotal = 0

    init {
        results.put(11, Result())
        results.put(12, Result())
        results.put(13, Result())
        results.put(14, Result())
    }

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        val status = game.status
        if (!status.winnerFound()) return
        var winBoard = -1
        var looseBoard = -1
        val black = game.getBlackPlayer()
        val white = game.getWhitePlayer()
        if (status == GameStatus.RESULT_BLACK_WINS) {
            winBoard = black.noStartingBugs - black.supply.size
            looseBoard = white.noStartingBugs - white.supply.size
        } else if (status == GameStatus.RESULT_WHITE_WINS) {
            winBoard = white.noStartingBugs - white.supply.size
            looseBoard = black.noStartingBugs - black.supply.size
        }

        val startingBugs = game.requireActivePlayer().noStartingBugs
        results.get(startingBugs)!!.winner[winBoard] = results.get(startingBugs)!!.winner[winBoard] + 1
        results.get(startingBugs)!!.looser[looseBoard] = results.get(startingBugs)!!.looser[looseBoard] + 1

        val diff = winBoard - looseBoard
        if (diffResults.containsKey(diff)) {
            diffResults.put(diff, diffResults.get(diff)!! + 1)
        } else {
            diffResults.put(diff, 1)
        }
        diffTotal++
    }

    override fun save() {
        saveStandard()
        saveDiff()
    }

    private fun saveStandard() {
        val file = Array<Array<String?>>(16) { arrayOfNulls<String>(9) }  // [y][x]

        // Set labels
        file[0] = arrayOf<String?>(
            "Board",
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
            val r: Result = results.get(i)!!
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
        val file = Array<Array<String?>>(diffResults.size + 1) { arrayOfNulls<String>(3) }  // [y][x]

        // Set labels
        file[0] = arrayOf<String?>("Diff", "Count", "Percent")

        // Fill in values
        val keys = ArrayList<Int?>(diffResults.keys).toTypedArray()
        Arrays.sort(keys)

        for (i in keys.indices) {
            file[i + 1][0] = keys[i].toString()
            file[i + 1][1] = diffResults.get(keys[i]).toString()
            file[i + 1][2] = (diffResults.get(keys[i])!! * 100 / diffTotal.toFloat()).toString()
        }

        saveStringToDisc(DIFF_FILE_NAME, matrixToString(file))
    }

    private inner class Result {
        var winner: IntArray = IntArray(15)
        var looser: IntArray = IntArray(15)
    }

    companion object {
        const val FILE_NAME: String = "bugs_on_board_at_end.data"
        const val DIFF_FILE_NAME: String = "bugs_on_board_at_end_diff.data"
    }
}
