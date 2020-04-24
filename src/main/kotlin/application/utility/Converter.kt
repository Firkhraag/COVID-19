package application.utility

import kotlin.math.abs

// Конвертер gps координат (широта, долгота) в прямоугольную систему координат
fun gpsToXy(gpsCoord: Pair<Double, Double>, width: Int, height: Int): Pair<Int, Int> {
    val topLeftGpsCoord = Pair(37.36526, 55.91366)
    val bottomRightGpsCoord = Pair(37.86479, 55.56637)
    val y = (abs((gpsCoord.second - topLeftGpsCoord.second) /
            (bottomRightGpsCoord.second - topLeftGpsCoord.second) * width)).toInt()
    val x = (abs((gpsCoord.first - topLeftGpsCoord.first) /
            (bottomRightGpsCoord.first - topLeftGpsCoord.first) * height)).toInt()
    return Pair(x, height - y)
}
