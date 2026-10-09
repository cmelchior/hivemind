package dk.ilios.hivemind.debug

import dk.ilios.asciihexgrid.AsciiBoard
import dk.ilios.asciihexgrid.printers.AsciiHexPrinter
import dk.ilios.asciihexgrid.printers.SmallFlatAsciiHexPrinter
import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.BugType
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.Player.PlayerType

/**
 * Pretty printer for outputting hive maps to the command line
 */
class HiveAsciiPrettyPrinter {
    private val hexPrinter: AsciiHexPrinter = SmallFlatAsciiHexPrinter()

    fun print(board: Board) {
        val asciiBoard = prettyPrintBoard(board)
        println(asciiBoard.prettPrint(true))
    }

    fun toString(board: Board): String? {
        return prettyPrintBoard(board).prettPrint(true)
    }


    private fun prettyPrintBoard(board: Board): AsciiBoard {
        if (board.isUsingStandardPosition) {
            return standardPositionBoard(board)
        } else {
            return normalBoard(board)
        }
    }

    private fun normalBoard(board: Board): AsciiBoard {
        // Find bound so we can adjust (top,left) to (0,0)
        val adjustQ = board.minQ
        val adjustR = board.minR
        val maxQ = board.maxQ
        val maxR = board.maxR

        val asciiBoard = AsciiBoard(adjustQ, maxQ, adjustR, maxR, hexPrinter)
        for (hex in board.filledHexes) {
            val player = hex.topToken!!.player

            val fill = if (player.type == PlayerType.WHITE) '#' else '+'
            asciiBoard.printHex(
                hex.topToken!!.id,
                getPlayerTitle(player),
                fill,
                hex.q - adjustQ,
                hex.r - adjustR
            )
        }
        return asciiBoard
    }

    private fun standardPositionBoard(board: Board): AsciiBoard {
        // Find bound so we can adjust (top,left) to (0,0)
        val adjustQ = board.minQ
        val adjustR = board.minR
        val maxQ = board.maxQ
        val maxR = board.maxR

        val asciiBoard = AsciiBoard(adjustQ, maxQ, adjustR, maxR, hexPrinter)
        for (hex in board.filledHexes) {
            val hexCoords = board.getSPCoordinatesFor(hex)
            val player = hex.topToken!!.player

            val fill = if (player.type == PlayerType.WHITE) '#' else '+'
            asciiBoard.printHex(
                hex.topToken!!.id,
                getPlayerTitle(player),
                fill,
                hexCoords[0] - adjustQ,
                hexCoords[1] - adjustR
            )
        }
        return asciiBoard
    }

    private fun getPlayerTitle(player: Player): String {
        when (player.type) {
            PlayerType.BLACK -> return "-B-"
            PlayerType.WHITE -> return "-W-"
            else -> return "-?-"
        }

        return "-?-"
    }

    private fun getTokenTitle(type: BugType): String {
        when (type) {
            BugType.UNKNOWN -> return "???"
            BugType.QUEEN_BEE -> return "QBE"
            BugType.BEETLE -> return "BTL"
            BugType.GRASSHOPPER -> return "HOP"
            BugType.SPIDER -> return "SPI"
            BugType.SOLDIER_ANT -> return "ANT"
            BugType.MOSQUITO -> return "MOS"
            BugType.LADY_BUG -> return "LDY"
            BugType.PILL_BUG -> return "PIL"
        }

        return "???"
    }
}
