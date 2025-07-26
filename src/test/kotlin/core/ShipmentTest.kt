package core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShipmentTest {
    class TestShipment(id: String, createdAt: Long) : Shipment(id, createdAt) {
        override fun validateDeliveryRules() {}
    }

    @Test
    fun `toDTO returns correct values`() {
        val shipment = TestShipment("id", 123L)
        shipment.setStatus("shipped")
        shipment.setLocation("Warehouse")
        shipment.setDeliveryTimestamp(456L)
        shipment.addNote("Test note")
        shipment.addViolation("Test violation")
        val dto = shipment.toDTO()
        assertEquals("id", dto.id)
        assertEquals("shipped", dto.status)
        assertEquals("Warehouse", dto.currentLocation)
        assertEquals(456L, dto.expectedDeliveryDateTimestamp)
        assertTrue(dto.notes.any { it.message == "Test note" })
        assertTrue(dto.violations.contains("Test violation"))
    }

    @Test
    fun `addObserver and notifyObservers works`() {
        val shipment = TestShipment("id", 0L)
        var called = false
        val observer = object : ShipmentObserver {
            override fun onUpdate(shipment: Shipment) { called = true }
        }
        shipment.addObserver(observer)
        shipment.setStatus("shipped")
        assertTrue(called)
    }


}
