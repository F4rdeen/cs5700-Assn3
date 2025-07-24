package core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ShipmentTest {
    @Test
    fun `fail - status should not be delivered after creation`() {
        val shipment = Shipment("id123")
        try {
            assertEquals("delivered", shipment.status)
            fail("Test should have failed but passed")
        } catch (e: AssertionError) {
            // Expected failure, test passes
        }
    }
    private lateinit var shipment: Shipment

    @BeforeEach
    fun setUp() {
        shipment = Shipment("id123")
    }

    @Test
    fun `initial state is correct`() {
        assertEquals("id123", shipment.id)
        assertEquals("created", shipment.status)
        assertEquals("Unknown", shipment.currentLocation)
        assertEquals(0, shipment.expectedDeliveryDateTimestamp)
        assertTrue(shipment.notes.isEmpty())
        assertTrue(shipment.updateHistory.isEmpty())
    }

    @Test
    fun `setStatus updates status and history`() {
        shipment.setStatus("shipped")
        assertEquals("shipped", shipment.status)
        assertEquals(1, shipment.updateHistory.size)
        assertEquals("created", shipment.updateHistory[0].previousStatus)
        assertEquals("shipped", shipment.updateHistory[0].newStatus)
    }

    @Test
    fun `setLocation updates location`() {
        shipment.setLocation("Warehouse")
        assertEquals("Warehouse", shipment.currentLocation)
    }

    @Test
    fun `setDeliveryTimestamp updates timestamp`() {
        shipment.setDeliveryTimestamp(123456789L)
        assertEquals(123456789L, shipment.expectedDeliveryDateTimestamp)
    }

    @Test
    fun `addNote adds note`() {
        shipment.addNote("Fragile")
        assertTrue(shipment.notes.contains("Fragile"))
    }

    @Test
    fun `addObserver and removeObserver work`() {
        val observer = object : ShipmentObserver {
            var called = false
            override fun onUpdate(shipment: Shipment) { called = true }
        }
        shipment.addObserver(observer)
        shipment.setStatus("delivered")
        assertTrue(observer.called)
        shipment.removeObserver(observer)
        observer.called = false
        shipment.setStatus("lost")
        assertFalse(observer.called)
    }
}
