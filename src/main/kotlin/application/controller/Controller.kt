package application.controller

import application.model.World
import javafx.beans.property.ReadOnlyDoubleWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.chart.XYChart
import tornadofx.Controller

// Контроллер для связи View с Model
open class MyController : Controller() {

    // Модель
    private lateinit var world: World

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
    val durationCoefficientTextField = SimpleStringProperty("6.0")
    val susceptibilityLowestInfluenceTextField = SimpleStringProperty("0.8")
    val startingRatioTextField = SimpleStringProperty("0.0001")
    val incubationPeriodTextField = SimpleStringProperty("5.2")
    val durationPeriodTextField = SimpleStringProperty("15.0")

    // Инициализация
    fun createPopulation() {
        world = World(progress)
        world.setParameters(durationCoefficientTextField.get().toDouble())
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
    }
}
