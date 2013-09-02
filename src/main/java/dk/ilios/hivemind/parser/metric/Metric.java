package dk.ilios.hivemind.parser.metric;

import dk.ilios.hivemind.debug.HiveAsciiPrettyPrinter;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.parser.BoardspaceGameType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for gathering 1 metric about the game
 */
public abstract class Metric {

    protected HiveAsciiPrettyPrinter printer = new HiveAsciiPrettyPrinter();

    /**
     * Analyse game for given metric
     */
    public abstract void analyzeGame(BoardspaceGameType type, String variant, Game game);

    /**
     * Save report to disc
     */
    public abstract void save();

    protected String getKey(BoardspaceGameType type, String variant) {
        return type.getPrefix() + "-" + variant;
    }


    /**
     * converts a matrix to a string that can be be persisted and is gnuplot friendly.
     *
     * @param file  String[y][x]
     */
    protected String matrixToString(String[][] file) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < file.length; i++) {
            String[] line = file[i];
            for (int j = 0; j < line.length; j++) {
                sb.append(file[i][j]);
                if (j < line.length - 1) sb.append('\t');
            }
            if (i < file.length - 1) sb.append('\n');
        }

        return sb.toString();
    }

    protected void ensureSize(ArrayList<Integer> list, int size) {
        // Prevent excessive copying while we're adding
        list.ensureCapacity(size);
        while (list.size() < size) {
            list.add(0);
        }
    }

    protected void saveStringToDisc(String fileName, String content) {
        try {
            File file = new File("results/" + fileName);
            System.out.println(file.getAbsolutePath());
            System.out.println(content);
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.write(content);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
