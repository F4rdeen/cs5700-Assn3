package core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShipmentFactoryTest {
    @Test
    fun `create returns correct shipment type`() {
        val shipment = ShipmentFactory.create("id", ShipmentFactory.Type.STANDARD)
        assertEquals("id", shipment.id)
        assertEquals("created", shipment.status)
    }

    @Test
    fun `create returns express shipment`() {
        val shipment = ShipmentFactory.create("id2", ShipmentFactory.Type.EXPRESS, 123L)
        assertEquals("id2", shipment.id)
        assertEquals("created", shipment.status)
        assertTrue(shipment is ExpressShipment)
        assertEquals(123L, shipment.createdAt)
    }

    @Test
    fun `create returns overnight shipment`() {
        val shipment = ShipmentFactory.create("id3", ShipmentFactory.Type.OVERNIGHT, 456L)
        assertEquals("id3", shipment.id)
        assertEquals("created", shipment.status)
        assertTrue(shipment is OvernightShipment)
        assertEquals(456L, shipment.createdAt)
    }

    @Test
    fun `create returns bulk shipment`() {
        val shipment = ShipmentFactory.create("id4", ShipmentFactory.Type.BULK, 789L)
        assertEquals("id4", shipment.id)
        assertEquals("created", shipment.status)
        assertTrue(shipment is BulkShipment)
        assertEquals(789L, shipment.createdAt)
    }

    @Test
    fun `all types in enum are supported by factory`() {
        for (type in ShipmentFactory.Type.values()) {
            val shipment = ShipmentFactory.create("test-${type.name}", type, 1000L)
            assertEquals("test-${type.name}", shipment.id)
            assertEquals(1000L, shipment.createdAt)
        }
    }

    @Test
    fun `factory create uses current time by default`() {
        val before = System.currentTimeMillis()
        val shipment = ShipmentFactory.create("id5", ShipmentFactory.Type.STANDARD)
        val after = System.currentTimeMillis()
        assertTrue(shipment.createdAt in before..after)
    }
}
