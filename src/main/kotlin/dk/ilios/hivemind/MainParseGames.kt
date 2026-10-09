package dk.ilios.hivemind

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.parser.BoardspaceGameParser
import dk.ilios.hivemind.parser.BoardspaceGameType
import dk.ilios.hivemind.parser.filters.Filter
import dk.ilios.hivemind.parser.filters.QueenAsStartingPieceFilter
import dk.ilios.hivemind.parser.metric.LinaelOpeningComparison
import dk.ilios.hivemind.parser.metric.Metric
import java.io.File
import java.util.*

class MainParseGames {
    var metrics: MutableList<Metric> = ArrayList<Metric>()
    var filters: MutableList<Filter> = ArrayList<Filter>()
    var files: MutableMap<BoardspaceGameType, MutableList<File>> = HashMap<BoardspaceGameType, MutableList<File>>()

    fun start() {
        setupMetrics()
        setupFilters()
        setupGameFiles()

        // Start parsing
        // If a game breaks, quit parsing and report progress so far
        val i = 0
        for (type in files.keys) {
            val fileList: MutableList<File> = files.get(type)!!
            for (file in fileList) {
                var parser: BoardspaceGameParser? = null
                var game: Game? = null

                try {
                    parser = BoardspaceGameParser(file)
                    game = parser.parse()
                } catch (e: RuntimeException) {
                    println("Game could not be parsed: " + file.getName() + " -> " + e)
                    continue
                }

                var analyseGame = true
                for (filter in filters) {
                    if (!filter.analyseGame(parser.getGameType(), game)) {
                        analyseGame = false
                        break
                    }
                }

                if (analyseGame) {
                    for (metric in metrics) {
                        game.setReplayMode(false)
                        metric.analyzeGame(type, parser.getGameType(), game)
                    }
                }
            }
        }

        saveMetrics()
    }

    private fun setupGameFiles() {
        // Test
        // val testFiles = mutableListOf(File("./plays/test.sgf"))
        // files[BoardspaceGameType.PLAYER] = testFiles

        val tournamentFiles = Stack<File>()
        tournamentFiles.add(File(TOURNAMENT_DIR))
        files.put(BoardspaceGameType.TOURNAMENT, findAllFiles(tournamentFiles, ArrayList<File>()))

        val playerFiles = Stack<File>()
        playerFiles.add(File(PLAYER_DIR))
        files.put(BoardspaceGameType.PLAYER, findAllFiles(playerFiles, ArrayList<File>()))

        // val dumbotFiles = Stack<File>()
        // dumbotFiles.add(File(DUMBOT_DIR))
        // files[BoardspaceGameType.DUMBOT] = findAllFiles(dumbotFiles, ArrayList<File>())
    }


    // Setup metrics to report on
    private fun setupMetrics() {
        // metrics.add(GamesAnalyzedMetric())
        // metrics.add(GameResultPrColorMetric(true))
        // metrics.add(GameDurationTurnsMetric())
        // metrics.add(GameDurationTimeMetric())
        // metrics.add(OpeningTokenMetric())
        // metrics.add(LastTokenMetric())
        // metrics.add(BugsInSupplyAtGameEndMetric())
        // metrics.add(BugsOnBoardAtGameEndMetric())
        // metrics.add(OpeningsMetric())
        // metrics.add(WinnerOpeningMetric())
        // metrics.add(FreeTokensPrTurnMetric(15))
        // metrics.add(FreeTokensPrTurnMetric(25))
        // metrics.add(TokensAroundQueenMetric(15))
        // metrics.add(TokensAroundQueenMetric(25))
        // metrics.add(HighRankedGamesMetric())
        // metrics.add(CompareOpeningsMetric())
        // metrics.add(LeastMovedTokenMetric())
        // metrics.add(IngersollOpeningsMetric())
        // metrics.add(LinaelOpeningList())
        metrics.add(LinaelOpeningComparison())
    }

    private fun setupFilters() {
        // filters.add(BoardspaceNetMasterPlayersFilter())
        // filters.add(HivePLGamesOnlyFilter())
        filters.add(QueenAsStartingPieceFilter())
    }


    // Recursively find all files in the dirs then return result when done
    private fun findAllFiles(dirs: Stack<File>, result: ArrayList<File>): MutableList<File> {
        if (dirs.isEmpty()) {
            return result
        } else {
            val files = dirs.pop()!!.listFiles()
            for (file in files!!) {
                if (file.isDirectory()) {
                    dirs.push(file)
                } else {
                    result.add(file)
                }
            }

            return findAllFiles(dirs, result)
        }
    }

    private fun saveMetrics() {
        for (metric in metrics) {
            metric.save()
        }
    }

    companion object {
        const val ALL_DIR: String = "./plays/all/" // tournament plays
        const val TOURNAMENT_DIR: String = "./plays/tournament/" // tournament plays
        const val PLAYER_DIR: String = "./plays/players/" // games between players
        const val DUMBOT_DIR: String = "./plays/dumbot/" // games between Dumbot and a human player

        @JvmStatic
        fun main(args: Array<String>) {
            val start = System.currentTimeMillis()
            MainParseGames().start()
            println("Done: " + (System.currentTimeMillis() - start) / 1000 + " s.")
        }
    }
}
