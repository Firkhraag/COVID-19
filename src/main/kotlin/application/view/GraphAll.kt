package application.view

import application.controller.MyController
import javafx.scene.chart.*
import tornadofx.*

// Not used
// Графики всех случаев
class GraphAll : View("Графики2") {

    private val controller: MyController by inject()

    override val root = stackpane {

        // Общее число всех случаев
        val xAxis1All = CategoryAxis()
        val yAxis1All = NumberAxis()
        xAxis1All.label = "День"
        xAxis1All.animated = false
        yAxis1All.label = "Человек"
        yAxis1All.animated = false
        val chart1All = AreaChart(xAxis1All, yAxis1All)
        chart1All.styleClass.add("graph1")
        chart1All.title = "Общее число всех случаев"
        chart1All.animated = false
        chart1All.isLegendVisible = false
        chart1All.data.add(controller.series1All)

        // Число всех активных больных
        val xAxis2All = CategoryAxis()
        val yAxis2All = NumberAxis()
        xAxis2All.label = "День"
        xAxis2All.animated = false
        yAxis2All.label = "Человек"
        yAxis2All.animated = false
        val chart2All = AreaChart(xAxis2All, yAxis2All)
        chart2All.styleClass.add("graph1")
        chart2All.title = "Число активных больных"
        chart2All.animated = false
        chart2All.isLegendVisible = false
        chart2All.data.add(controller.series2All)

//        chart.barGap = 0.0
//        chart.categoryGap = 1.0

        // Число всех новых случаев
        val xAxis3All = CategoryAxis()
        val yAxis3All = NumberAxis()
        xAxis3All.label = "День"
        xAxis3All.animated = false
        yAxis3All.label = "Человек"
        yAxis3All.animated = false
        val chart3All = AreaChart(xAxis3All, yAxis3All)
        chart3All.styleClass.add("graph2")
        chart3All.title = "Число новых случаев"
        chart3All.animated = false
        chart3All.isLegendVisible = false
        chart3All.data.add(controller.series3All)

        // Число новых смертей
        val xAxis5 = CategoryAxis()
        val yAxis5 = NumberAxis()
        xAxis5.label = "День"
        xAxis5.animated = false
        yAxis5.label = "Человек"
        yAxis5.animated = false
        val chart5 = AreaChart(xAxis5, yAxis5)
        chart5.styleClass.add("graph2")
        chart5.title = "Общее число смертей"
        chart5.animated = false
        chart5.isLegendVisible = false
        chart5.data.add(controller.series5)

        vbox {
            hbox {
                add(chart1All)
                add(chart2All)
            }.apply {
                this.spacing = 20.0
            }

            hbox {
                add(chart3All)
                add(chart5)
            }.apply {
                this.spacing = 20.0
            }
        }.apply {
            this.spacing = 10.0
        }

    }
}
