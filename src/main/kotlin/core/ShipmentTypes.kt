package core

import java.util.concurrent.TimeUnit

class StandardShipment(id: String, createdAt: Long) : Shipment(id, createdAt) {
    override fun validateDeliveryRules() {
        // No special rules
    }
}

class ExpressShipment(id: String, createdAt: Long) : Shipment(id, createdAt) {
    override fun validateDeliveryRules() {
        if (expectedDeliveryDateTimestamp == 0L) return

        // Validate future date
        if (expectedDeliveryDateTimestamp < System.currentTimeMillis()) {
            addViolation("Express shipment delivery date cannot be in the past")
            return
        }

        val days = TimeUnit.MILLISECONDS.toDays(expectedDeliveryDateTimestamp - createdAt)
        if (days > 3) {
            addViolation("Express shipment delivery exceeds 3-day limit")
        }
    }
}

class OvernightShipment(id: String, createdAt: Long) : Shipment(id, createdAt) {
    override fun validateDeliveryRules() {
        if (expectedDeliveryDateTimestamp == 0L) return

        // Validate future date
        if (expectedDeliveryDateTimestamp < System.currentTimeMillis()) {
            addViolation("Overnight shipment delivery date cannot be in the past")
            return
        }

        val hours = TimeUnit.MILLISECONDS.toHours(expectedDeliveryDateTimestamp - createdAt)
        if (hours >= 24) {
            addViolation("Overnight shipment delivery exceeds 24-hour limit")
        }
    }
}

class BulkShipment(id: String, createdAt: Long) : Shipment(id, createdAt) {
    override fun validateDeliveryRules() {
        if (expectedDeliveryDateTimestamp == 0L) return

        // Validate future date
        if (expectedDeliveryDateTimestamp < System.currentTimeMillis()) {
            addViolation("Bulk shipment delivery date cannot be in the past")
            return
        }

        val days = TimeUnit.MILLISECONDS.toDays(expectedDeliveryDateTimestamp - createdAt)
        if (days < 3) {
            addViolation("Bulk shipment delivery before 3-day minimum")
        }
    }
}