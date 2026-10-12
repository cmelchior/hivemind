package dk.ilios.hivemind.utils

class LimitedBuffer<E>(private val limit: Int) : AbstractList<E>() {
    private val entries = ArrayDeque<E>()

    override val size: Int
        get() = entries.size

    override fun get(index: Int): E = entries[index]

    fun clear() = entries.clear()

    fun add(element: E): Boolean {
        entries.addFirst(element)
        if (size > limit) {
            entries.removeLast()
        }
        return true
    }
}
