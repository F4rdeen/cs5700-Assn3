package core

interface ShipmentObserver {
    fun onUpdate(shipment: Shipment)
}