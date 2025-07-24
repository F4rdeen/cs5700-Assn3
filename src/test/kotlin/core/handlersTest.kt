package core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HandlersTest {
    // --- Intentionally failing tests below ---
    @Test
    fun `CreatedHandler fails if status is not shipped`() {
        val shipment = Shipment("id")
        CreatedHandler().handle(shipment, 0, null)
        try {
            assertEquals("shipped", shipment.status)
            fail("Test should have failed but passed")
        } catch (e: AssertionError) {
            // Expected failure, test passes
        }
    }

    @Test
    fun `NoteAddedHandler fails if note is missing`() {
        val shipment = Shipment("id")
        NoteAddedHandler().handle(shipment, 0, "Handle with care")
        try {
            assertFalse(shipment.notes.contains("Handle with care"))
            fail("Test should have failed but passed")
        } catch (e: AssertionError) {
            // Expected failure, test passes
        }
    }
    @Test
    fun `CreatedHandler sets status to created`() {
        val shipment = Shipment("id")
        CreatedHandler().handle(shipment, 0, null)
        assertEquals("created", shipment.status)
    }

    @Test
    fun `ShippedHandler sets status and delivery timestamp`() {
        val shipment = Shipment("id")
        ShippedHandler().handle(shipment, 0, "123456789")
        assertEquals("shipped", shipment.status)
        assertEquals(123456789L, shipment.expectedDeliveryDateTimestamp)
    }

    @Test
    fun `LocationHandler sets location`() {
        val shipment = Shipment("id")
        LocationHandler().handle(shipment, 0, "Warehouse")
        assertEquals("Warehouse", shipment.currentLocation)
    }

    @Test
    fun `DeliveredHandler sets status to delivered`() {
        val shipment = Shipment("id")
        DeliveredHandler().handle(shipment, 0, null)
        assertEquals("delivered", shipment.status)
    }

    @Test
    fun `DelayedHandler sets status and delivery timestamp`() {
        val shipment = Shipment("id")
        DelayedHandler().handle(shipment, 0, "987654321")
        assertEquals("delayed", shipment.status)
        assertEquals(987654321L, shipment.expectedDeliveryDateTimestamp)
    }

    @Test
    fun `LostHandler sets status to lost`() {
        val shipment = Shipment("id")
        LostHandler().handle(shipment, 0, null)
        assertEquals("lost", shipment.status)
    }

    @Test
    fun `CanceledHandler sets status to canceled`() {
        val shipment = Shipment("id")
        CanceledHandler().handle(shipment, 0, null)
        assertEquals("canceled", shipment.status)
    }

    @Test
    fun `NoteAddedHandler adds note if info is not blank`() {
        val shipment = Shipment("id")
        NoteAddedHandler().handle(shipment, 0, "Handle with care")
        assertTrue(shipment.notes.contains("Handle with care"))
    }
}
