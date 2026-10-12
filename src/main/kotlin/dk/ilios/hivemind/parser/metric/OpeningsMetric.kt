package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.parser.BoardspaceGameType


/**
 * Analyzes openings, ie. the first 4 moves for White player
 */
class OpeningsMetric : Metric() {
    private val whiteResults: MutableMap<String, Result> = hashMapOf<String, Result>()
    private val blackResults: MutableMap<String, Result> = hashMapOf<String, Result>()
    private var whiteTotal = 0
    private var blackTotal = 0

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        analyzeWhite(type, variant, game)
        analyzeBlack(type, variant, game)
    }

    private fun analyzeBlack(type: BoardspaceGameType, variant: String, game: Game) {
        if (game.blackPlayer.moves < 4) return
        val sb = StringBuilder()

        // Collect first 4 moves from white
        for (i in 1..4) {
            val command = game.getMove(game.blackPlayer, i)
            if (i > 1) {
                sb.append("-")
            }
            if (command!!.token == null) {
                sb.append("??")
            } else {
                sb.append(command.token.id)
            }
        }

        val opening = sb.toString()
        if (!blackResults.containsKey(opening)) {
            blackResults[opening] = Result(opening, 1)
        } else {
            val r: Result = blackResults[opening]!!
            r.count += 1
            blackResults[opening] = r
        }

        blackTotal++
    }

    private fun analyzeWhite(type: BoardspaceGameType, variant: String, game: Game) {
        if (game.whitePlayer.moves < 4) return
        val sb = StringBuilder()

        // Collect first 4 moves from white
        for (i in 1..4) {
            val command = game.getMove(game.whitePlayer, i)
            if (i > 1) {
                sb.append("-")
            }
            if (command!!.token == null) {
                sb.append("??")
            } else {
                sb.append(command.token.id)
            }
        }

        val opening = sb.toString()
        if (!whiteResults.containsKey(opening)) {
            whiteResults[opening] = Result(opening, 1)
        } else {
            val r: Result = whiteResults[opening]!!
            r.count += 1
            whiteResults[opening] = r
        }

        whiteTotal++
    }

    override fun save() {
        save(WHITE_FILE_NAME, whiteResults, whiteTotal)
        save(BLACK_FILE_NAME, blackResults, blackTotal)
    }

    private fun save(fileName: String?, results: Map<String, Result>, total: Int) {
        val file = Array(results.size + 2) { arrayOfNulls<String>(3) }
        file[0] = arrayOf<String?>("Opening", "Count", "%")

        val keys = results.values.sorted()

        var row = 1
        for (i in keys.indices.reversed()) {
            val r = keys[i]
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
        const val WHITE_FILE_NAME: String = "white_openings.data"
        const val BLACK_FILE_NAME: String = "black_openings.data"
    }
}
