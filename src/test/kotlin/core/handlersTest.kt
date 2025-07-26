package core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HandlersTest {
    class DummyShipment(id: String, createdAt: Long) : Shipment(id, createdAt) {
        override fun validateDeliveryRules() {}
    }

    @Test
    fun `CreatedHandler sets status to created`() {
        val shipment = DummyShipment("id", 0L)
        CreatedHandler().handle(shipment, 0, null)
        assertEquals("created", shipment.toDTO().status)
    }

    @Test
    fun `ShippedHandler sets status and delivery timestamp`() {
        val shipment = DummyShipment("id", 0L)
        ShippedHandler().handle(shipment, 0, "123456789")
        assertEquals("shipped", shipment.toDTO().status)
        assertEquals(123456789L, shipment.toDTO().expectedDeliveryDateTimestamp)
    }

    @Test
    fun `LocationHandler sets location`() {
        val shipment = DummyShipment("id", 0L)
        LocationHandler().handle(shipment, 0, "Warehouse")
        assertEquals("Warehouse", shipment.toDTO().currentLocation)
    }

    @Test
    fun `DeliveredHandler sets status to delivered`() {
        val shipment = DummyShipment("id", 0L)
        DeliveredHandler().handle(shipment, 0, null)
        assertEquals("delivered", shipment.toDTO().status)
    }

    @Test
    fun `DelayedHandler sets status and delivery timestamp`() {
        val shipment = DummyShipment("id", 0L)
        DelayedHandler().handle(shipment, 0, "987654321")
        assertEquals("delayed", shipment.toDTO().status)
        assertEquals(987654321L, shipment.toDTO().expectedDeliveryDateTimestamp)
    }

    @Test
    fun `LostHandler sets status to lost`() {
        val shipment = DummyShipment("id", 0L)
        LostHandler().handle(shipment, 0, null)
        assertEquals("lost", shipment.toDTO().status)
    }

    @Test
    fun `CanceledHandler sets status to canceled`() {
        val shipment = DummyShipment("id", 0L)
        CanceledHandler().handle(shipment, 0, null)
        assertEquals("canceled", shipment.toDTO().status)
    }

    @Test
    fun `NoteAddedHandler adds note if info is not blank`() {
        val shipment = DummyShipment("id", 0L)
        NoteAddedHandler().handle(shipment, 0, "Handle with care")
        assertTrue(shipment.toDTO().notes.any { it.message == "Handle with care" })
    }
}
