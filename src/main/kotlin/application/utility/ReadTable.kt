package application.utility

import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream
import java.io.IOException

// Чтение из таблицы в двумерный массив чисел с плавающей запятой
fun readTableDouble(filepath: String, numOfRows: Int, numOfCols: Int, array: ArrayList<ArrayList<Double>>) {
    try {
        val inputStream = FileInputStream(filepath)
        val xlWb = WorkbookFactory.create(inputStream)
        val table = xlWb.getSheetAt(0)

        for (rowNumber in 1..numOfRows) {
            array.add(arrayListOf())
            for (colNumber in 1..numOfCols) {
                array[rowNumber - 1].add(table.getRow(rowNumber).getCell(colNumber).numericCellValue)
            }
        }
        inputStream.close()
    } catch(err: IOException) {
        println("Unable to read the file")
    }
}

// Чтение из таблицы в двумерный массив целых чисел
fun readTableInt(filepath: String, numOfRows: Int, numOfCols: Int, array: ArrayList<ArrayList<Int>>) {
    try {
        val inputStream = FileInputStream(filepath)
        val xlWb = WorkbookFactory.create(inputStream)

        val table = xlWb.getSheetAt(0)

        for (rowNumber in 1..numOfRows) {
            array.add(arrayListOf())
            for (colNumber in 1..numOfCols) {
                array[rowNumber - 1].add(table.getRow(rowNumber).getCell(colNumber).numericCellValue.toInt())
            }
        }
        inputStream.close()
    } catch(err: IOException) {
        println("Unable to read the file")
    }
}

fun readTableInt2(filepath: String, numOfRows: Int, array: ArrayList<ArrayList<Int>>) {
    try {
        val inputStream = FileInputStream(filepath)
        val xlWb = WorkbookFactory.create(inputStream)

        val table = xlWb.getSheetAt(0)

        for (rowNumber in 0 until numOfRows) {
            array.add(arrayListOf())
            var k = 0
            while (table.getRow(rowNumber).getCell(k) != null) {
                array[rowNumber].add(table.getRow(rowNumber).getCell(k).numericCellValue.toInt())
                k++
            }
        }
        inputStream.close()
    } catch(err: IOException) {
        println("Unable to read the file")
    }
}
