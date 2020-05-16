package application.view

import application.controller.MyController
import javafx.scene.chart.*
import tornadofx.*

// Deprecated
// Карта
// Deprecated
class Canvas : View("Карта") {

    private val controller: MyController by inject()

    override val root = stackpane {
        val xAxis = NumberAxis(0.0, 1000.0, 1.0)
        xAxis.styleClass.add("canvas-axis")
        val yAxis = NumberAxis(0.0, 1000.0, 1.0)
        yAxis.styleClass.add("canvas-axis")
        val chart = LineChart(xAxis, yAxis)
        chart.axisSortingPolicy = LineChart.SortingPolicy.NONE
        chart.styleClass.add("canvas")
        chart.isLegendVisible = false

//        var series = XYChart.Series<Number, Number>()
//        try {
//            val inputStream = FileInputStream(
//                "D:\\Dev\\Projects\\KotlinProjects\\MasterThesis\\src\\tables\\municipality_borders.xlsx")
//            val xlWb = WorkbookFactory.create(inputStream)
//
//            val table = xlWb.getSheetAt(0)
//
//            for (rowNumber in 0 until 107) {
//                print(rowNumber)
//                var k = 0
//                print(table.getRow(rowNumber).getCell(k))
//                while (table.getRow(rowNumber).getCell(k) != null) {
//                    val point = gpsToXy(Pair(table.getRow(rowNumber).getCell(k).numericCellValue.toDouble(),
//                        table.getRow(rowNumber).getCell(k + 1).numericCellValue.toDouble()), 1000, 1000)
//                    series.data.add(XYChart.Data<Number, Number>(point.first, point.second))
//                    k += 2
//                }
//                chart.data.add(series)
//                series = XYChart.Series<Number, Number>()
//            }
//            inputStream.close()
//        } catch(err: IOException) {
//            println("Unable to read the file")
//        }

//        series = XYChart.Series<Number, Number>()
//        val points = gpsToXy(Pair(37.640278, 55.845556), 1000, 1000)
//        series.data.add(XYChart.Data<Number, Number>(points.first, points.second))
//        series.data.add(XYChart.Data<Number, Number>(501, 500))
//        series.data.add(XYChart.Data<Number, Number>(500, 501))
//        series.data.add(XYChart.Data<Number, Number>(499, 500))
//        series.data.add(XYChart.Data<Number, Number>(500, 500))

//        chart.data.add(controller.infectedPoints)

        add(chart)

    }
}
