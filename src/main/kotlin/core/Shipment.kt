package core

import javafx.collections.FXCollections
import javafx.collections.ObservableList

class Shipment(val id: String) {
    var status: String = "created"
        private set
    val notes: ObservableList<String> = FXCollections.observableArrayList()
    val updateHistory: ObservableList<ShippingUpdate> = FXCollections.observableArrayList()
    var expectedDeliveryDateTimestamp: Long = 0
        private set
    var currentLocation: String = "Unknown"
        private set

    private val observers = mutableListOf<ShipmentObserver>()

    fun addObserver(observer: ShipmentObserver) {
        if (!observers.contains(observer)) {
            observers.add(observer)
        }
    }

    fun removeObserver(observer: ShipmentObserver) {
        observers.remove(observer)
    }

    private fun notifyObservers() {
        observers.forEach { it.onUpdate(this) }
    }

    // State modification methods with notification
    fun setStatus(newStatus: String) {
        val previous = status
        status = newStatus
        addUpdate(ShippingUpdate(previous, newStatus, System.currentTimeMillis()))
        notifyObservers()
    }

    fun setLocation(location: String) {
        currentLocation = location
        notifyObservers()
    }

    fun setDeliveryTimestamp(timestamp: Long) {
        expectedDeliveryDateTimestamp = timestamp
        notifyObservers()
    }

    // Original methods from UML
    fun addNote(note: String) {
        notes.add(note)
        notifyObservers()
    }

    fun addUpdate(update: ShippingUpdate) {
        updateHistory.add(update)
    }
}