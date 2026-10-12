package dk.ilios.hivemind.utils

import org.junit.Assert.*
import org.junit.Test

class LimitedBufferTest {
    @Test
    fun retainsNewestEntriesAndCanBeReusedAfterClearing() {
        val buffer = LimitedBuffer<String?>(2)
        buffer.add("first")
        buffer.add("second")
        buffer.add("third")
        assertEquals(listOf("third", "second"), buffer.toList())
        assertNull(buffer.getOrNull(2))

        buffer.clear()
        assertTrue(buffer.isEmpty())
        buffer.add(null)
        buffer.add("new")
        assertEquals(listOf("new", null), buffer.toList())
    }
}
