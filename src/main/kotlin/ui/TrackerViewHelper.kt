package ui

import core.Shipment
import core.ShipmentObserver
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import simulator.TrackingSimulator
import java.text.SimpleDateFormat
import java.util.*
import tornadofx.runLater

class TrackerViewHelper : ShipmentObserver {
    private var shipment: Shipment? = null

    // State properties from original UML
    val shipmentId = SimpleStringProperty("")
    val shipmentStatus = SimpleStringProperty("")
    val shipmentNotes: ObservableList<String> = FXCollections.observableArrayList()
    val shipmentUpdateHistory: ObservableList<String> = FXCollections.observableArrayList()
    val expectedShipmentDeliveryDate = SimpleStringProperty("")
    val currentLocation = SimpleStringProperty("")

    fun refreshProperties() {
        updateDisplay()
    }

    fun trackShipment(id: String) {
        // Stop tracking current shipment if any
        shipment?.removeObserver(this)

        // Find new shipment
        shipment = TrackingSimulator.findShipment(id)
        if (shipment == null) {
            shipmentId.set(id)
            shipmentStatus.set("core.Shipment not found")
            return
        }

        shipmentId.set(id)
        shipment?.addObserver(this)
        updateDisplay()
    }

    fun stopTracking() {
        shipment?.removeObserver(this)
        shipment = null
        clearDisplay()
    }

    override fun onUpdate(shipment: Shipment) {
        updateDisplay()
    }

    private fun updateDisplay() {
        shipment?.let {
            runLater {
                shipmentStatus.set(it.status)
                currentLocation.set(it.currentLocation)
                expectedShipmentDeliveryDate.set(formatDate(it.expectedDeliveryDateTimestamp))
                shipmentNotes.setAll(it.notes)
                shipmentUpdateHistory.setAll(it.updateHistory.map { update ->
                    "core.Shipment went from ${update.previousStatus} to ${update.newStatus} on ${formatDate(update.timestamp)}"
                })
            }
        }
    }

    private fun clearDisplay() {
        shipmentId.set("")
        shipmentStatus.set("")
        currentLocation.set("")
        expectedShipmentDeliveryDate.set("")
        shipmentNotes.clear()
        shipmentUpdateHistory.clear()
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "Unknown"
        val date = Date(timestamp) // timestamp is already in ms
        return SimpleDateFormat("yyyy-MM-dd HH:mm").format(date)
    }
}