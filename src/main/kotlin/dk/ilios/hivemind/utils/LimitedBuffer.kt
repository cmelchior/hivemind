package dk.ilios.hivemind.utils

import java.util.*

class LimitedBuffer<E>(limit: Int) : LinkedList<E?>() {
    private val limit: Int

    init {
        this.limit = limit
    }

    override fun add(e: E?): Boolean {
        super.addFirst(e)
        if (size > limit) {
            super.removeLast()
        }
        return true
    }
}
