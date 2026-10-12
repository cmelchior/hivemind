package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.parser.BoardspaceGameType
import java.io.File
import java.io.IOException

/**
 * Interface for gathering 1 metric about the game
 */
abstract class Metric {
    protected val printer = HiveAsciiPrettyPrinter()

    /**
     * Analyse game for given metric
     */
    abstract fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game)

    /**
     * Save report to disc
     */
    abstract fun save()

    protected fun getKey(type: BoardspaceGameType, variant: String): String = type.prefix + "-" + variant


    /**
     * converts a matrix to a string that can be be persisted and is gnuplot friendly.
     *
     * @param file  String[y][x]
     */
    protected fun matrixToString(file: Array<Array<String?>>): String =
        file.joinToString("\n") { row -> row.joinToString("\t") }

    protected fun ensureSize(list: MutableList<Int>, size: Int) {
        while (list.size < size) {
            list.add(0)
        }
    }

    protected fun saveStringToDisc(fileName: String?, content: String) {
        try {
            val file = File("results/" + fileName)
            println(file.absolutePath)
            println(content)
            file.bufferedWriter().use { it.write(content) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
