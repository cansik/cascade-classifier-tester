package ch.bildspur.cctester.controller

import ch.bildspur.cctester.*
import ch.bildspur.cctester.editor.ImageEditor
import ch.bildspur.cctester.reader.AFImageReader
import ch.fhnw.afpars.model.AFImage
import ch.bildspur.cctester.ui.RelationNumberField
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.embed.swing.SwingFXUtils
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.image.ImageView
import javafx.scene.input.Clipboard
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.Callback
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.concurrent.thread

/**
 * Created by cansik on 22.02.17.
 */
class MainView {

    companion object
    {
        @JvmStatic
        val MAX_TEXTURE_SIZE = 2048
    }

    @FXML
    lateinit var editor : ImageEditor

    @FXML
    lateinit var scaleFactor : RelationNumberField

    @FXML
    lateinit var minNeighbors : RelationNumberField

    @FXML
    lateinit var ccFileLabel : Label

    @FXML
    lateinit var loadImageButton : Button

    @FXML
    lateinit var statusLabel : Label

    @FXML
    lateinit var loadFromClipButton : Button

    @FXML
    lateinit var imageLabel : Label

    @FXML
    lateinit var runDetectionButton : Button

    @FXML
    lateinit var areaListView: ListView<AFImage>

    val image = SimpleObjectProperty<AFImage>()

    var areaImages = FXCollections.observableArrayList<AFImage>()

    var cascadeFile = ""

    var currentImage : AFImage? = null

    fun setupView()
    {
        image.addListener {
            o ->
            editor.displayImage(image.value.image.toImage())
            editor.redraw()
        }

        scaleFactor.setValue(1.1)
        minNeighbors.setValue(3.0)

        scaleFactor.valueProperty().addListener { o ->  runDetection()}

        minNeighbors.valueProperty().addListener { o ->  runDetection()}

        areaListView.items = areaImages
        areaListView.cellFactory = Callback { listView ->
            object : ListCell<AFImage>() {
                override fun updateItem(item: AFImage?, empty: Boolean) {
                    if (item != null) {
                        super.updateItem(item, empty)

                        if (empty) {
                            graphic = null
                        } else {
                            // true makes this load in background
                            val imageView = ImageView(item.image.toImage())
                            imageView.preserveRatioProperty().set(true)
                            imageView.fitWidth = areaListView.width * 0.75

                            val label = Label(item.name)
                            graphic = VBox(label, imageView)
                        }
                    }
                }
            }
        }
        areaListView.selectionModel.selectedItemProperty().addListener { observable, oldValue, newValue ->
            if (newValue != null) {
                editor.resetZoom()
                editor.displayImage(newValue.image.toImage())

                currentImage = newValue
            }
        }

        loadFromClipButton.isDisable = true
        loadImageButton.isDisable = true
        runDetectionButton.isDisable = true
    }

    fun loadCascadeFile(e : ActionEvent)
    {
        val stage = (e.source as Node).scene.window as Stage

        val fileChooser = FileChooser()
        fileChooser.title = "Select cascade file to process"
        fileChooser.initialFileName = ""
        fileChooser.extensionFilters.addAll(
                FileChooser.ExtensionFilter("Xml", "*.xml", "*.XML", "*.Xml")
        )

        val result = fileChooser.showOpenDialog(stage)

        if (result != null) {
            // save cascade file
            cascadeFile = result.absolutePath.toString()

            ccFileLabel.text = result.name
            loadFromClipButton.isDisable = false
            loadImageButton.isDisable = false
        }
    }

    fun loadImageFromClipBoard(e: ActionEvent) {
        val cb = Clipboard.getSystemClipboard()
        if (cb.hasImage()) {
            val afImg = AFImage(cb.image.toMat())
            imageLabel.text = "Clipboard (${afImg.image.width()}|${afImg.image.height()})"
            loadImage(afImg)
        }
    }

    fun loadImage(e: ActionEvent) {
        val stage = (e.source as Node).scene.window as Stage

        val fileChooser = FileChooser()
        fileChooser.title = "Select image to process"
        fileChooser.initialFileName = ""
        fileChooser.extensionFilters.addAll(
                FileChooser.ExtensionFilter("All Images", "*.jpg", "*.jpeg", "*.png"),
                FileChooser.ExtensionFilter("JPG", "*.jpg", "*.jpeg"),
                FileChooser.ExtensionFilter("PNG", "*.png")
        )

        val result = fileChooser.showOpenDialog(stage)

        if (result != null) {
            val img = AFImageReader().read(result.toPath())
            imageLabel.text = "${result.name} (${img.image.width()}|${img.image.height()})"
            loadImage(img)
        }
    }

    fun loadImage(afImg : AFImage)
    {
        // resize if needed
        if(afImg.image.width() > MAX_TEXTURE_SIZE || afImg.image.height() > MAX_TEXTURE_SIZE) {
            var nimg = Mat()

            if (afImg.image.width() > afImg.image.height())
                nimg = afImg.image.resize(MAX_TEXTURE_SIZE, 0)
            else
                nimg = afImg.image.resize(0, MAX_TEXTURE_SIZE)

            afImg.image.release()
            afImg.image = nimg
        }

        image.set(afImg)

        runDetection()
    }

    fun runDetection()
    {
        statusLabel.text = "detecting..."
        runDetectionButton.isDisable = false
        editor.cursor = Cursor.WAIT

        thread {
            val cascadeDetector = CascadeClassifier(cascadeFile)

            val areas = MatOfRect()
            val result = image.value.image.copy()

            cascadeDetector.detectMultiScale(image.value.image, areas, scaleFactor.getValue(), minNeighbors.getValue().toInt(), 0, Size(0.0, 0.0), Size(0.0, 0.0))

            val areaAFImages = areas.toArray().mapIndexed { i, rect ->  AFImage(Mat(result, rect).copy(), "Area $i")}

            Platform.runLater {
                areaImages.clear()
                areaListView.items.clear()

                // create small images from areas
                areaImages.addAll(areaAFImages)
            }

            for (rect in areas.toArray()) {
                Imgproc.rectangle(result, Point(rect.x.toDouble(), rect.y.toDouble()),
                        Point(rect.x + rect.width.toDouble(), rect.y + rect.height.toDouble()), Scalar(0.0, 255.0, 0.0), 2)
            }

            areaImages.add(0, AFImage(result, "Original"))

            currentImage = AFImage(result, "Original")

            Platform.runLater {
                editor.displayImage(result.toImage())
                editor.redraw()
                editor.cursor = editor.activeTool.cursor
                statusLabel.text = "Objects: ${areas.toArray().size}"
            }
        }
    }

    fun saveButtonClicked(e : ActionEvent)
    {
        if(currentImage == null)
            return

        val stage = (e.source as Node).scene.window as Stage

        val fileChooser = FileChooser()
        fileChooser.initialFileName = "${currentImage!!.name}.png"
        fileChooser.title = "Save image as png"
        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("Portable Network Graphics", "*.png"))

        val file = fileChooser.showSaveDialog(stage)

        if (file != null) {
            val outputImage = currentImage!!.image.toImage()

            try {
                ImageIO.write(SwingFXUtils.fromFXImage(outputImage, null), "png", file)
            } catch (e: IOException) {
                println("Error: $e")
            }
        }
    }
}