package dk.ilios.hivemind.parser.filters

import dk.ilios.hivemind.game.Game

/**
 * Only analyze games where both players has 1600+ ranking
 */
class HighRankedGamesFilter : Filter {
    override fun analyseGame(type: String, game: Game): Boolean {
        return game.whitePlayer.ranking >= REQUIRED_RANKING && game.blackPlayer
            .ranking >= REQUIRED_RANKING
    }

    companion object {
        private const val REQUIRED_RANKING = 1600
    }
}
