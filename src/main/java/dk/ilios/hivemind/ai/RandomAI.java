package dk.ilios.hivemind.ai;

import dk.ilios.hivemind.ai.statistics.AIStatistics;
import dk.ilios.hivemind.game.Game;
import dk.ilios.hivemind.game.GameCommand;
import dk.ilios.hivemind.model.*;
import dk.ilios.hivemind.model.rules.Rules;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Test AI that randomly either places a bug or moves a existing bug.
 * For the first 3 turns there is a 50% each turn to place the queen.
 * If queen has not been placed by turn 3 it will always happen on turn 4.
 */
public class RandomAI implements HiveAI {

    private String name;
    private Random random = new Random();
    private Game state;
    private AIStatistics stats = new AIStatistics(this);

    public RandomAI(String name) {
        this.name = name;
    }

    @Override
    public HiveAI copy() {
        return new RandomAI(name);
    }

    @Override
    public synchronized GameCommand nextMove(Game state, Board board) {
        this.state = state;
        Player activePlayer = state.getActivePlayer();

        // If by 4th turn the queen hasn't been placed yet, it must be placed
        if (state.getActivePlayer().getMoves() == 3 && !activePlayer.hasPlacedQueen()) {
            return moveQueenToRandomBoardLocation();
        }

        // 50% chance to place queen the first 3 turns
        if (!activePlayer.hasPlacedQueen() && random.nextBoolean()) {
            return moveQueenToRandomBoardLocation();
        };

        // 50% to move token on board instead of adding new. Always add tokens until queen has been placed
        boolean moveTokenOnBoard = activePlayer.hasPlacedQueen() && random.nextBoolean() && getRandomTokenFromBoard() != null;

        // Supply still has available tokens
        if (!moveTokenOnBoard && haveTokensInSupply()) {
            Token token = getRandomTokenFromSupply();
            return moveTokenFromSupplyToRandomBoardLocation(token);
        } else {
            Token token = getRandomTokenFromBoard();
            if (token != null) {
                return moveTokenToRandomLocation(token);
            } else {
                return GameCommand.PASS;
            }
        }
    }

    @Override
    public AIStatistics getAiStats() {
        return stats;
    }

    private GameCommand moveQueenToRandomBoardLocation() {
        Token token = state.getActivePlayer().getFromSupply(BugType.QUEEN_BEE);
        return moveTokenFromSupplyToRandomBoardLocation(token);
    }

    private boolean haveTokensInSupply() {
        return state.getActivePlayer().getSupply().size() > 0;
    }

    private GameCommand moveTokenToRandomLocation(Token token) {
        List<Hex> targets = Rules.getInstance().getTargetHexes(token, state.getBoard());
        if (targets.size() == 0) {
            return GameCommand.PASS;
        } else {
            Hex fromHex = token.getHex();
            Hex hex = targets.get(random.nextInt(targets.size()));

            return new GameCommand(fromHex.getQ(), fromHex.getR(), hex.getQ(), hex.getR(), token, false);
        }
    }

    private GameCommand moveTokenFromSupplyToRandomBoardLocation(Token token) {
        List<Hex> targets = Rules.getInstance().getStartHexes(token.getPlayer(), state.getBoard());
        if (targets.size() == 0) {
            return GameCommand.PASS;
        } else {
            Hex hex =  targets.get(random.nextInt(targets.size()));
            return new GameCommand(Hex.SUPPLY, Hex.SUPPLY, hex.getQ(), hex.getR(), token, false);
        }
    }

    public Token getRandomTokenFromSupply() {
        Player player = state.getActivePlayer();
        Set<Token> supply = player.getSupply();
        int size = supply.size();
        if (size > 0) {
            for (Token t : supply) {
                return player.getFromSupply(t.getType());
            }
        }

        return null;
    }

    public Token getRandomTokenFromBoard() {
        Player activePlayer = state.getActivePlayer();
        Set<Token> tokens = Rules.getInstance().getFreeTokens(activePlayer, state.getBoard());
        for (Token token : tokens) {
            if (token.getPlayer().equals(activePlayer)) {
                return token;
            }
        }

        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean maintainsStandardPosition() {
        return false;
    }
}
