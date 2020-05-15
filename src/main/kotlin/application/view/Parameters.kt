package application.view

import application.controller.MyController
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent.Task
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.paint.Color
import tornadofx.*

class Parameters : View("Параметры") {

    private val controller: MyController by inject()

    override val root = stackpane {

        val statusLabel = Label()
        statusLabel.minWidth = 250.0
        statusLabel.textFill = Color.BLUE

        val progressBar = ProgressBar(0.0)
        progressBar.managedProperty().bind(visibleProperty())
        progressBar.visibleProperty().bind(controller.showProgressBar)
        progressBar.styleClass.add("progress")

//        val explanationPane1 = stackpane {
//            label("Текст")
//        }
//        val questionButton1 = Button("?")
//        questionButton1.hoverProperty().addListener { _, _, new ->
//            if (new) {
//                explanationPane1.show();
//            } else {
//                explanationPane1.hide();
//            }
//        }

        vbox {
//
//            hbox {
//                vbox {
//                    label("Свойства популяции")
//                    label("Поля")
//                }.apply {
//                    styleClass.add("parameters-part")
//                }
//                vbox {
//                    label("Эпидемиологические свойства")
//                    label("Поля")
//                }.apply {
//                    styleClass.add("parameters-part")
//                }
//            }.apply {
//                spacing = 10.0
//            }

            hbox {
                vbox {
                    hbox {
                        label("Инкубационный период")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.durationIncubationTextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 77.0
                    }

                    hbox {
                        label("Период болезни в тяжелом случае")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.durationCriticalTextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 10.0
                    }

                    hbox {
//                        label("Средняя продолжительность симптомного периода болезни, дней")
                        label("Средний период болезни")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.durationSymptomaticTextField)
                        }
                    }.apply {
                        managedProperty().bind(visibleProperty())
                        alignment = Pos.CENTER_LEFT
                        spacing = 69.0
                    }

                    hbox {
//                        label("Средняя продолжительность бессимптомного периода болезни, дней")
                        label("Бессимптомный период болезни")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.durationAsymptomaticTextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 21.0
                    }

                    hbox {
                        label("День самоизоляции")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.isolationTextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 105.0
                    }

                    hbox {
                        label("День выявления")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.registrationTextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 128.0
                    }

                    hbox {
                        label("День смерти")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.deathTextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 152.0
                    }

                    hbox {
                        label("Начальное число больных")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.startingInfectedTextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
//                        spacing = 72.0
                        spacing = 63.2
                    }
                }.apply {
                    managedProperty().bind(visibleProperty())
                    alignment = Pos.CENTER
                    spacing = 15.0
                }


                vbox {

                    hbox {
                        label("Хронические заболевания")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.comorbidity1TextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 100.0
                    }

                    hbox {
                        label("Хронические заболевания (сдвиг)")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.comorbidity2TextField)
                        }
                    }.apply {
                        managedProperty().bind(visibleProperty())
                        alignment = Pos.CENTER_LEFT
                        spacing = 53.0
                    }

                    hbox {
                        label("Бессимптомная форма (коморбидность)")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.asymptomatic1TextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 10.0
                    }

                    hbox {
                        label("Бессимптомная форма")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.asymptomatic2TextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 124.0
                    }

                    hbox {
                        label("Тяжелая форма (коморбидность)")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.critical1TextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 57.0
                    }

                    hbox {
                        label("Тяжелая форма")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.critical2TextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 170.0
                    }

                    hbox {
                        label("Смерть (коморбидность)")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.death1TextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 110.0
                    }

                    hbox {
                        label("Смерть")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.death2TextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 224.0
                    }
                }.apply {
                    managedProperty().bind(visibleProperty())
                    alignment = Pos.CENTER
                    spacing = 15.0
                }

                vbox {
                    hbox {
                        label("Влияние продолжительности контакта")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.durationInfluenceTextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 10.0
                    }

                    hbox {
                        label("Влияние силы инфекции")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.viralLoadInfluenceTextField)
                        }
                    }.apply {
                        alignment = Pos.CENTER_LEFT
                        spacing = 100.0
                    }

                    hbox {
                        label("Влияние восприимчивости")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.susceptibilityInfluenceTextField)
                        }
                    }.apply {
                        managedProperty().bind(visibleProperty())
                        alignment = Pos.CENTER_LEFT
                        spacing = 84.0
                    }

                    hbox {
                        label("Влияние восприимчивости (сдвиг)")
                        textfield().apply {
                            disableProperty().bind(controller.started)
                            textProperty().bindBidirectional(controller.susceptibilityInfluence2TextField)
                        }
                    }.apply {
                        managedProperty().bind(visibleProperty())
                        alignment = Pos.CENTER_LEFT
                        spacing = 37.0
                    }


                }.apply {
                    managedProperty().bind(visibleProperty())
                    alignment = Pos.CENTER
                    spacing = 15.0
                }
            }.apply {
                spacing = 50.0
            }

            button("Старт").apply {
                disableProperty().bind(controller.started)
                styleClass.add("start-btn")
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
            vbox {
                label("Создание популяции").apply {
                    managedProperty().bind(visibleProperty())
                    visibleProperty().bind(controller.showProgressBar)
                    styleClass.add("progress-label")
                }
                add(progressBar)
            }.apply {
                spacing = 20.0
                alignment = Pos.CENTER
            }
        }.apply {
            spacing = 33.0
            padding = Insets(40.0, 20.0, 40.0, 20.0)
            alignment = Pos.TOP_CENTER
        }
    }
}
