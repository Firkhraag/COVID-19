package application.view

import application.controller.MyController
import javafx.scene.chart.*
import tornadofx.*

class Graph : View("Графики") {

    private val controller: MyController by inject()

    override val root = stackpane {

        // Общее число зарегистрированных случаев
        val xAxis1 = CategoryAxis()
        val yAxis1 = NumberAxis()
        xAxis1.label = "День"
        xAxis1.animated = false
        yAxis1.label = "Человек"
        yAxis1.animated = false
        val chart1 = LineChart(xAxis1, yAxis1)
        chart1.styleClass.add("graph1")
        chart1.title = "Общее число случаев"
        chart1.animated = false
        chart1.isLegendVisible = false
        chart1.data.add(controller.series1)
        chart1.data.add(controller.series1Real)

        // Число зарегистрированных активных больных
        val xAxis2 = CategoryAxis()
        val yAxis2 = NumberAxis()
        xAxis2.label = "День"
        xAxis2.animated = false
        yAxis2.label = "Человек"
        yAxis2.animated = false
        val chart2 = LineChart(xAxis2, yAxis2)
        chart2.styleClass.add("graph2")
        chart2.title = "Число активных больных"
        chart2.animated = false
        chart2.isLegendVisible = false
        chart2.data.add(controller.series2)
        chart2.data.add(controller.series2Real)

        // Число зарегистрированных новых случаев
        val xAxis3 = CategoryAxis()
        val yAxis3 = NumberAxis()
        xAxis3.label = "День"
        xAxis3.animated = false
        yAxis3.label = "Человек"
        yAxis3.animated = false
        val chart3 = LineChart(xAxis3, yAxis3)
        chart3.styleClass.add("graph3")
        chart3.title = "Число новых случаев"
        chart3.animated = false
        chart3.isLegendVisible = false
        chart3.data.add(controller.series3)
        chart3.data.add(controller.series3Real)

        // Общее число смертей
        val xAxis4 = CategoryAxis()
        val yAxis4 = NumberAxis()
        xAxis4.label = "День"
        xAxis4.animated = false
        yAxis4.label = "Человек"
        yAxis4.animated = false
        val chart4 = LineChart(xAxis4, yAxis4)
        chart4.styleClass.add("graph4")
        chart4.title = "Общее число смертей"
        chart4.animated = false
        chart4.isLegendVisible = false
        chart4.data.add(controller.series4)
        chart4.data.add(controller.series4Real)

        vbox {
            hbox {
                add(chart1)
                add(chart2)
            }.apply {
                this.spacing = 20.0
            }

            hbox {
                add(chart3)
                add(chart4)
            }.apply {
                this.spacing = 20.0
            }
        }.apply {
            this.spacing = 10.0
        }

    }
}
