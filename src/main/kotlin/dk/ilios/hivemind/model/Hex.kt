package dk.ilios.hivemind.model

import java.util.EmptyStackException

/**
 * Describe a Hex on the board
 */
class Hex(val q: Int, val r: Int) : Comparable<Hex?> {
    private val tokens = mutableListOf<Token>()

    val isEmpty: Boolean
        get() = tokens.isEmpty()

    fun addToken(token: Token?) {
        if (token == null) return
        tokens.add(token)
    }

    fun removeToken(): Token {
        if (tokens.isEmpty()) throw EmptyStackException()
        return tokens.removeAt(tokens.lastIndex)
    }

    val topToken: Token?
        get() = tokens.lastOrNull()

    /**
     * Return token at given height.
     * @param height Height to find token. Ground level is 1;
     */
    fun getTokenAt(height: Int): Token? {
        if (tokens.size < height) {
            return null
        } else {
            return tokens[height - 1]
        }
    }

    val height: Int
        get() = tokens.size

    override fun toString(): String = "(" + q + ", " + r + ")"

    override fun compareTo(other: Hex?): Int {
        if (other == null) return 1
        if (q == other.q) {
            return r.compareTo(other.r)
        } else {
            return q.compareTo(other.q)
        }
    }

    companion object {
        const val SUPPLY: Int = Int.MAX_VALUE
    }
}
