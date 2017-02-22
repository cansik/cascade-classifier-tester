package ch.bildspur.cctester.editor.tools

import ch.fhnw.afpars.model.AFImage
import ch.bildspur.cctester.editor.ImageEditor
import ch.bildspur.cctester.editor.tools.BaseEditorTool
import ch.bildspur.cctester.editor.shapes.BaseShape
import ch.bildspur.cctester.event.Event
import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.scene.input.ScrollEvent

/**
 * Created by cansik on 25.01.17.
 */
class ViewTool : BaseEditorTool() {
    val scaleSpeed = 1.0 / 50.0

    var dragStart = Point2D.ZERO!!

    val shapesSelected = Event<List<BaseShape>>()

    override val cursor: Cursor
        get() = Cursor.OPEN_HAND

    override fun onEditorMousePressed(imageEditor: ImageEditor, event: MouseEvent) {
        if (event.clickCount == 2) {
            imageEditor.resetZoom()
            return
        }

        dragStart = Point2D(event.x, event.y)
    }

    override fun onEditorMouseDragged(imageEditor: ImageEditor, event: MouseEvent) {
        // drag
        val point = Point2D(event.x, event.y)
        val delta = dragStart.subtract(point)

        imageEditor.canvasTransformation = delta.multiply(-1.0)
        dragStart = point

        imageEditor.resize()
    }

    override fun onCanvasMouseClicked(imageEditor: ImageEditor, event: MouseEvent) {
        // check if items selected
        val point = Point2D(event.x, event.y)
        val shapes = imageEditor.layers.flatMap { it.shapes.filter { it.contains(point) } }

        if(shapes.isNotEmpty())
            shapesSelected(shapes)
    }

    override fun onEditorScroll(imageEditor: ImageEditor, event: ScrollEvent) {
        // zoom point
        imageEditor.zoomTransformation = Point2D(event.x, event.y)

        // scale
        imageEditor.zoomScale += -1 * event.deltaY * scaleSpeed
        imageEditor.zoomScale = Math.min(Math.max(imageEditor.minimumZoom, imageEditor.zoomScale), imageEditor.maximumZoom)
        imageEditor.resize()
    }
}