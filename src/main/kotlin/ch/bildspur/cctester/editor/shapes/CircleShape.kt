package ch.bildspur.cctester.editor.shapes

import ch.bildspur.cctester.format
import ch.bildspur.cctester.editor.shapes.OvalShape
import javafx.geometry.Dimension2D
import javafx.geometry.Point2D

/**
 * Created by cansik on 29.01.17.
 */
class CircleShape(val center: Point2D, val radius: Double) : OvalShape(center, Dimension2D(radius * 2.0, radius * 2.0)) {
    override fun toString(): String {
        return "Circle (${center.x.format(1)} | ${center.y.format(1)}, r: $radius)"
    }
}