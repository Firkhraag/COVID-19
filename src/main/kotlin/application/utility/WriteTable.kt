package application.utility

import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

// Запись массива целых чисел в новую строку таблицы
fun writeTableInt(filepath: String, row: Int, stats: ArrayList<Int>) {
    try {
        val inputStream = FileInputStream(filepath)
        val xlWb = WorkbookFactory.create(inputStream)

        val xlWs = xlWb.getSheetAt(0)
        val xlRow = xlWs.createRow(row)

        stats.forEachIndexed { index, item ->
            val xlCol = xlRow.createCell(index)
            xlCol.setCellValue(item.toDouble())
        }
        val outputStream = FileOutputStream(filepath)
        xlWb.write(outputStream)
        xlWb.close()
        inputStream.close()
    } catch(err: IOException) {
        println("Unable to read the file: $filepath")
    }
}

// Запись массива чисел с плавающей запятой в новую строку таблицы
fun writeTableDouble(filepath: String, row: Int, stats: ArrayList<Double>) {
    try {
        val inputStream = FileInputStream(filepath)
        val xlWb = WorkbookFactory.create(inputStream)

        val xlWs = xlWb.getSheetAt(0)
        val xlRow = xlWs.createRow(row)

        stats.forEachIndexed { index, item ->
            val xlCol = xlRow.createCell(index)
            xlCol.setCellValue(item)
        }
        val outputStream = FileOutputStream(filepath)
        xlWb.write(outputStream)
        xlWb.close()
        inputStream.close()
    } catch(err: IOException) {
        println("Unable to read the file: $filepath")
    }
}
