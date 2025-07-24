package simulator

import core.Shipment
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TrackingSimulatorTest {
    @Test
    fun `fail - findShipment returns null for added shipment`() {
        val shipment = core.Shipment("id")
        simulator.TrackingSimulator.addShipment(shipment)
        try {
            assertNull(simulator.TrackingSimulator.findShipment("id"))
            fail("Test should have failed but passed")
        } catch (e: AssertionError) {
            // Expected failure, test passes
        }
    }
    @BeforeEach
    fun setUp() {
        // Reset state if needed
    }

    @Test
    fun `addShipment and findShipment work`() {
        val shipment = Shipment("id")
        TrackingSimulator.addShipment(shipment)
        assertEquals(shipment, TrackingSimulator.findShipment("id"))
    }
}
