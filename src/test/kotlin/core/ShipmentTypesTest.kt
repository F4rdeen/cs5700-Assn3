package core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class ShipmentTypesTest {
    @Test
    fun `StandardShipment validateDeliveryRules does not add violations`() {
        val shipment = StandardShipment("id", System.currentTimeMillis())
        shipment.setDeliveryTimestamp(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(5))
        assertTrue(shipment.violations.isEmpty())
    }

    @Test
    fun `ExpressShipment adds violation for past delivery`() {
        val shipment = ExpressShipment("id", System.currentTimeMillis())
        shipment.setDeliveryTimestamp(System.currentTimeMillis() - 1000)
        assertTrue(shipment.violations.any { it.contains("past") })
    }

    @Test
    fun `OvernightShipment adds violation for more than 24h delivery`() {
        val shipment = OvernightShipment("id", System.currentTimeMillis())
        shipment.setDeliveryTimestamp(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(25))
        assertTrue(shipment.violations.any { it.contains("24-hour") })
    }

    @Test
    fun `BulkShipment adds violation for less than 3 day delivery`() {
        val shipment = BulkShipment("id", System.currentTimeMillis())
        shipment.setDeliveryTimestamp(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2))
        assertTrue(shipment.violations.any { it.contains("3-day minimum") })
    }
}
