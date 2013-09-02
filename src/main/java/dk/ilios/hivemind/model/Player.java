package dk.ilios.hivemind.model;

import dk.ilios.hivemind.game.CommandProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Describes a player
 */
public class Player {

    public enum PlayerType {
        BLACK, WHITE;
    }
    private CommandProvider commandProvider;

    private final String name;
    private int ranking;

    private final PlayerType type;
    private int moves = 0; // Number of moves made in the game
    private int passes = 0; // Number of passes made in the game

    private Token queen;
    private Map<String, Token> bugs = new HashMap<String, Token>();
    private Map<BugType, Integer>  supplyCreatureCounter = new HashMap<BugType, Integer>();     // Easy lookup to determine number of tokens of a specific type in the supply.
    private Set<Token> supply = new HashSet<Token>();                            // Tokens in the supply

    private long playTime = 0; // How much time has the player used so far

    public Player(String name, PlayerType type) {
        this.name = name;
        this.type = type;
    }

    public void fillBaseSupply() {
        addToSupply(BugType.QUEEN_BEE, 1);
        addToSupply(BugType.SPIDER, 2);
        addToSupply(BugType.GRASSHOPPER,3);
        addToSupply(BugType.SOLDIER_ANT, 3);
        addToSupply(BugType.BEETLE, 2);
    }

    public void useAllExpansions() {
        useMosquitoExpansion();
        useLadyBugExpansion();
        usePillBugExpansion();
    }

    public void useMosquitoExpansion() {
        addToSupply(BugType.MOSQUITO, 1);
    }

    public void useLadyBugExpansion() {
        addToSupply(BugType.LADY_BUG, 1);
    }

    public void usePillBugExpansion() {
        addToSupply(BugType.PILL_BUG, 1);
    }

    /**
     * Add a number of bugs to the supply.
     */
    public void addToSupply(BugType type, int count) {

        int currentCount = supplyCreatureCounter.containsKey(type) ? supplyCreatureCounter.get(type) : 0;

        for (int i = 0; i < count; i++) {
            Token t = new Token(this, type);
            t.setId(t.getOriginalType().generateId(currentCount + 1 + i)); // Add ID mimicing the ID scheme used by Boardspace.net
            supply.add(t);
            bugs.put(t.getId(), t);
        }

        supplyCreatureCounter.put(type, currentCount + count);

        // Set queen if needed
        if (queen == null && type == BugType.QUEEN_BEE) {
            this.queen = getFromSupply(BugType.QUEEN_BEE);
        }
    }


    public String getName() {
        return name;
    }

    public PlayerType getType() {
        return type;
    }

    public CommandProvider getCommandProvider() {
        return commandProvider;
    }

    public void setCommandProvider(CommandProvider commandProvider) {
        this.commandProvider = commandProvider;
    }

    public int getMoves() {
        return moves;
    }

    public void movedToken() {
        moves++;
    }

    public void undoTokenMoved() {
        moves--;
    }

    /**
     * Helper function to explicitly set how many tokens the player has moved already.
     */
    public void setTokensMoved(int tokensMoved) {
        this.moves = tokensMoved;
    }

    public boolean isBlackPlayer() {
        return type == PlayerType.BLACK;
    }

    public boolean isWhitePlayer() {
        return type == PlayerType.WHITE;
    }

    /**
     * Adds a token to the stash.
     */
    public void addToSupply(Token token) {
        supply.add(token);
        BugType type = token.getType();
        if (supplyCreatureCounter.containsKey(type)) {
            supplyCreatureCounter.put(type, supplyCreatureCounter.get(type) + 1);
        } else {
            supplyCreatureCounter.put(type, 1);
        }
    }

    public void removeFromSupply(Token token) {
        if (supply.contains(token)) {
            supply.remove(token);
            int count = supplyCreatureCounter.get(token.getOriginalType()) - 1;
            supplyCreatureCounter.put(token.getOriginalType(), count);
        } else {
            throw new IllegalStateException("Supply doesn't contain: " + token);
        }
    }

    /**
     * Fetches a token from the supply. It is not removed until removeFromSupply() is called.
     * An IllegalStateException will be thrown if no such is available.
     */
    public Token getFromSupply(BugType type) {
        int count = supplyCreatureCounter.containsKey(type) ? supplyCreatureCounter.get(type) : 0;
        if (count > 0) {
            Token result = null;
            for (Token token : supply) {
                 if (token.getType() == type) {
                     result = token;
                     break;
                 }
             }
            return result;
        }

        throw new IllegalStateException("No more " + type + " + tokens available");
    }


    /**
     * Returns a reference to the bug with the given Id, no matter where it is.
     */
    public Token get(String bugId) {
        return bugs.get(bugId.toUpperCase());
    }

    /**
     * Returns true if the stash contains more tokens of the given creature type
     * @param type
     * @return
     */
    public boolean haveMoreTokens(BugType type) {
        int count = supplyCreatureCounter.containsKey(type) ? supplyCreatureCounter.get(type) : 0;
        return count > 0;
    }

    /**
     * Return available tokens
     */
    public Set<Token> getSupply() {
        return supply;
    }

    public Token getQueen() {
        return queen;
    }

    public boolean hasPlacedQueen() {
        return queen.getHex() != null;
    }

    /**
     * Returns how many turns the player has had.
     */
    public int getTurns() {
        return moves + passes;
    }

    public int getPasses() {
        return passes;
    }

    /**
     * Return the number of bugs that make up each side
     */
    public int getNoStartingBugs() {
        return bugs.size();
    }

    public long getPlayTime() {
        return playTime;
    }

    public void addPlayTime(long timeInMs) {
        playTime += timeInMs;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }
}
