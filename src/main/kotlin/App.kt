import tornadofx.launch
import tornadofx.App
import ui.MainView

class ShipmentTrackerApp : App(MainView::class)

fun main() = launch<ShipmentTrackerApp>()