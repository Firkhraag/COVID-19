import application.app.MyApp

// Исполняемый файл
class MyApplicationToRun {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MyApp().main(arrayOf())
        }
    }
}
