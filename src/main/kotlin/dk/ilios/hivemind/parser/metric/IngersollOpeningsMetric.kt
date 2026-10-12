package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameStatus
import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.BugType
import dk.ilios.hivemind.model.HexagonUtils
import dk.ilios.hivemind.parser.BoardspaceGameType


/**
 * Staticstics for openings defined by Randy Ingersoll in "Play Hive like a Champion"
 *
 * Win/Loose/Draw/Other is tracked for each opening type: C, Z, I, J, F, X
 *
 */
class IngersollOpeningsMetric : Metric() {
    private val results: MutableMap<String, Result> = hashMapOf<String, Result>()

    init {
        results["C"] = Result()
        results["Z"] = Result()
        results["I"] = Result()
        results["J"] = Result()
        results["F"] = Result()
        results["X"] = Result()
        results["?"] = Result()
        results["%"] = Result()
    }

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        game.setReplayMode(true)
        val openingType = getOpeningType(game)
        val status = game.status
        when (status) {
            GameStatus.RESULT_DRAW -> results[openingType]!!.draw++
            GameStatus.RESULT_BLACK_WINS -> results[openingType]!!.blackWin++
            GameStatus.RESULT_WHITE_WINS -> results[openingType]!!.whiteWin++
            else -> results[openingType]!!.other++
        }
    }

    private fun getOpeningType(game: Game): String {
        // Forward game until both queens are placed.

        var i = 10
        while ((game.whitePlayer.queen!!.inSupply() || game.blackPlayer.queen!!.inSupply()) && i > 0) {
            game.forward()
            i--
        }

        if ((game.whitePlayer.queen!!.inSupply() || game.blackPlayer.queen!!.inSupply())) {
            return "%"
        }

        if (isC(game)) {
            return "C"
        } else if (isZ(game)) {
            return "Z"
        } else if (isI(game)) {
            return "I"
        } else if (isJ(game)) {
            return "J"
        } else if (isF(game)) {
            return "F"
        } else if (isX(game)) {
            return "X"
        } else {
            return "?"
        }
    }

    private fun isX(game: Game): Boolean {
        val startingWhiteHex = game.getMove(game.whitePlayer, 1)!!.token!!.hex
        val whiteQueenHex = game.whitePlayer.queen!!.hex
        val startingBlackHex = game.getMove(game.blackPlayer, 1)!!.token!!.hex
        val blackQueenHex = game.blackPlayer.queen!!.hex

        if (HexagonUtils.distance(
                startingWhiteHex!!.q,
                startingWhiteHex.r,
                whiteQueenHex!!.q,
                whiteQueenHex.r
            ) > 1
            || HexagonUtils.distance(
                startingBlackHex!!.q,
                startingBlackHex.r,
                blackQueenHex!!.q,
                blackQueenHex.r
            ) > 1
        ) {
            return true
        } else {
            return false
        }
    }

    private fun isF(game: Game): Boolean =
        checkTokens(1, 0, 2, -1, 3, -2, game.board) || checkTokens(1, -1, 2, -1, 3, -1, game.board)

    private fun isJ(game: Game): Boolean =
        checkTokens(1, 0, 2, 0, 3, -1, game.board) || checkTokens(1, -1, 2, -2, 3, -2, game.board)

    private fun isI(game: Game): Boolean = checkTokens(1, 0, 2, 0, 3, 0, game.board)

    private fun isZ(game: Game): Boolean =
        checkTokens(1, 0, 2, -1, 3, -1, game.board) || checkTokens(1, -1, 2, -1, 3, -2, game.board)

    private fun isC(game: Game): Boolean =
        checkTokens(1, -1, 2, -1, 2, 0, game.board) || checkTokens(0, 1, 1, 1, 2, 0, game.board)

    private fun checkTokens(q1: Int, r1: Int, q2: Int, r2: Int, q3: Int, r3: Int, b: Board): Boolean {
        val t1 = b.getHexForStandardPosition(q1, r1).topToken
        val t2 = b.getHexForStandardPosition(q2, r2).topToken
        val t3 = b.getHexForStandardPosition(q3, r3).topToken

        if (t1 == null || t2 == null || t3 == null) {
            return false
        }

        if (!t1.player.isWhitePlayer) {
            return false
        }

        if (!t2.player.isBlack) {
            return false
        }

        if (!t3.player.isBlack || t3.originalType != BugType.QUEEN_BEE) {
            return false
        }

        return true
    }

    override fun save() {
        results.remove("%")
        savePercentage()
        saveCount()
    }

    private fun saveCount() {
        val sb = StringBuilder()
        val labels = StringBuilder("label")
        val white = StringBuilder("white-wins")
        val black = StringBuilder("black-wins")
        val draw = StringBuilder("draw")
        val other = StringBuilder("other")

        val keys = results.keys.sorted()

        for (key in keys) {
            val r: Result = results[key]!!
            labels.append('\t').append(key)
            white.append('\t').append(r.whiteWin)
            black.append('\t').append(r.blackWin)
            draw.append('\t').append(r.draw)
            other.append('\t').append(r.other)
        }

        sb.append(labels.toString()).append('\n')
        sb.append(white.toString()).append('\n')
        sb.append(black.toString()).append('\n')
        sb.append(draw.toString()).append('\n')
        sb.append(other.toString()).append('\n')

        saveStringToDisc(COUNT_FILE_NAME, sb.toString())
    }

    private fun savePercentage() {
        val sb = StringBuilder()
        val labels = StringBuilder("label")
        val white = StringBuilder("white-wins")
        val black = StringBuilder("black-wins")
        val draw = StringBuilder("draw")
        val other = StringBuilder("other")

        val keys = results.keys.sorted()

        for (key in keys) {
            val r: Result = results[key]!!
            labels.append('\t').append(key)
            white.append('\t').append(if (r.total > 0) r.whiteWin * 100 / r.total else "0.0")
            black.append('\t').append(if (r.total > 0) r.blackWin * 100 / r.total else "0.0")
            draw.append('\t').append(if (r.total > 0) r.draw * 100 / r.total else "0.0")
            other.append('\t').append(if (r.total > 0) r.other * 100 / r.total else "0.0")
        }

        sb.append(labels.toString()).append('\n')
        sb.append(white.toString()).append('\n')
        sb.append(black.toString()).append('\n')
        sb.append(draw.toString()).append('\n')
        sb.append(other.toString()).append('\n')

        saveStringToDisc(PERCENTAGE_FILE_NAME, sb.toString())
    }

    private inner class Result {
        var whiteWin: Int = 0
        var blackWin: Int = 0
        var draw: Int = 0
        var other: Int = 0

        val total: Float
            get() = whiteWin.toFloat() + blackWin + draw + other
    }

    companion object {
        const val COUNT_FILE_NAME: String = "game_openings_vs_results_count.data"
        const val PERCENTAGE_FILE_NAME: String = "game_openings_vs_results_percentage.data"
    }
}
