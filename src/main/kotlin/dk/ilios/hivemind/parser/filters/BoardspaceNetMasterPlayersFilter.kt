package dk.ilios.hivemind.parser.filters

import dk.ilios.hivemind.game.Game
import java.util.*

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
    private val players: HashSet<String?> = object : HashSet<String?>() {
        init {
            add("image13".lowercase(Locale.getDefault()))
            add("ringersoll".lowercase(Locale.getDefault()))
            add("Fumanchu".lowercase(Locale.getDefault()))
            add("Eucalyx".lowercase(Locale.getDefault()))
            add("DrRaven".lowercase(Locale.getDefault()))
            add("stepanzo".lowercase(Locale.getDefault()))
            add("fabian".lowercase(Locale.getDefault()))
            add("Docster".lowercase(Locale.getDefault()))
            add("pirtscire".lowercase(Locale.getDefault()))
            add("Loizz".lowercase(Locale.getDefault()))
            add("EddyMarlo".lowercase(Locale.getDefault()))
            add("pilgra".lowercase(Locale.getDefault()))
            add("seneca29".lowercase(Locale.getDefault()))
            add("hlaspoor".lowercase(Locale.getDefault()))
            add("humdeabril".lowercase(Locale.getDefault()))
            add("BlackMagic".lowercase(Locale.getDefault()))
            add("Ampexian".lowercase(Locale.getDefault()))
        }
    }

    override fun analyseGame(type: String, game: Game): Boolean {
        return players.contains(game.getWhitePlayer().name!!.lowercase(Locale.getDefault())) && players.contains(
            game.getBlackPlayer().name!!.lowercase(
                Locale.getDefault()
            )
        )
    }
}
