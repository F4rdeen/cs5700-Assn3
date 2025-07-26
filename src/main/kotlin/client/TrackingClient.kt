package client

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


fun main() = application {
    var updateText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var shipmentId by remember { mutableStateOf("") }
    var shipmentType by remember { mutableStateOf("STANDARD") }

    val client = HttpClient(CIO)
    val scope = CoroutineScope(Dispatchers.IO)

    Window(onCloseRequest = ::exitApplication, title = "Tracking Client") {
        MaterialTheme {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Create Shipment", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = shipmentId,
                    onValueChange = { shipmentId = it },
                    label = { Text("Shipment ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = shipmentType,
                    onValueChange = { shipmentType = it },
                    label = { Text("Shipment Type (STANDARD, EXPRESS, OVERNIGHT, BULK)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (shipmentId.isBlank()) {
                            message = "Error: Shipment ID cannot be empty"
                            return@Button
                        }
                        scope.launch {
                            try {
                                val response = client.post("http://localhost:8080/shipments") {
                                    parameter("id", shipmentId)
                                    parameter("type", shipmentType)
                                }
                                message = if (response.status.value in 200..299) {
                                    "Shipment $shipmentId created"
                                } else {
                                    "Error: ${response.status.description}"
                                }
                            } catch (e: Exception) {
                                message = "Error: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Create Shipment")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Enter Update String", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = updateText,
                    onValueChange = { updateText = it },
                    label = { Text("Format: type,id,timestamp,info") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Examples:", style = MaterialTheme.typography.caption)
                Text(" - created,s1,1690000000000,BULK", style = MaterialTheme.typography.caption)
                Text(" - shipped,s1,1690000001000,1690300000000", style = MaterialTheme.typography.caption)
                Text(" - location,s1,1690000002000,New York", style = MaterialTheme.typography.caption)
                Text(" - delivered,s1,1690000003000", style = MaterialTheme.typography.caption)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Validate update format
                        val parts = updateText.split(",")
                        if (parts.size < 3) {
                            message = "Error: Update must have at least 3 parts (type,id,timestamp)"
                            return@Button
                        }
                        // Validate timestamp
                        if (parts[2].toLongOrNull() == null) {
                            message = "Error: Invalid timestamp format"
                            return@Button
                        }
                        scope.launch {
                            try {
                                client.post("http://localhost:8080/updates") {
                                    setBody(updateText)
                                }
                                message = "Update sent successfully"
                            } catch (e: Exception) {
                                message = "Error: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Send Update")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(message)
            }
        }
    }
}