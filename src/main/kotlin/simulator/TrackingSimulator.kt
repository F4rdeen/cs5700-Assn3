package simulator

import core.CanceledHandler
import core.CreatedHandler
import core.DelayedHandler
import core.DeliveredHandler
import core.LocationHandler
import core.LostHandler
import core.NoteAddedHandler
import core.Shipment
import core.ShippedHandler
import simulator.ShipmentEvent
import kotlinx.coroutines.*
import java.io.File

object TrackingSimulator {
    private val shipments = mutableMapOf<String, Shipment>()
    private val handlers = mapOf(
        "created" to CreatedHandler(),
        "shipped" to ShippedHandler(),
        "location" to LocationHandler(),
        "delivered" to DeliveredHandler(),
        "delayed" to DelayedHandler(),
        "lost" to LostHandler(),
        "canceled" to CanceledHandler(),
        "noteadded" to NoteAddedHandler()
    )
    private lateinit var events: List<ShipmentEvent>
    private var simulationJob: Job? = null

    fun initialize(filePath: String) {
        events = File(filePath).readLines()
            .mapNotNull { parseEvent(it) }
            .sortedBy { it.timestamp }
    }

    private fun parseEvent(line: String): ShipmentEvent? {
        val parts = line.split(",")
        if (parts.size < 3) return null
        val type = parts[0]
        val shipmentId = parts[1]
        val timestamp = parts[2].toLongOrNull() ?: return null
        val info = parts.getOrNull(3)
        return ShipmentEvent(type, shipmentId, timestamp, info)
    }

    fun runSimulation() {
        simulationJob?.cancel()
        simulationJob = CoroutineScope(Dispatchers.Default).launch {
            for (event in events) {
                val shipment = shipments.getOrPut(event.shipmentId) {
                    Shipment(event.shipmentId).apply {
                        addShipment(this)
                    }
                }
                handlers[event.type]?.handle(shipment, event.timestamp, event.info)
                delay(1000) // 1-second delay between updates
            }
        }
    }

    fun findShipment(id: String): Shipment? = shipments[id]

    fun addShipment(shipment: Shipment) {
        shipments[shipment.id] = shipment
    }

    fun stopSimulation() {
        simulationJob?.cancel()
    }
}