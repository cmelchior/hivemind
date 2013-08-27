package dk.ilios.hivemind.debug;

import dk.ilios.asciihexgrid.AsciiBoard;
import dk.ilios.asciihexgrid.printers.AsciiHexPrinter;
import dk.ilios.asciihexgrid.printers.SmallFlatAsciiHexPrinter;
import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.BugType;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Player;

/**
 * Pretty printer for outputting hive maps to the command line
 */
public class HiveAsciiPrettyPrinter {

    private AsciiHexPrinter hexPrinter = new SmallFlatAsciiHexPrinter();

    public void print(Board board) {
        AsciiBoard asciiBoard = prettyPrintBoard(board);
        System.out.println(asciiBoard.prettPrint(true));
    }

    public String toString(Board board) {
        return prettyPrintBoard(board).prettPrint(true);
    }


    private AsciiBoard prettyPrintBoard(Board board) {

        // Find bound so we can adjust (top,left) to (0,0)
        int adjustQ = board.getMinQ();
        int adjustR = board.getMinR();
        int maxQ = board.getMaxQ();
        int maxR = board.getMaxR();

        AsciiBoard asciiBoard = new AsciiBoard(adjustQ, maxQ, adjustR, maxR, hexPrinter);
        for (Hex hex : board.getFilledHexes()) {
            BugType type = hex.getTopToken().getType();
            Player player = hex.getTopToken().getPlayer();

            char fill = (player.getType() == Player.PlayerType.WHITE) ? '#' : '+';
            asciiBoard.printHex(getTokenTitle(type), getPlayerTitle(player), fill, hex.getQ() - adjustQ, hex.getR() - adjustR);
        }
        return asciiBoard;
    }

    private String getPlayerTitle(Player player) {
        switch (player.getType()) {
            case BLACK: return "-B-";
            case WHITE: return "-W-";
        }

        return "-?-";
    }

    private String getTokenTitle(BugType type) {
        switch (type) {
            case UNKNOWN: return "???";
            case QUEEN_BEE: return "QBE";
            case BEETLE: return "BTL";
            case GRASSHOPPER: return "HOP";
            case SPIDER: return "SPI";
            case SOLDIER_ANT: return "ANT";
            case MOSQUITO: return "MOS";
            case LADY_BUG: return "LDY";
            case PILL_BUG: return "PIL";
        }

        return "???";
    }
}
