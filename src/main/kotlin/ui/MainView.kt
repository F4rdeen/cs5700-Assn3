package ui

import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.*
import simulator.TrackingSimulator
import tornadofx.*

class MainView : View("core.Shipment Tracker") {
    private val inputField = TextField()
    private val trackButton = Button("Track")
    private val container = VBox()
    private val scrollPane = ScrollPane(container).apply {
        isFitToWidth = true
        vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
        hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
    }

    private val trackingHelpers = mutableMapOf<String, TrackerViewHelper>()
    private val trackingPanels = mutableMapOf<String, VBox>()

    override val root = VBox(10.0).apply {
        padding = Insets(10.0)
        children.add(HBox(10.0).apply {
            children.addAll(
                Label("Tracking ID:"),
                inputField.apply { HBox.setHgrow(this, Priority.ALWAYS) },
                trackButton
            )
        })
        children.add(scrollPane.apply { VBox.setVgrow(this, Priority.ALWAYS) })
    }

    init {
        trackButton.setOnAction {
            val id = inputField.text.trim()
            if (id.isEmpty()) return@setOnAction

            if (trackingHelpers.containsKey(id)) {
                showAlert("Already Tracking", "core.Shipment $id is already being tracked")
                return@setOnAction
            }

            val helper = TrackerViewHelper()
            helper.trackShipment(id)
            // Ensure properties are refreshed after tracking starts
            helper.refreshProperties()

            if (helper.shipmentStatus.get() == "core.Shipment not found") {
                showAlert("Not Found", "core.Shipment $id does not exist")
                return@setOnAction
            }

            trackingHelpers[id] = helper
            addTrackingPanel(id, helper)
            inputField.clear()
        }
    }

    private fun addTrackingPanel(id: String, helper: TrackerViewHelper) {
        lateinit var panel: VBox
        panel = VBox(10.0).apply {
            padding = Insets(10.0)
            style {
                borderColor += box(javafx.scene.paint.Color.LIGHTGRAY)
                borderWidth += box(1.px)
                backgroundColor += javafx.scene.paint.Color.WHITESMOKE
            }

            children.addAll(
                Label().apply { textProperty().bind(helper.shipmentId) },
                HBox(5.0, Label("Status:"), Label().apply { textProperty().bind(helper.shipmentStatus) }),
                HBox(5.0, Label("Location:"), Label().apply { textProperty().bind(helper.currentLocation) }),
                HBox(5.0, Label("Expected Delivery:"), Label().apply { textProperty().bind(helper.expectedShipmentDeliveryDate) }),
                Label("Notes:"),
                ListView(helper.shipmentNotes).apply { prefHeight = 100.0 },
                Label("History:"),
                ListView(helper.shipmentUpdateHistory).apply { prefHeight = 200.0 },
                Button("Stop Tracking").apply {
                    setOnAction {
                        helper.stopTracking()
                        trackingHelpers.remove(id)
                        trackingPanels.remove(id)
                        runLater {
                            container.children.remove(panel)
                        }
                    }
                }
            )
        }

        trackingPanels[id] = panel
        container.children.add(panel)
    }

    private fun showAlert(title: String, message: String) {
        alert(Alert.AlertType.INFORMATION, title, message)
    }

    override fun onDock() {
        // Get resource URL using classloader
        val resource = javaClass.getResource("/test.txt")
        if (resource == null) {
            showAlert("Error", "Resource file not found: test.txt")
            return
        }

        // Initialize with the absolute path from the resource URL
        TrackingSimulator.initialize(resource.toURI().path)
        TrackingSimulator.runSimulation()
    }

    override fun onUndock() {
        TrackingSimulator.stopSimulation()
    }
}