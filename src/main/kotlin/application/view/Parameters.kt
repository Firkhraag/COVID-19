package application.view

import application.controller.MyController
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.paint.Color
import tornadofx.*

class Parameters : View("Параметры") {

    private val controller: MyController by inject()

    private var buttonText = SimpleStringProperty("Старт")

    override val root = stackpane {

        val statusLabel = Label()
        statusLabel.minWidth = 250.0
        statusLabel.textFill = Color.BLUE

        val progressBar = ProgressBar(0.0)
        progressBar.managedProperty().bind(visibleProperty())
        progressBar.visibleProperty().bind(controller.showProgressBar)
        progressBar.styleClass.add("progress")

        vbox {
            hbox {
                vbox {
                    label("Коэффициент продолжительности")
                    textfield().apply {
                        disableProperty().bind(controller.started)
                        textProperty().bindBidirectional(controller.durationCoefficientTextField)
                    }
                }.apply {
                    alignment = Pos.CENTER
                    spacing = 5.0
                }

                vbox {
                    label("Коэффициент восприимчивости")
                    textfield().apply {
                        disableProperty().bind(controller.started)
                        textProperty().bindBidirectional(controller.susceptibilityLowestInfluenceTextField)
                    }
                }.apply {
                    managedProperty().bind(visibleProperty())
                    alignment = Pos.CENTER
                    spacing = 5.0
                }

                vbox {
                    label("Коэффициент продолжительности2")
                    textfield().apply {
                        disableProperty().bind(controller.started)
                        textProperty().bindBidirectional(controller.durationCoefficientTextField)
                    }
                }.apply {
                    alignment = Pos.CENTER
                    spacing = 5.0
                }
            }.apply {
                managedProperty().bind(visibleProperty())
                alignment = Pos.CENTER
                spacing = 50.0
            }

            vbox {
                label("Начальная доля больных")
                textfield().apply {
                    disableProperty().bind(controller.started)
                    textProperty().bindBidirectional(controller.startingRatioTextField)
                }
            }.apply {
                managedProperty().bind(visibleProperty())
                alignment = Pos.CENTER
                spacing = 5.0
            }

            hbox {
                vbox {
                    label("Инкубационный период, дней")
                    textfield().apply {
                        disableProperty().bind(controller.started)
                        textProperty().bindBidirectional(controller.incubationPeriodTextField)
                    }
                }.apply {
                    managedProperty().bind(visibleProperty())
                    alignment = Pos.CENTER
                    spacing = 5.0
                }

                vbox {
                    label("Период болезни, дней")
                    textfield().apply {
                        disableProperty().bind(controller.started)
                        textProperty().bindBidirectional(controller.durationPeriodTextField)
                    }
                }.apply {
                    managedProperty().bind(visibleProperty())
                    alignment = Pos.CENTER
                    spacing = 5.0
                }

            }.apply {
                managedProperty().bind(visibleProperty())
                alignment = Pos.CENTER
                spacing = 85.0
            }

            button(buttonText).apply {
                disableProperty().bind(controller.started)
            }.setOnAction {
                controller.started.set(true)
                controller.showProgressBar.set(true)

                val task = object : Task<Boolean?>() {
                    override fun call(): Boolean? {
                        controller.createPopulation()
                        controller.showProgressBar.set(false)
                        Platform.runLater {
                            controller.dateLabelText.set("1 Марта")
                        }
                        controller.runSimulation()
                        return true
                    }
                }
                progressBar.progressProperty().bind(controller.progress)
                Thread(task).start()
            }
            hbox {
                label("Создание популяции:").apply {
                    managedProperty().bind(visibleProperty())
                    visibleProperty().bind(controller.showProgressBar)
                }
                add(progressBar)
            }.apply {
                spacing = 20.0
                alignment = Pos.CENTER
            }
        }.apply {
            spacing = 20.0
            alignment = Pos.CENTER
        }
    }
}
