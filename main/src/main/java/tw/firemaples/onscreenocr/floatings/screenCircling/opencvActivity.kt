package tw.firemaples.onscreenocr.floatings.screenCircling

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.screenshot.ScreenExtractor


class opencvActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opencv)



        var imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageBitmap(makeGray(ScreenExtractor.getbitmap))
    }

    fun makeGray(bitmap: Bitmap) : Bitmap {

        // Create OpenCV mat object and copy content from bitmap
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // Convert to grayscale
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY)
        Imgproc.cvtColor(mat, mat, Imgproc.ADAPTIVE_THRESH_MEAN_C)
        // Make a mutable bitmap to copy grayscale image
        val grayBitmap = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(mat, grayBitmap)

        return grayBitmap
    }
}