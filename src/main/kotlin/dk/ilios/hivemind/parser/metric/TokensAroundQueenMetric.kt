package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.game.GameStatus
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.parser.BoardspaceGameType

/**
 * Keeps track of tokens around queens. Number if counted after player has moved.
 */
class TokensAroundQueenMetric(winnerTurns: Int) : Metric() {
    var winnerData: Array<IntArray?> // [y][x]
    var looserData: Array<IntArray?> // [y][x]
    var gamesWithWinnerTurns: Int // Only look at games where winner had that many turns

    init {
        gamesWithWinnerTurns = winnerTurns
        winnerData = Array<IntArray?>(gamesWithWinnerTurns) { IntArray(7) }  // [y][x] : from -14 to + 14 and and 1 - 16
        looserData = Array<IntArray?>(gamesWithWinnerTurns) { IntArray(7) }  // [y][x] : from -14 to + 14 and and 1 - 16
    }

    override fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game) {
        val status = game.status
        val winner: Player
        val looser: Player
        if (status == GameStatus.RESULT_BLACK_WINS && game.getBlackPlayer().turns == gamesWithWinnerTurns) {
            winner = game.getBlackPlayer()
            looser = game.getWhitePlayer()
        } else if (status == GameStatus.RESULT_WHITE_WINS && game.getWhitePlayer().turns == gamesWithWinnerTurns) {
            winner = game.getWhitePlayer()
            looser = game.getBlackPlayer()
        } else {
            return
        }

        game.setReplayMode(true)
        var lastActive: Player? = null
        for (i in 0..<gamesWithWinnerTurns * 2) {
            val turn = i / 2
            game.forward()
            if (lastActive != null && game.requireActivePlayer() === lastActive) {
                game.togglePlayer() // End of game
            }

            if (game.requireActivePlayer() === looser) {
                val winnerTokensAroundQueen = game.getBoard().getNeighborTokens(looser.queen!!.hex).size
                winnerData[turn]!![winnerTokensAroundQueen] += 1
            } else if (game.requireActivePlayer() === winner) {
                val looserTokensAroundQueen = game.getBoard().getNeighborTokens(winner.queen!!.hex).size
                looserData[turn]!![looserTokensAroundQueen] += 1
            }
            lastActive = game.requireActivePlayer()
        }
    }

    override fun save() {
        save(WINNER_FILE_NAME, winnerData)
        save(LOOSER_FILE_NAME, looserData)
        saveAverages()
    }

    private fun saveAverages() {
        val file: Array<Array<String?>>? = Array<Array<String?>>(winnerData.size + 1) { arrayOfNulls<String>(3) }
        file!![0] = arrayOf<String?>("Turns", "Winner", "Looser")

        for (row in winnerData.indices) {
            file[row + 1][0] = (row + 1).toString()

            var total = 0f
            var games = 0f
            for (x in winnerData[row]!!.indices) {
                games += winnerData[row]!![x].toFloat()
                total += (x * winnerData[row]!![x]).toFloat()
            }
            file[row + 1][1] = (total / games).toString()

            total = 0f
            games = 0f
            for (x in looserData[row]!!.indices) {
                games += looserData[row]!![x].toFloat()
                total += (x * looserData[row]!![x]).toFloat()
            }
            file[row + 1][2] = (total / games).toString()
        }

        saveStringToDisc(String.format(FILE_NAME, gamesWithWinnerTurns), matrixToString(file))
    }

    private fun save(fileName: String, data: Array<IntArray?>) {
        val file: Array<Array<String?>>? = Array<Array<String?>>(data.size + 1) { arrayOfNulls<String>(8) }

        // Labels
        file!![0][0] = "Turns"
        for (i in 1..<file[0].size) {
            file[0][i] = (i - 1).toString()
        }

        for (y in data.indices) {
            file[y + 1][0] = (y + 1).toString()
            for (x in data[y]!!.indices) {
                file[y + 1][x + 1] = data[y]!![x].toString()
            }
        }

        saveStringToDisc(String.format(fileName, gamesWithWinnerTurns), matrixToString(file))
    }

    companion object {
        const val FILE_NAME: String = "tokens_around_queen_%s_turns.data"
        const val WINNER_FILE_NAME: String = "winner_tokens_around_queen_%s_turns.data"
        const val LOOSER_FILE_NAME: String = "looser_tokens_around_queen_%s_turns.data"
    }
}
