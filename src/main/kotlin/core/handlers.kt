package core

// core.CreatedHandler.kt
class CreatedHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        shipment.setStatus("created")
    }
}

// core.ShippedHandler.kt
class ShippedHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        shipment.setStatus("shipped")
        shipment.setDeliveryTimestamp(info?.toLongOrNull() ?: 0)
    }
}

// core.LocationHandler.kt
class LocationHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        shipment.setLocation(info ?: "Unknown")
    }
}

// core.DeliveredHandler.kt
class DeliveredHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        shipment.setStatus("delivered")
    }
}

// core.DelayedHandler.kt
class DelayedHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        shipment.setStatus("delayed")
        shipment.setDeliveryTimestamp(info?.toLongOrNull() ?: shipment.expectedDeliveryDateTimestamp)
    }
}

// core.LostHandler.kt
class LostHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        shipment.setStatus("lost")
    }
}

// core.CanceledHandler.kt
class CanceledHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        shipment.setStatus("canceled")
    }
}

// core.NoteAddedHandler.kt
class NoteAddedHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        if (!info.isNullOrBlank()) {
            shipment.addNote(info)
        }
    }
}