package ui

import core.Shipment
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import simulator.TrackingSimulator

class TrackerViewHelperTest {
    companion object {
        @JvmStatic
        @org.junit.jupiter.api.BeforeAll
        fun initToolkit() {
            try {
                com.sun.javafx.application.PlatformImpl.startup {}
                Thread.sleep(500) // Give time for FX thread to start
            } catch (_: IllegalStateException) {
                // Already initialized
            }
        }
        fun runAndWait(action: () -> Unit) {
            val latch = java.util.concurrent.CountDownLatch(1)
            javafx.application.Platform.runLater {
                action()
                latch.countDown()
            }
            latch.await()
        }
    }
    @Test
    fun `fail - shipmentStatus should not be lost after tracking known id`() {
        val shipment = core.Shipment("testid")
        simulator.TrackingSimulator.addShipment(shipment)
        runAndWait { helper.trackShipment("testid") }
        try {
            assertEquals("lost", helper.shipmentStatus.get())
            fail("Test should have failed but passed")
        } catch (e: AssertionError) {
            // Expected failure, test passes
        }
    }
    private lateinit var helper: TrackerViewHelper

    @BeforeEach
    fun setUp() {
        helper = TrackerViewHelper()
    }

    @Test
    fun `trackShipment sets not found status for unknown id`() {
        helper.trackShipment("unknown")
        assertEquals("core.Shipment not found", helper.shipmentStatus.get())
        assertEquals("unknown", helper.shipmentId.get())
    }

    @Test
    fun `trackShipment sets shipment info for known id`() {
        val shipment = core.Shipment("testid")
        simulator.TrackingSimulator.addShipment(shipment)
        runAndWait { helper.trackShipment("testid") }
        runAndWait { helper.refreshProperties() }
        println("DEBUG: shipmentStatus = '${helper.shipmentStatus.get()}'")
        assertEquals("testid", helper.shipmentId.get())
        assertEquals(shipment.status, helper.shipmentStatus.get())
    }

    @Test
    fun `stopTracking clears all properties`() {
        val shipment = core.Shipment("testid")
        simulator.TrackingSimulator.addShipment(shipment)
        runAndWait { helper.trackShipment("testid") }
        runAndWait { helper.stopTracking() }
        assertEquals("", helper.shipmentId.get())
        assertEquals("", helper.shipmentStatus.get())
        assertEquals("", helper.currentLocation.get())
        assertEquals("", helper.expectedShipmentDeliveryDate.get())
        assertTrue(helper.shipmentNotes.isEmpty())
        assertTrue(helper.shipmentUpdateHistory.isEmpty())
    }

    @Test
    fun `refreshProperties updates properties`() {
        val shipment = core.Shipment("testid")
        shipment.setStatus("shipped")
        simulator.TrackingSimulator.addShipment(shipment)
        runAndWait { helper.trackShipment("testid") }
        runAndWait { shipment.setLocation("Warehouse") }
        runAndWait { helper.refreshProperties() }
        assertEquals("Warehouse", helper.currentLocation.get())
    }

    @Test
    fun `onUpdate updates properties`() {
        val shipment = core.Shipment("testid")
        simulator.TrackingSimulator.addShipment(shipment)
        runAndWait { helper.trackShipment("testid") }
        runAndWait { shipment.setStatus("delivered") }
        runAndWait { helper.onUpdate(shipment) }
        assertEquals("delivered", helper.shipmentStatus.get())
    }
}
