package application.controller

import application.model.World
import application.model.setParameters
import javafx.application.Platform
import javafx.beans.property.ReadOnlyDoubleWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.chart.XYChart
import tornadofx.Controller

// Контроллер для связи View с Model
open class MyController : Controller() {

    // Модель
    private lateinit var world: World
    // Популяция инициализирована
    private var populationIsCreated = false

    // Модель работает
    val started = SimpleBooleanProperty(false)
    // Показывать прогресс по созданию домохозяйств
    val showProgressBar = SimpleBooleanProperty(false)

    // Массивы данных для отображения на графиках
    val series1 = XYChart.Series<String, Number>()
    val series2 = XYChart.Series<String, Number>()
    val series3 = XYChart.Series<String, Number>()
    val series4 = XYChart.Series<String, Number>()

    val series1Real = XYChart.Series<String, Number>()
    val series2Real = XYChart.Series<String, Number>()
    val series3Real = XYChart.Series<String, Number>()
    val series4Real = XYChart.Series<String, Number>()

    val series1All = XYChart.Series<String, Number>()
    val series2All = XYChart.Series<String, Number>()
    val series3All = XYChart.Series<String, Number>()
    val series5 = XYChart.Series<String, Number>()

    // Дата
    val dateLabelText = SimpleStringProperty("День Месяц")
    // Прогресс по созданию домохозяйств
    val progress = ReadOnlyDoubleWrapper()

    // Параметры

    val durationIncubationTextField = SimpleStringProperty("5.2")
    val durationCriticalTextField = SimpleStringProperty("31.0")
    val durationSymptomaticTextField = SimpleStringProperty("20.0")
    val durationAsymptomaticTextField = SimpleStringProperty("14.0")
    val isolationTextField = SimpleStringProperty("2.9")
    val registrationTextField = SimpleStringProperty("6.1")
    val deathTextField = SimpleStringProperty("17.8")
    val startingInfectedTextField = SimpleStringProperty("50")

    val comorbidity1TextField = SimpleStringProperty("0.0111")
    val comorbidity2TextField = SimpleStringProperty("0.93")
    val asymptomatic1TextField = SimpleStringProperty("0.1")
    val asymptomatic2TextField = SimpleStringProperty("0.04")
    val critical1TextField = SimpleStringProperty("0.011")
    val critical2TextField = SimpleStringProperty("0.001")
    val death1TextField = SimpleStringProperty("0.01")
    val death2TextField = SimpleStringProperty("0.001")

    val durationInfluenceTextField = SimpleStringProperty("6.0")
    val viralLoadInfluenceTextField = SimpleStringProperty("0.05")
//    val susceptibilityInfluenceTextField = SimpleStringProperty("0.318")
    val susceptibilityInfluenceTextField = SimpleStringProperty("0.9")
    val susceptibilityInfluence2TextField = SimpleStringProperty("0.68")

    // Инициализация
    fun createPopulation() {
        setParameters(
            durationIncubationTextField.get().toDouble(),
            durationCriticalTextField.get().toDouble(),
            durationSymptomaticTextField.get().toDouble(),
            durationAsymptomaticTextField.get().toDouble(),
            isolationTextField.get().toDouble(),
            registrationTextField.get().toDouble(),
            deathTextField.get().toDouble(),
            startingInfectedTextField.get().toInt(),

            comorbidity1TextField.get().toDouble(),
            comorbidity2TextField.get().toDouble(),
            asymptomatic1TextField.get().toDouble(),
            asymptomatic2TextField.get().toDouble(),
            critical1TextField.get().toDouble(),
            critical2TextField.get().toDouble(),
            death1TextField.get().toDouble(),
            death2TextField.get().toDouble(),

            durationInfluenceTextField.get().toDouble(),
            viralLoadInfluenceTextField.get().toDouble(),
            susceptibilityInfluenceTextField.get().toDouble(),
            susceptibilityInfluence2TextField.get().toDouble()
        )
        if (!populationIsCreated) {
            world = World(progress)
            populationIsCreated = true
        } else {
            world.restartWorld()
            Platform.runLater {
                series1.data.clear()
                series2.data.clear()
                series3.data.clear()
                series4.data.clear()
                series1Real.data.clear()
                series3Real.data.clear()
                series4Real.data.clear()
            }
        }
    }

    // Симуляция
    fun runSimulation() {
        for (numOfIter in (1..1)) {
            world.runSimulation(
                numOfIter,
                series1, series2, series3, series4,
                series1Real, series2Real, series3Real, series4Real,
                dateLabelText
            )
        }
        started.set(false)
    }
}
