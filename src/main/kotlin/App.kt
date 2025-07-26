import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.TrackerUI

fun main() = application {
    Window(
        title = "Shipment Tracker",
        onCloseRequest = ::exitApplication
    ) {
        MaterialTheme {
            TrackerUI()
        }
    }
}