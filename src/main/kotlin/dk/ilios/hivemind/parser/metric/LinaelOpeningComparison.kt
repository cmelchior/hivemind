package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameStatus
import dk.ilios.hivemind.model.Hex
import dk.ilios.hivemind.model.StandardPositionMode
import dk.ilios.hivemind.parser.BoardspaceGameType
import java.util.*

/**
 * Compare all opening variants when looking at the first 3 tokens for both sides. No moves are allowed.
 */
class LinaelOpeningComparison : Metric() {
    private val allResults = HashMap<String?, Result>()
    private val whiteResults = HashMap<String?, Result>()
    private val blackResults = HashMap<String?, Result>()
    private var totalGames = 0

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        if (game.getWhitePlayer().turns < 3 || game.getBlackPlayer()
                .turns < 3
        ) return  // Both players must have had 3 turns


        val status = game.status
        game.setReplayMode(true)
        game.setStandardPositionMode(StandardPositionMode.LIMITED)
        val hexes: Array<Hex?> = arrayOfNulls<Hex>(6)
        val whiteHexes: Array<Hex?> = arrayOfNulls<Hex>(3)
        val blackHexes: Array<Hex?> = arrayOfNulls<Hex>(3)
        for (i in 0..5) {
            val gc = game.forward()
            if (gc.fromQ != Hex.SUPPLY) return  // We only care about games where players put tokens on the board for the first 3 turns.

            val spCoords = game.getBoard().getSPCoordinatesFor(gc.toQ, gc.toR)
            hexes[i] = Hex(spCoords[0], spCoords[1])
            if (gc.token!!.player.isWhitePlayer) {
                whiteHexes[i / 2] = Hex(spCoords[0], spCoords[1])
            } else {
                blackHexes[i / 2] = Hex(spCoords[0], spCoords[1])
            }
        }

        val allKey = createKey(hexes.map { checkNotNull(it) }.toTypedArray())
        val whiteKey = createKey(whiteHexes.map { checkNotNull(it) }.toTypedArray())
        val blackKey = createKey(blackHexes.map { checkNotNull(it) }.toTypedArray())

        saveResults(allKey, status, allResults, game)
        saveResults(whiteKey, status, whiteResults, game)
        saveResults(blackKey, status, blackResults, game)

        totalGames++
    }

    private fun saveResults(key: String?, status: GameStatus, results: HashMap<String?, Result>, game: Game) {
        var result = results.get(key)
        if (result == null) {
            val example = printer.toString(game.getBoard())
            println((results.size + 1).toString() + ". " + key)
            println(example)
            result = Result(example)
            results.put(key, result)
        }

        when (status) {
            GameStatus.RESULT_NOT_STARTED, GameStatus.RESULT_MATCH_IN_PROGRESS, GameStatus.RESULT_TURN_LIMIT_REACHED, GameStatus.RESULT_TIME_LIMIT_REACHED -> result.other++
            GameStatus.RESULT_DRAW -> result.draw++
            GameStatus.RESULT_BLACK_WINS -> result.blackWin++
            GameStatus.RESULT_WHITE_WINS -> result.whiteWin++
        }
    }


    private fun createKey(hexes: Array<Hex>): String {
        Arrays.sort(hexes)
        val key = StringBuilder()
        for (hex in hexes) {
            key.append('(')
            key.append(hex.q)
            key.append(',')
            key.append(hex.r)
            key.append(')')
        }
        return key.toString()
    }

    override fun save() {
        save(FILE_NAME, allResults)
        save(FILE_NAME_WHITE, whiteResults)
        save(FILE_NAME_BLACK, blackResults)
    }

    private fun save(fileName: String?, results: HashMap<String?, Result>) {
        val sb = StringBuilder()
        sb.append("Total games: " + totalGames + "\n")
        sb.append("WhiteWin\tBlackWin\tDraw\tOther\tTotal\n")
        sb.append("---------------------------\n")
        for (result in results.values) {
            sb.append(result.example)
            sb.append('\n')
            sb.append(
                result.whiteWin.toString() + '\t' + result.blackWin.toString() + '\t' + result.draw.toString() + '\t' + result.other.toString() + '\t' + (result.total()
                    .toInt()).toString() + '\n'
            )

            val whiteWin: String? =
                if ((result.total()) > 0) (result.whiteWin * 100 / result.total()).toString() else "0"
            val blackWin: String? =
                if ((result.total()) > 0) (result.blackWin * 100 / result.total()).toString() else "0"
            val draw: String? = if ((result.total()) > 0) (result.draw * 100 / result.total()).toString() else "0"
            val other: String? = if ((result.total()) > 0) (result.other * 100 / result.total()).toString() else "0"
            sb.append(whiteWin + '\t' + blackWin + '\t' + draw + '\t' + other + '\t' + '\n')

            sb.append("---------------------------\n")
        }
        saveStringToDisc(fileName, sb.toString())
    }

    private inner class Result(example: String?) {
        var example: String?
        var whiteWin: Int = 0
        var blackWin: Int = 0
        var draw: Int = 0
        var other: Int = 0

        init {
            this.example = example
        }

        fun total(): Float {
            val total = (whiteWin + blackWin + draw + other).toFloat()
            return total
        }
    }

    companion object {
        private const val FILE_NAME = "linael_opening_comparison.data"
        private const val FILE_NAME_WHITE = "linael_white_opening_comparison.data"
        private const val FILE_NAME_BLACK = "linael_black_opening_comparison.data"
    }
}
