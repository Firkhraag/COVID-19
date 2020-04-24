package application.app

import application.view.MainView
import javafx.scene.image.Image
import javafx.stage.Stage
import tornadofx.*

// Настройка сцены
class MyApp : App(MainView::class) {

    override fun start(stage: Stage) {
        super.start(stage)
        stage.icons.add(Image(
            "file:D:\\Dev\\Projects\\KotlinProjects\\TornadoFX\\src\\main\\resources\\icon\\virus.png"))
        stage.isResizable = false
    }

    fun main(args: Array<String>) {
        importStylesheet("/css/style.css")
        launch<MyApp>(args)
    }
}