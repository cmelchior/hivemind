package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameStatus
import dk.ilios.hivemind.parser.BoardspaceGameType
import java.util.*

class WinnerOpeningMetric : Metric() {
    private val whiteResults: MutableMap<String?, Result> = HashMap<String?, Result>()
    private val blackResults: MutableMap<String?, Result> = HashMap<String?, Result>()

    private var whiteTotal = 0
    private var blackTotal = 0

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        if (game.getWhitePlayer().moves < 4 || game.getBlackPlayer().moves < 4) return
        if (game.status != GameStatus.RESULT_WHITE_WINS && game.status != GameStatus.RESULT_BLACK_WINS) return
        val sb = StringBuilder()

        val isWhite = (game.status == GameStatus.RESULT_WHITE_WINS)
        val p = if (isWhite) game.getWhitePlayer() else game.getBlackPlayer()

        // Collect first 4 moves from player
        for (i in 1..4) {
            val command = game.getMove(p, i)
            if (i > 1) {
                sb.append("-")
            }
            sb.append(command!!.token!!.id)
        }

        val opening = sb.toString()
        val map = if (isWhite) whiteResults else blackResults

        if (!map.containsKey(opening)) {
            map.put(opening, Result(opening, 1))
        } else {
            val r: Result = map.get(opening)!!
            r.count += 1
            map.put(opening, r)
        }

        if (isWhite) {
            whiteTotal++
        } else {
            blackTotal++
        }
    }

    override fun save() {
        save(WHITE_FILE_NAME, whiteResults, whiteTotal)
        save(BLACK_FILE_NAME, blackResults, blackTotal)
    }

    private fun save(fileName: String?, results: MutableMap<String?, Result>, total: Int) {
        val file = Array<Array<String?>>(results.size + 2) { arrayOfNulls<String>(3) }
        file[0] = arrayOf<String?>("Opening", "Count", "%")

        val keys = ArrayList<Result?>(results.values).toTypedArray()
        Arrays.sort(keys)

        var row = 1
        for (i in keys.indices.reversed()) {
            val r = keys[i] as Result
            file[row][0] = r.key
            file[row][1] = r.count.toString()
            file[row][2] = (r.count * 100 / total.toFloat()).toString()
            row++
        }

        file[row] = arrayOf<String?>("Total", total.toString())
        saveStringToDisc(fileName, matrixToString(file))
    }

    private inner class Result(opening: String, count: Int) : Comparable<Any?> {
        var key: String
        var count: Int

        init {
            this.key = opening
            this.count = count
        }

        override fun compareTo(other: Any?): Int {
            if (other !is Result) return 1

            val r2 = other
            if (count == r2.count) {
                return key.compareTo(r2.key)
            } else {
                if (count < r2.count) {
                    return -1
                } else {
                    return 1
                }
            }
        }
    }

    companion object {
        const val WHITE_FILE_NAME: String = "white_winner_openings.data"
        const val BLACK_FILE_NAME: String = "black_winner_openings.data"
    }
}
