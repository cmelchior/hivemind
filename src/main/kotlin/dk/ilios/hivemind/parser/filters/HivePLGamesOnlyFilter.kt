package dk.ilios.hivemind.parser.filters

import dk.ilios.hivemind.game.Game
import java.util.*

/**
 * Used for debugging. Only analyzes games with PillBug / LadyBug
 */
class HivePLGamesOnlyFilter : Filter {
    override fun analyseGame(type: String, game: Game): Boolean {
        return type.lowercase(Locale.getDefault()).endsWith("hive-pl")
    }
}
