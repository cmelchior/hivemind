package dk.ilios.hivemind.parser.filters

import dk.ilios.hivemind.game.Game

/**
 * Interface for predicates used to determine wether or not to analyze a game.
 */
interface Filter {
    fun analyseGame(type: String, game: Game): Boolean
}
