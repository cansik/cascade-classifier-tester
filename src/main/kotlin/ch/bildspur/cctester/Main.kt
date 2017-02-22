package ch.bildspur.cctester

import ch.bildspur.cctester.controller.MainView
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.opencv.core.Core


class Main : Application() {

    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

        val loader = FXMLLoader(javaClass.classLoader.getResource("view/MainView.fxml"))
        val root = loader.load<Any>() as Parent
        val controller = loader.getController<Any>() as MainView


        primaryStage.title = "Cascade Classifier Tester"
        primaryStage.scene = Scene(root)


        primaryStage.setOnShown { controller.setupView() }
        primaryStage.setOnCloseRequest {
            Platform.exit()
            System.exit(0)
        }

        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }
}