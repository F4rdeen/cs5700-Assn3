package core

import java.util.concurrent.TimeUnit
import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val message: String,
    val timestamp: Long
)

@Serializable
data class ShipmentDTO(
    val id: String,
    val status: String,
    val currentLocation: String,
    val expectedDeliveryDateTimestamp: Long,
    val notes: List<Note>,
    val violations: List<String>,
    val updateHistory: List<ShippingUpdate>
)

abstract class Shipment(val id: String, val createdAt: Long) {
    var status: String = "created"
        private set
    val notes = mutableListOf<Note>()
    val updateHistory = mutableListOf<ShippingUpdate>()
    var expectedDeliveryDateTimestamp: Long = 0
        protected set
    var currentLocation: String = "Unknown"
        protected set
    val violations = mutableListOf<String>()

    private val observers = mutableListOf<ShipmentObserver>()

    abstract fun validateDeliveryRules()

    fun toDTO(): ShipmentDTO {
        return ShipmentDTO(
            id = id,
            status = status,
            currentLocation = currentLocation,
            expectedDeliveryDateTimestamp = expectedDeliveryDateTimestamp,
            notes = notes.toList(),
            violations = violations.toList(),
            updateHistory = updateHistory.toList()
        )
    }

    fun addViolation(message: String) {
        violations.add(message)
        notifyObservers()
    }

    fun addObserver(observer: ShipmentObserver) {
        if (!observers.contains(observer)) {
            observers.add(observer)
        }
    }

    fun removeObserver(observer: ShipmentObserver) {
        observers.remove(observer)
    }

    fun notifyObservers() {
        observers.forEach { it.onUpdate(this) }
    }

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
        validateDeliveryRules()
        notifyObservers()
    }

    fun addNote(message: String) {
        notes.add(Note(message, System.currentTimeMillis()))
        notifyObservers()
    }

    fun addUpdate(update: ShippingUpdate) {
        updateHistory.add(update)
        notifyObservers() // Added missing notification
    }
}