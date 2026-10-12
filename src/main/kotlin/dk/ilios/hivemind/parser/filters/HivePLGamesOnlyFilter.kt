package dk.ilios.hivemind.parser.filters

import dk.ilios.hivemind.game.Game
import java.util.Locale

/**
 * Used for debugging. Only analyzes games with PillBug / LadyBug
 */
class HivePLGamesOnlyFilter : Filter {
    override fun analyseGame(type: String, game: Game): Boolean =
        type.lowercase(Locale.getDefault()).endsWith("hive-pl")
}
