package simulator

data class ShipmentEvent(
    val type: String,
    val shipmentId: String,
    val timestamp: Long,
    val info: String?
)