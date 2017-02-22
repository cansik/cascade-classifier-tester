package ch.fhnw.afpars.model

import ch.bildspur.cctester.copy
import ch.bildspur.cctester.editor.shapes.BaseShape
import org.opencv.core.Mat

/**
 * Created by cansik on 06.10.16.
 */
class AFImage : Cloneable {
    var name: String
    var image: Mat
    val attributes: MutableMap<String, Mat>
    val layers: MutableMap<String, MutableList<BaseShape>>


    constructor(image: Mat,
                name: String = "AFImage",
                attributes: MutableMap<String, Mat> = mutableMapOf(),
                layers: MutableMap<String, MutableList<BaseShape>> = mutableMapOf()) {
        this.image = image
        this.name = name
        this.attributes = attributes
        this.layers = layers
    }

    fun addLayer(name: String, vararg shapes: BaseShape = arrayOf<BaseShape>()) {
        val shapeList = mutableListOf<BaseShape>()
        shapeList.addAll(shapes)
        layers.put(name, shapeList)
    }

    public override fun clone(): AFImage {
        val img = AFImage(image.copy(), name, this.attributes, this.layers)
        return img
    }

    fun clear(){
        image.release()
        attributes.clear()
        System.gc()
    }
}