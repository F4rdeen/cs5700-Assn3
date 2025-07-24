package core

interface UpdateHandler {
    fun handle(shipment: Shipment, timestamp: Long, info: String?)
}