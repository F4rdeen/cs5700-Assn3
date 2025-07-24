package core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShippingUpdateTest {
    @Test
    fun `fail - previousStatus should not be shipped`() {
        val update = ShippingUpdate("created", "shipped", 123456789L)
        try {
            assertEquals("shipped", update.previousStatus)
            fail("Test should have failed but passed")
        } catch (e: AssertionError) {
            // Expected failure, test passes
        }
    }
    @Test
    fun `ShippingUpdate stores previous and new status and timestamp`() {
        val update = ShippingUpdate("created", "shipped", 123456789L)
        assertEquals("created", update.previousStatus)
        assertEquals("shipped", update.newStatus)
        assertEquals(123456789L, update.timestamp)
    }
}
