package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import core.Note
import core.ShipmentDTO
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

// Top-level date formatting function
private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0) return "Unknown"
    return SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(timestamp))
}

@Composable
fun TrackerUI() {
    var shipmentId by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val trackedShipments = remember { mutableStateMapOf<String, ShipmentState>() }
    val scope = rememberCoroutineScope()
    val httpClient = remember { HttpClient(CIO) }

    Column(modifier = Modifier.padding(16.dp)) {
        // Show error message if any
        errorMessage?.let {
            Text(it, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Tracking ID input
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = shipmentId,
                onValueChange = { shipmentId = it },
                label = { Text("Tracking ID") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (shipmentId.isNotBlank() && !trackedShipments.containsKey(shipmentId)) {
                    scope.launch {
                        try {
                            // Validate shipment exists on server
                            val response: HttpResponse = httpClient.get("http://localhost:8080/shipments/$shipmentId")
                            if (response.status == HttpStatusCode.OK) {
                                val state = ShipmentState(shipmentId)
                                state.trackShipment(scope)
                                trackedShipments[shipmentId] = state
                                shipmentId = ""
                                errorMessage = null
                            } else {
                                errorMessage = "Shipment not found: $shipmentId"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                        }
                    }
                } else if (shipmentId.isBlank()) {
                    errorMessage = "Please enter a shipment ID"
                }
            }) {
                Text("Track Shipment")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tracked shipments
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(trackedShipments.values.toList()) { shipment ->
                ShipmentCard(shipment, onRemove = {
                    shipment.stopTracking()
                    trackedShipments.remove(shipment.id)
                })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
@Composable
fun ShipmentCard(state: ShipmentState, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with ID and remove button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Shipment ID: ${state.id}", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "Stop tracking")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status and location
            Row {
                Text("Status: ", fontWeight = FontWeight.Bold)
                Text(
                    state.status,
                    color = when (state.status) {
                        "delayed" -> Color(0xFFFFA500) // Orange
                        "lost" -> Color.Red
                        "canceled" -> Color.DarkGray
                        else -> Color.Unspecified
                    }
                )
            }

            Row {
                Text("Location: ", fontWeight = FontWeight.Bold)
                Text(state.location)
            }

            Row {
                Text("Expected Delivery: ", fontWeight = FontWeight.Bold)
                Text(state.expectedDelivery)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Violations
            if (state.violations.isNotEmpty()) {
                Text("RULE VIOLATIONS:", fontWeight = FontWeight.Bold, color = Color.Red)
                state.violations.forEach { violation ->
                    Text("- $violation", color = Color.Red)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Notes
            Text("Notes:", fontWeight = FontWeight.Bold)
            Column(modifier = Modifier.padding(start = 8.dp)) {
                state.notes.forEach { note ->
                    Text("- ${note.message} (${formatDate(note.timestamp)})")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // History
            Text("History:", fontWeight = FontWeight.Bold)
            Column(modifier = Modifier.padding(start = 8.dp)) {
                state.history.forEach { event ->
                    Text("- $event")
                }
            }
        }
    }
}

class ShipmentState(val id: String) {
    var status by mutableStateOf("Loading...")
    var location by mutableStateOf("")
    var expectedDelivery by mutableStateOf("")
    var violations by mutableStateOf(emptyList<String>())
    var notes by mutableStateOf(emptyList<Note>())
    var history by mutableStateOf(emptyList<String>())

    private var webSocketJob: Job? = null
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }


    fun trackShipment(scope: CoroutineScope) {
        webSocketJob = scope.launch(Dispatchers.IO) {
            try {
                client.webSocket("ws://localhost:8080/track/$id") {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val dto = Json.decodeFromString<ShipmentDTO>(frame.readText())
                            updateFromDTO(dto)
                        }
                    }
                }
            } catch (e: Exception) {
                status = "Error: ${e.message}"
            }
        }
    }

    fun stopTracking() {
        webSocketJob?.cancel()
        client.close()
    }

    private fun updateFromDTO(dto: ShipmentDTO) {
        status = dto.status
        location = dto.currentLocation
        expectedDelivery = formatDate(dto.expectedDeliveryDateTimestamp)
        violations = dto.violations
        notes = dto.notes
        history = dto.updateHistory.map {
            "From ${it.previousStatus} to ${it.newStatus} at ${formatDate(it.timestamp)}"
        }
    }
}