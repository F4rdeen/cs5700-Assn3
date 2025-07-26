package server

fun main() {
    TrackingServer.start(port = 8080)
    println("Shipment tracking server started on port 8080")
    // Keep server running
    Thread.currentThread().join()
}