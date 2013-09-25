package dk.ilios.hivemind.game;

import dk.ilios.hivemind.model.Board;
import dk.ilios.hivemind.model.Hex;
import dk.ilios.hivemind.model.Token;

public class GameCommand {

    public static final GameCommand PASS = new GameCommand(0, 0, 0, 0, null, false);

    final int fromQ;
    final int fromR;
    final int toQ;
    final int toR;
    final Token token;
    final boolean movedByPillbug;

    /**
     * Helper constructor: Move a token
     */
    public static GameCommand move(Token token, int toQ, int toR) {
        return new GameCommand(token.getHex().getQ(), token.getHex().getR(), toQ, toR, token, false);
    }

    /**
     * Helper constructor: Add to board from supply
     */
    public static GameCommand addFromSupply(Token token, int toQ, int toR) {
        return new GameCommand (Hex.SUPPLY, Hex.SUPPLY, toQ, toR, token, false);
    }

    public static GameCommand moveByPillbug(Token token, int toQ, int toR) {
        return new GameCommand(token.getHex().getQ(), token.getHex().getR(), toQ, toR, token, true);
    }


    public GameCommand(int fromQ, int fromR, int toQ, int toR, Token token, boolean movedByPillbug) {
        this.fromQ = fromQ;
        this.fromR = fromR;
        this.toQ = toQ;
        this.toR = toR;
        this.token = token;
        this.movedByPillbug = movedByPillbug;
    }

    public int getFromQ() {
        return fromQ;
    }

    public int getFromR() {
        return fromR;
    }

    public int getToQ() {
        return toQ;
    }

    public int getToR() {
        return toR;
    }

    public Token getToken() {
        return token;
    }

    public boolean isMovedByPillbug() {
        return movedByPillbug;
    }

    public void execute(Game game) {
        Board board = game.getBoard();

        if (GameCommand.PASS.equals(this)) {
            // Do nothing;
        } else if (fromQ != Hex.SUPPLY) {
            board.moveToken(fromQ, fromR, toQ, toR);
        } else {
            board.addToken(token, toQ, toR);
        }

        game.getActivePlayer().movedToken();
        game.togglePlayer();
        game.updateZobristKey();
    }

    public void undo(Game game) {
        Board board = game.getBoard();

        if (GameCommand.PASS.equals(this)) {
            // Do nothing;
        } else if (fromQ != Hex.SUPPLY) {
            board.moveToken(toQ, toR, fromQ, fromR);
        } else {
            board.removeToken(toQ, toR);
        }

        // Make sure that the old active players gets it turn correctly modified
            // by toggling player first.
        game.togglePlayer();
        game.getActivePlayer().undoTokenMoved();
        game.updateZobristKey();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append((token != null) ? token.toString() : "null");
        sb.append(": ");
        if (fromQ == Hex.SUPPLY) {
            sb.append("SUPPLY");
        } else {
            sb.append("(" + fromQ + ", " + fromR + ")");
        }

        sb.append (" -> ");
        sb.append("(" + toQ + ", " + toR + ")");
        return sb.toString();
    }

    public String getTargetSquareDesc() {
        if (toQ == Hex.SUPPLY || toR == Hex.SUPPLY) {
            return "SUPPLY";
        } else {
            return "(" + toQ + ", " + toR + ")";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof GameCommand)) return false;
        GameCommand gc2 = (GameCommand) o;
        if (fromQ != gc2.getFromQ()) return false;
        if (fromR != gc2.getFromR()) return false;
        if (toQ != gc2.getToQ()) return false;
        if (toR != gc2.getToR()) return false;
        if (movedByPillbug != gc2.isMovedByPillbug()) return false;
        return (token == null && gc2.getToken() == null) || token.equals(gc2.getToken());
    }
}
