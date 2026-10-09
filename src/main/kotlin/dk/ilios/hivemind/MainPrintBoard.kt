package dk.ilios.hivemind

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter
import dk.ilios.hivemind.model.Board
import dk.ilios.hivemind.model.BugType
import dk.ilios.hivemind.model.Player
import dk.ilios.hivemind.model.Player.PlayerType

/**
 * Used to print various boards
 */
object MainPrintBoard {
    @JvmStatic
    fun main(args: Array<String>) {
        val printer = HiveAsciiPrettyPrinter()

        val white = Player("White", PlayerType.WHITE)
        white.fillBaseSupply()
        white.useAllExpansions()

        val black = Player("Black", PlayerType.BLACK)
        black.fillBaseSupply()
        black.useAllExpansions()

        val board = Board(white, black)
        setupBoard(white, black, board)
        printer.print(board)
    }

    private fun setupBoard(white: Player, black: Player, board: Board) {
        board.addToken(white.getFromSupply(BugType.LADY_BUG), 0, 0)
        board.addToken(black.getFromSupply(BugType.BEETLE), 1, 0)
        board.addToken(white.getFromSupply(BugType.QUEEN_BEE), -1, 0)
        board.addToken(black.getFromSupply(BugType.QUEEN_BEE), 2, -1)
        // board.addToken(black.getFromSupply(BugType.SPIDER), 0, -1)
        // board.addToken(black.getFromSupply(BugType.SPIDER), 2, -2)
    }
}
