package core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShipmentObserverTest {
    class TestShipment(id: String, createdAt: Long) : Shipment(id, createdAt) {
        override fun validateDeliveryRules() {}
    }
    @Test
    fun `observer notifies on update`() {
        val shipment = TestShipment("id", 0L)
        var notified = false
        val observer = object : ShipmentObserver {
            override fun onUpdate(shipment: Shipment) {
                notified = true
            }
        }
        shipment.addObserver(observer)
        shipment.setStatus("shipped")
        assertTrue(notified)
    }
}
