package application.view

import application.controller.MyController
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.stage.Screen
import tornadofx.*
import kotlin.system.exitProcess

// GUI
class MainView : View("Агентная модель COVID-19") {

    private val controller: MyController by inject()

    override val root = borderpane {
        minWidth = Screen.getPrimary().bounds.width * 0.8
        minHeight = Screen.getPrimary().bounds.height * 0.8
        vgrow = Priority.ALWAYS
        hgrow = Priority.ALWAYS

        val dateLabel = Label()
        dateLabel.textProperty().bind(controller.dateLabelText)
        dateLabel.styleClass.add("date-label")

        top = stackpane {
            add(dateLabel)
        }
        center = tabpane {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            tab(Parameters::class)
            tab(Graph::class)
//            tab(GraphAll::class)
//            tab(Canvas::class)
        }
    }.also { primaryStage.setOnCloseRequest { exitProcess(0) } }
}
