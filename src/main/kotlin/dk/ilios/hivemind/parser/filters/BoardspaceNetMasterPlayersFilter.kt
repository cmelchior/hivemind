package dk.ilios.hivemind.parser.filters

import dk.ilios.hivemind.game.Game
import java.util.Locale

/**
 * Only analyze games between "master" players as defined by Eucalyx:
 *
 * image13
 * ringersoll
 * Fumanchu
 * Eucalyx
 * DrRaven
 * stepanzo
 * fabian
 * Docster
 * pirtscire
 * Loizz
 * EddyMarlo
 * pilgra
 * seneca29
 * hlaspoor
 * humdeabril
 * BlackMagic
 * Ampexian
 */
class BoardspaceNetMasterPlayersFilter : Filter {
    private val players = setOf(
        "image13", "ringersoll", "Fumanchu", "Eucalyx", "DrRaven", "stepanzo",
        "fabian", "Docster", "pirtscire", "Loizz", "EddyMarlo", "pilgra",
        "seneca29", "hlaspoor", "humdeabril", "BlackMagic", "Ampexian"
    ).map { it.lowercase(Locale.getDefault()) }.toSet()

    override fun analyseGame(type: String, game: Game): Boolean {
        return players.contains(game.whitePlayer.name!!.lowercase(Locale.getDefault())) && players.contains(
            game.blackPlayer.name!!.lowercase(
                Locale.getDefault()
            )
        )
    }
}
