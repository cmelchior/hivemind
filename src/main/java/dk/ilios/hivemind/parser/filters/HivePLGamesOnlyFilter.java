package dk.ilios.hivemind.parser.filters;

import dk.ilios.hivemind.game.Game;

/**
 * Used for debugging. Only analyzes games with PillBug / LadyBug
 */
public class HivePLGamesOnlyFilter implements Filter {

    @Override
    public boolean analyseGame(String type, Game game) {
        return type.toLowerCase().endsWith("hive-pl");
    }
}
