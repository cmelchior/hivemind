package dk.ilios.hivemind;

import dk.ilios.asciihexgrid.HexOrientation;
import dk.ilios.asciihexgrid.printers.AsciiHexPrinter;
import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.BugType;
import dk.ilios.hivemind.model.Player;

/**
 * Used to print various boards
 */
public class MainPrintBoard {

    public static void main(String[] args) {

        HiveAsciiPrettyPrinter printer = new HiveAsciiPrettyPrinter();

        Player white = new Player("White", Player.PlayerType.WHITE);
        white.fillBaseSupply();
        white.useAllExpansions();

        Player black = new Player("Black", Player.PlayerType.BLACK);
        black.fillBaseSupply();
        black.useAllExpansions();

        Board board = new Board(white, black);
        setupBoard(white, black, board);
        printer.print(board);
    }

    private static void setupBoard(Player white, Player black, Board board) {
        board.addToken(white.getFromSupply(BugType.LADY_BUG), 0,0);
        board.addToken(black.getFromSupply(BugType.BEETLE), 1,0);
        board.addToken(white.getFromSupply(BugType.QUEEN_BEE), -1,0);
        board.addToken(black.getFromSupply(BugType.QUEEN_BEE), 2,-1);
//        board.addToken(black.getFromSupply(BugType.SPIDER), 0,-1);
//        board.addToken(black.getFromSupply(BugType.SPIDER), 2,-2);
    }
}
