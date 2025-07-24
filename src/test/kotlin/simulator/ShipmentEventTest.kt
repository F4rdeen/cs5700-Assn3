package simulator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShipmentEventTest {
    @Test
    fun `fail - type should not be delivered`() {
        val event = ShipmentEvent("created", "id", 123456789L, "info")
        try {
            assertEquals("delivered", event.type)
            fail("Test should have failed but passed")
        } catch (e: AssertionError) {
            // Expected failure, test passes
        }
    }
    @Test
    fun `ShipmentEvent stores type, id, timestamp, info`() {
        val event = ShipmentEvent("created", "id", 123456789L, "info")
        assertEquals("created", event.type)
        assertEquals("id", event.shipmentId)
        assertEquals(123456789L, event.timestamp)
        assertEquals("info", event.info)
    }
}
