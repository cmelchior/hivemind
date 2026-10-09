package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.ai.HiveAI
import dk.ilios.hivemind.ai.IDDFSAlphaBetaMiniMaxAI
import dk.ilios.hivemind.ai.heuristics.SimpleHeuristicV3
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameStatus
import dk.ilios.hivemind.parser.BoardspaceGameType
import java.util.*

/**
 * Reports game result, ie. which color won or draw.
 */
class GameResultPrColorMetric(predictLastMoves: Boolean) : Metric() {
    private val results: MutableMap<String?, Result> = HashMap<String?, Result>()

    private val ai: HiveAI = IDDFSAlphaBetaMiniMaxAI("WinnerAI", SimpleHeuristicV3(), 2, 20)
    private var predictMoves = false // If true, the next two moves are predicted if the game has not ended

    init {
        predictMoves = predictLastMoves
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

        val status = game.status
        when (status) {
            GameStatus.RESULT_DRAW -> {
                allResults.draw++
                variantResults.draw++
                groupResults.draw++
            }

            GameStatus.RESULT_BLACK_WINS -> {
                allResults.blackWin++
                variantResults.blackWin++
                groupResults.blackWin++
            }

            GameStatus.RESULT_WHITE_WINS -> {
                allResults.whiteWin++
                variantResults.whiteWin++
                groupResults.whiteWin++
            }

            else -> {
                // Try to play two more turns to determine if the game was ended because a win was "sure"
                if (predictMoves) {
                    game.continueGame(ai.nextMove(game, game.getBoard()))
                    game.continueGame(ai.nextMove(game, game.getBoard()))
                }

                if (game.status == GameStatus.RESULT_WHITE_WINS) {
                    allResults.whiteWin++
                    variantResults.whiteWin++
                    groupResults.whiteWin++
                } else if (game.status == GameStatus.RESULT_BLACK_WINS) {
                    allResults.blackWin++
                    variantResults.blackWin++
                    groupResults.blackWin++
                } else {
                    allResults.other++
                    variantResults.other++
                    groupResults.other++
                }
            }
        }
    }

    override fun save() {
        val sb = StringBuilder()
        val labels = StringBuilder("label")
        val white = StringBuilder("white-wins")
        val black = StringBuilder("black-wins")
        val draw = StringBuilder("draw")
        val other = StringBuilder("other")

        val keys = ArrayList<String?>(results.keys).toTypedArray()
        Arrays.sort(keys)

        for (key in keys) {
            val r: Result = results.get(key)!!
            labels.append('\t').append(key)
            white.append('\t').append(r.whiteWin * 100 / r.total)
            black.append('\t').append(r.blackWin * 100 / r.total)
            draw.append('\t').append(r.draw * 100 / r.total)
            other.append('\t').append(r.other * 100 / r.total)
        }

        sb.append(labels.toString()).append('\n')
        sb.append(white.toString()).append('\n')
        sb.append(black.toString()).append('\n')
        sb.append(draw.toString()).append('\n')
        sb.append(other.toString()).append('\n')

        saveStringToDisc(FILE_NAME, sb.toString())
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
        const val FILE_NAME: String = "game_results.data"
    }
}
