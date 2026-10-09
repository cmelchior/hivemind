package dk.ilios.hivemind.model

import java.util.*

/**
 * Describe a Hex on the board
 */
class Hex(x: Int, y: Int) : Comparable<Hex?> {
    private val tokens = Stack<Token>()
    var q: Int = 0
        private set
    var r: Int = 0
        private set

    /**
     * Construct empty hex with no neighbors
     */
    init {
        this.q = x
        this.r = y
    }

    val isEmpty: Boolean
        get() = tokens.isEmpty()

    fun addToken(token: Token?) {
        if (token == null) return
        tokens.push(token)
    }

    fun removeToken(): Token {
        return tokens.pop()
    }

    val topToken: Token?
        get() {
            if (tokens.isEmpty()) return null
            return tokens.peek()
        }

    /**
     * Return token at given height.
     * @param height Height to find token. Ground level is 1;
     */
    fun getTokenAt(height: Int): Token? {
        if (tokens.size < height) {
            return null
        } else {
            return tokens.get(height - 1)
        }
    }

    val height: Int
        get() = tokens.size

    override fun toString(): String {
        return "(" + q + ", " + r + ")"
    }

    override fun compareTo(other: Hex?): Int {
        if (other == null) return 1
        if (q == other.q) {
            return compareInt(r, other.r)
        } else {
            return compareInt(q, other.q)
        }
    }

    private fun compareInt(a: Int, b: Int): Int {
        if (a == b) return 0
        if (a > b) {
            return 1
        } else {
            return -1
        }
    }

    companion object {
        val SUPPLY: Int = Int.MAX_VALUE
    }
}
