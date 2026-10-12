package dk.ilios.hivemind

import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.parser.BoardspaceGameParser
import dk.ilios.hivemind.parser.BoardspaceGameType
import dk.ilios.hivemind.parser.filters.Filter
import dk.ilios.hivemind.parser.filters.QueenAsStartingPieceFilter
import dk.ilios.hivemind.parser.metric.LinaelOpeningComparison
import dk.ilios.hivemind.parser.metric.Metric
import java.io.File

class MainParseGames {
    val metrics: List<Metric> = setupMetrics()
    val filters: List<Filter> = setupFilters()
    var files: Map<BoardspaceGameType, List<File>> = emptyMap()
        private set

    fun start() {
        files = setupGameFiles()

        // Start parsing. Skip files that cannot be parsed.
        for ((type, fileList) in files) {
            for (file in fileList) {
                val parser: BoardspaceGameParser
                val game: Game
                try {
                    parser = BoardspaceGameParser(file)
                    game = parser.parse()
                } catch (e: RuntimeException) {
                    println("Game could not be parsed: ${file.name} -> $e")
                    continue
                }

                if (filters.all { it.analyseGame(parser.gameType, game) }) {
                    for (metric in metrics) {
                        game.setReplayMode(false)
                        metric.analyzeGame(type, parser.gameType, game)
                    }
                }
            }
        }

        metrics.forEach { it.save() }
    }

    private fun setupGameFiles(): Map<BoardspaceGameType, List<File>> = hashMapOf(
        BoardspaceGameType.TOURNAMENT to findAllFiles(File(TOURNAMENT_DIR)),
        BoardspaceGameType.PLAYER to findAllFiles(File(PLAYER_DIR)),
        // BoardspaceGameType.DUMBOT to findAllFiles(File(DUMBOT_DIR)),
        // BoardspaceGameType.PLAYER to listOf(File("./plays/test.sgf")),
    )

    // Setup metrics to report on
    private fun setupMetrics(): List<Metric> = listOf(
        // GamesAnalyzedMetric(),
        // GameResultPrColorMetric(true),
        // GameDurationTurnsMetric(),
        // GameDurationTimeMetric(),
        // OpeningTokenMetric(),
        // LastTokenMetric(),
        // BugsInSupplyAtGameEndMetric(),
        // BugsOnBoardAtGameEndMetric(),
        // OpeningsMetric(),
        // WinnerOpeningMetric(),
        // FreeTokensPrTurnMetric(15),
        // FreeTokensPrTurnMetric(25),
        // TokensAroundQueenMetric(15),
        // TokensAroundQueenMetric(25),
        // HighRankedGamesMetric(),
        // CompareOpeningsMetric(),
        // LeastMovedTokenMetric(),
        // IngersollOpeningsMetric(),
        // LinaelOpeningList(),
        LinaelOpeningComparison(),
    )

    private fun setupFilters(): List<Filter> = listOf(
        // BoardspaceNetMasterPlayersFilter(),
        // HivePLGamesOnlyFilter(),
        QueenAsStartingPieceFilter(),
    )

    // Visit directories in the same depth-first order as the original stack traversal.
    private fun findAllFiles(directory: File): List<File> = buildList {
        val directories = ArrayDeque<File>()
        directories.addLast(directory)
        while (directories.isNotEmpty()) {
            val children = checkNotNull(directories.removeLast().listFiles())
            for (file in children) {
                if (file.isDirectory) directories.addLast(file) else add(file)
            }
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
