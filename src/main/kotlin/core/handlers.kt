package core

class CreatedHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        shipment.setStatus("created")
    }
}

class ShippedHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        shipment.setStatus("shipped")
        shipment.setDeliveryTimestamp(info?.toLongOrNull() ?: 0)
    }
}

class LocationHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        shipment.setLocation(info ?: "Unknown")
    }
}

class DeliveredHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        shipment.setStatus("delivered")
    }
}

class DelayedHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        shipment.setStatus("delayed")
        shipment.setDeliveryTimestamp(info?.toLongOrNull() ?: shipment.expectedDeliveryDateTimestamp)
    }
}

class LostHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        shipment.setStatus("lost")
    }
}

class CanceledHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        shipment.setStatus("canceled")
    }
}

class NoteAddedHandler : UpdateHandler {
    override fun handle(shipment: Shipment, timestamp: Long, info: String?) {
        if (!info.isNullOrBlank()) {
            shipment.addNote(info)
        }
    }
}