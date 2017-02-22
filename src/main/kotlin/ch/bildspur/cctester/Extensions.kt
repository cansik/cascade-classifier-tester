package ch.bildspur.cctester

import javafx.scene.image.Image
import javafx.scene.image.WritablePixelFormat
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayInputStream

/**
 * Created by cansik on 22.02.17.
 */

fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)

fun Mat.toImage(): Image {
    val byteMat = MatOfByte()
    Imgcodecs.imencode(".bmp", this, byteMat)
    return Image(ByteArrayInputStream(byteMat.toArray()))
}

fun Mat.zeros(): Mat {
    return this.zeros(this.type())
}

fun Mat.zeros(type: Int): Mat {
    return Mat.zeros(this.rows(), this.cols(), type)
}

fun Mat.copy(): Mat {
    val m = this.zeros()
    this.copyTo(m)
    return m
}

fun Mat.resize(width: Int, height: Int): Mat {
    assert(width > 0 || height > 0)

    var w = width
    var h = height

    if (width == 0) {
        w = ((height.toDouble() / this.height()) * this.width()).toInt()
    }

    if (height == 0) {
        h = ((width.toDouble() / this.width()) * this.height()).toInt()
    }

    val result = Mat.zeros(h, w, this.type())
    Imgproc.resize(this, result, result.size())
    return result
}

fun Image.toMat(): Mat {
    val width = this.width.toInt()
    val height = this.height.toInt()
    val buffer = ByteArray(width * height * 4)

    val reader = this.pixelReader
    val format = WritablePixelFormat.getByteBgraInstance()
    reader.getPixels(0, 0, width, height, format, buffer, 0, width * 4)

    val mat = Mat(height, width, CvType.CV_8UC4)
    mat.put(0, 0, buffer)
    return mat
}