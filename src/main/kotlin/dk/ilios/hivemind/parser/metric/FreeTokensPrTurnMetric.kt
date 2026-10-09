package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameStatus
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.rules.Rules
import dk.ilios.hivemind.parser.BoardspaceGameType
import kotlin.math.max
import kotlin.math.min

/**
 * Keeps track of the difference between free tokens between winner and looser.
 * Free tokens are counted at the beginning of each of the winners turns.
 */
class FreeTokensPrTurnMetric(winnerTurns: Int) : Metric() {
    var whiteData: Array<IntArray?> // [y][x]
    var blackData: Array<IntArray?> // [y][x]
    var gamesWithWinnerTurns: Int // Only look at games where winner had that many turns

    init {
        gamesWithWinnerTurns = winnerTurns
        whiteData = Array<IntArray?>(gamesWithWinnerTurns) { IntArray(29) }  // [y][x] : from -14 to + 14 and and 1 - 16
        blackData = Array<IntArray?>(gamesWithWinnerTurns) { IntArray(29) }  // [y][x] : from -14 to + 14 and and 1 - 16
    }

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        val status = game.status
        var p: Player? = null
        var data: Array<IntArray?>? = null
        if (status == GameStatus.RESULT_BLACK_WINS && game.getBlackPlayer().turns == gamesWithWinnerTurns) {
            data = blackData
            p = game.getBlackPlayer()
            game.setReplayMode(true)
            game.forward()
        } else if (status == GameStatus.RESULT_WHITE_WINS && game.getWhitePlayer().turns == gamesWithWinnerTurns) {
            data = whiteData
            p = game.getWhitePlayer()
            game.setReplayMode(true)
        } else {
            return
        }

        for (i in 0..<gamesWithWinnerTurns) {
            val winnerFreeTokens: Int =
                Rules.instance.getFreeTokens(game.requireActivePlayer(), game.getBoard()).size
            val looserFreeTokens: Int =
                Rules.instance.getFreeTokens(game.otherPlayer, game.getBoard()).size
            val diff = winnerFreeTokens - looserFreeTokens
            data[i]!![diff + 14] = data[i]!![diff + 14] + 1

            if (i < gamesWithWinnerTurns - 1) {
                game.forward()
                game.forward()
            }
        }
    }

    override fun save() {
        save(WHITE_FILE_NAME, whiteData)
        save(BLACK_FILE_NAME, blackData)
    }

    private fun save(fileName: String, data: Array<IntArray?>) {
        var leftBound = data[0]!!.size - 1
        var rightBound = 0

        for (y in data.indices) {
            for (x in data[y]!!.indices) {
                if (data[y]!![x] > 0) {
                    leftBound = min(leftBound, x)
                    rightBound = max(rightBound, x)
                }
            }
        }

        val file: Array<Array<String?>>? =
            Array<Array<String?>>(data.size + 1) { arrayOfNulls<String>(rightBound - leftBound + 2) }

        // Labels
        file!![0][0] = "Turns"
        for (i in leftBound..rightBound) {
            file[0][i - leftBound + 1] = (i - 14).toString()
        }

        for (y in data.indices) {
            file[y + 1][0] = (y + 1).toString()
            for (x in leftBound..rightBound) {
                file[y + 1][x - leftBound + 1] = data[y]!![x].toString()
            }
        }

        saveStringToDisc(String.format(fileName, gamesWithWinnerTurns), matrixToString(file))
    }

    companion object {
        const val WHITE_FILE_NAME: String = "white_winner_free_tokens_during_games_%s_turns.data"
        const val BLACK_FILE_NAME: String = "black_winner_free_tokens_during_games_%s_turns.data"
    }
}
