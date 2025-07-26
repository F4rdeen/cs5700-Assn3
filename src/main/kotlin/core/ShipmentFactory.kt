import core.Shipment
import core.StandardShipment
import core.ExpressShipment
import core.OvernightShipment
import core.BulkShipment

object ShipmentFactory {
    enum class Type { STANDARD, EXPRESS, OVERNIGHT, BULK }

    fun create(id: String, type: Type, createdAt: Long = System.currentTimeMillis()): Shipment {
        return when (type) {
            Type.STANDARD -> StandardShipment(id, createdAt)
            Type.EXPRESS -> ExpressShipment(id, createdAt)
            Type.OVERNIGHT -> OvernightShipment(id, createdAt)
            Type.BULK -> BulkShipment(id, createdAt)
        }
    }
}