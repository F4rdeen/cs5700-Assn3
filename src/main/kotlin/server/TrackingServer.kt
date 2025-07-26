
package server

import ShipmentFactory
import core.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.server.request.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

object TrackingServer {
    private val shipments = ConcurrentHashMap<String, Shipment>()
    private val connections = ConcurrentHashMap<String, MutableList<WebSocketSession>>()
    private val json = Json { prettyPrint = true }

    fun start(port: Int = 8080) {
        embeddedServer(Netty, port = port) {
            install(WebSockets)

            routing {
                // Create shipment endpoint
                post("/shipments") {
    val id = call.request.queryParameters["id"] ?: ""
    val type = call.request.queryParameters["type"] ?: "STANDARD"

    if (id.isBlank()) {
        call.respondText("Missing ID", status = HttpStatusCode.BadRequest)
        return@post
    }
    
    if (shipments.containsKey(id)) {
        call.respondText("Shipment ID already exists", status = HttpStatusCode.Conflict)
        return@post
    }

    try {
        val shipmentType = ShipmentFactory.Type.valueOf(type.uppercase())
        createShipment(id, shipmentType)
        call.respondText("Shipment created", status = HttpStatusCode.Created)
    } catch (e: IllegalArgumentException) {
        call.respondText("Invalid shipment type", status = HttpStatusCode.BadRequest)
    }
}

                // Get shipment endpoint
                get("/shipments/{id}") {
                    val id = call.parameters["id"] ?: ""
                    val shipment = shipments[id]

                    if (shipment == null) {
                        call.respondText("Shipment not found", status = HttpStatusCode.NotFound)
                    } else {
                        call.respondText(json.encodeToString(shipment.toDTO()), status = HttpStatusCode.OK)
                    }
                }

                // Update shipment endpoint
                post("/updates") {
                    val update = call.receiveText()
                    if (processUpdate(update)) {
                        call.respondText("Update processed")
                    } else {
                        call.respondText("Invalid update format", status = HttpStatusCode.BadRequest)
                    }
                }

                // Tracking endpoint
                webSocket("/track/{id}") {
                    val id = call.parameters["id"] ?: return@webSocket
                    val shipment = shipments[id]
                    
                    if (shipment == null) {
                        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Shipment not found"))
                        return@webSocket
                    }
                    
                    registerConnection(id, this)
                    
                    try {
                        // Send initial state
                        send(Frame.Text(json.encodeToString(shipment.toDTO())))

                        // Keep connection open
                        for (frame in incoming) {
                            // We don't need to process incoming messages from client
                        }
                    } finally {
                        unregisterConnection(id, this)
                    }
                }
            }
        }.start(wait = false)
    }

    private fun processUpdate(update: String): Boolean {
    val parts = update.split(",")
    if (parts.size < 3) return false

    val type = parts[0]
    val id = parts[1]
    val timestamp = parts[2].toLongOrNull() ?: return false
    val info = parts.getOrNull(3)

    // Special handling for creation updates
    if (type == "created" && !shipments.containsKey(id)) {
        val shipmentType = try {
            ShipmentFactory.Type.valueOf(info?.uppercase() ?: "STANDARD")
        } catch (e: Exception) {
            ShipmentFactory.Type.STANDARD
        }
        createShipment(id, shipmentType, timestamp)
    }

    val shipment = shipments[id] ?: return false
    val handler = when (type) {
        "created" -> CreatedHandler()
        "shipped" -> ShippedHandler()
        "location" -> LocationHandler()
        "delivered" -> DeliveredHandler()
        "delayed" -> DelayedHandler()
        "lost" -> LostHandler()
        "canceled" -> CanceledHandler()
        "noteadded" -> NoteAddedHandler()
        else -> null
    } ?: return false

    handler.handle(shipment, timestamp, info)
    broadcastUpdate(id)
    return true
}

private fun createShipment(id: String, type: ShipmentFactory.Type, createdAt: Long = System.currentTimeMillis()) {
    shipments[id] = ShipmentFactory.create(id, type, createdAt)
}

    private fun registerConnection(shipmentId: String, session: WebSocketSession) {
        connections.getOrPut(shipmentId) { mutableListOf() }.add(session)
    }

    private fun unregisterConnection(shipmentId: String, session: WebSocketSession) {
        connections[shipmentId]?.remove(session)
    }

    private fun broadcastUpdate(shipmentId: String) {
        val shipment = shipments[shipmentId] ?: return
        val dto = shipment.toDTO()
        val jsonString = json.encodeToString(dto)

        connections[shipmentId]?.forEach { session ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    session.send(Frame.Text(jsonString))
                } catch (e: Exception) {
                    unregisterConnection(shipmentId, session)
                }
            }
        }
    }
}