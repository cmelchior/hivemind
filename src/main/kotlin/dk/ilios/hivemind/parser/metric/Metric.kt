package dk.ilios.hivemind.parser.metric

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter
import dk.ilios.hivemind.game.Game
import dk.ilios.hivemind.parser.BoardspaceGameType
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * Interface for gathering 1 metric about the game
 */
abstract class Metric {
    protected var printer: HiveAsciiPrettyPrinter = HiveAsciiPrettyPrinter()

    /**
     * Analyse game for given metric
     */
    abstract fun analyzeGame(type: BoardspaceGameType, variant: String, game: Game)

    /**
     * Save report to disc
     */
    abstract fun save()

    protected fun getKey(type: BoardspaceGameType, variant: String): String {
        return type.prefix + "-" + variant
    }


    /**
     * converts a matrix to a string that can be be persisted and is gnuplot friendly.
     *
     * @param file  String[y][x]
     */
    protected fun matrixToString(file: Array<Array<String?>>): String {
        val sb = StringBuilder()
        for (i in file.indices) {
            val line: Array<String?> = file[i]
            for (j in line.indices) {
                sb.append(file[i][j])
                if (j < line.size - 1) sb.append('\t')
            }
            if (i < file.size - 1) sb.append('\n')
        }

        return sb.toString()
    }

    protected fun ensureSize(list: ArrayList<Int?>, size: Int) {
        // Prevent excessive copying while we're adding
        list.ensureCapacity(size)
        while (list.size < size) {
            list.add(0)
        }
    }

    protected fun saveStringToDisc(fileName: String?, content: String) {
        try {
            val file = File("results/" + fileName)
            println(file.getAbsolutePath())
            println(content)
            val output = BufferedWriter(FileWriter(file))
            output.write(content)
            output.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
