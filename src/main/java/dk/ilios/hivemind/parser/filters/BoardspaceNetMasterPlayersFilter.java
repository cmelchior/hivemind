package dk.ilios.hivemind.parser.filters;

import dk.ilios.hivemind.game.Game;

import java.util.HashSet;

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
public class BoardspaceNetMasterPlayersFilter implements Filter {

    private HashSet<String> players = new HashSet<String>() {
        {
            add("image13".toLowerCase());
            add("ringersoll".toLowerCase());
            add("Fumanchu".toLowerCase());
            add("Eucalyx".toLowerCase());
            add("DrRaven".toLowerCase());
            add("stepanzo".toLowerCase());
            add("fabian".toLowerCase());
            add("Docster".toLowerCase());
            add("pirtscire".toLowerCase());
            add("Loizz".toLowerCase());
            add("EddyMarlo".toLowerCase());
            add("pilgra".toLowerCase());
            add("seneca29".toLowerCase());
            add("hlaspoor".toLowerCase());
            add("humdeabril".toLowerCase());
            add("BlackMagic".toLowerCase());
            add("Ampexian".toLowerCase());
        }
    };

    @Override
    public boolean analyseGame(String type, Game game) {
        return players.contains(game.getWhitePlayer().getName().toLowerCase()) && players.contains(game.getBlackPlayer().getName().toLowerCase());
    }
}
