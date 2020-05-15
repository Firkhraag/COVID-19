package application.view

import application.controller.MyController
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TabPane
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

//        top = stackpane {
//            add(dateLabel)
//            label("Данные")
//            label("Модель")
//        }
        top = stackpane {

            hbox {
                label("• Данные").apply {
                    styleClass.add("data-label")
                }.apply {
                    padding = Insets(0.0, 0.0, 0.0, 20.0)
                }
                add(dateLabel)
                label("• Модель").apply {
                    styleClass.add("model-label")
                }.apply {
                    padding = Insets(0.0, 20.0, 0.0, 0.0)
                }
            }.apply {
                spacing = 15.0
                alignment = Pos.CENTER
            }

//            center = dateLabel
//            left = label("• Данные").apply {
//                styleClass.add("data-label")
//            }.apply {
//                padding = Insets(0.0, 0.0, 0.0, 20.0)
//            }
//            right = label("• Модель").apply {
//                styleClass.add("model-label")
//            }.apply {
//                padding = Insets(0.0, 20.0, 0.0, 0.0)
//            }

//            right = hbox {
//                label("• Данные").apply {
//                    styleClass.add("data-label")
//                }
//                label("• Модель").apply {
//                    styleClass.add("model-label")
//                }
//            }.apply {
//                spacing = 20.0
//                padding = Insets(0.0, 20.0, 0.0, 0.0)
//            }
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
