import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.littlekai.heneikenobjdetection.helper.ObjectDetectorHelper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
//import org.junit.jupiter.api.Assertions.assertEquals // Đảm bảo rằng import này đúng

//import org.junit.Assert.assertEquals
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.vision.detector.Detection
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import java.io.InputStream
import org.junit.jupiter.api.Assertions.assertEquals // Import cho JUnit 5


@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context của app được sử dụng để kiểm tra
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.littlekai.heneikenobjdetection", appContext.packageName)
    }

//class ExampleInstrumentedTest {
//    val controlResults = listOf<Detection>(
//        Detection.create(
//            RectF(69.0f, 58.0f, 227.0f, 171.0f),
//            listOf<Category>(Category.create("cat", "cat", 0.77734375f))),
//        Detection.create(
//            RectF(13.0f, 6.0f, 283.0f, 215.0f),
//            listOf<Category>(Category.create("couch", "couch", 0.5859375f))),
//        Detection.create(
//            RectF(45.0f, 27.0f, 257.0f, 184.0f),
//            listOf<Category>(Category.create("chair", "chair", 0.55078125f)))
//    )
//
//    @Test
//    @Throws(Exception::class)
//    fun detectionResultsShouldNotChange() {
//        val objectDetectorHelper =
//            ObjectDetectorHelper(
//                context = InstrumentationRegistry.getInstrumentation().context,
//                objectDetectorListener =
//                object : ObjectDetectorHelper.DetectorListener {
//                    override fun onError(error: String) {
//                        // no op
//                    }
//
//                    override fun onResults(
//                        results: MutableList<Detection>?,
//                        inferenceTime: Long,
//                        imageHeight: Int,
//                        imageWidth: Int
//                    ) {
//
//                        assertEquals(controlResults.size, results!!.size)
//
//                        // Loop through the detected and control data
//                        for (i in controlResults.indices) {
//                            // Verify that the bounding boxes are the same
//                            assertEquals(results[i].boundingBox, controlResults[i].boundingBox)
//
//                            // Verify that the detected data and control
//                            // data have the same number of categories
//                            assertEquals(
//                                results[i].categories.size,
//                                controlResults[i].categories.size
//                            )
//
//                            // Loop through the categories
//                            for (j in 0 until controlResults[i].categories.size - 1) {
//                                // Verify that the labels are consistent
//                                assertEquals(
//                                    results[i].categories[j].label,
//                                    controlResults[i].categories[j].label
//                                )
//                            }
//                        }
//                    }
//                }
//            )
//        // Create Bitmap and convert to TensorImage
//        val bitmap = loadImage("cat1.png")
//        // Run the object detector on the sample image
//        objectDetectorHelper.detect(bitmap!!, 0)
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun detectedImageIsScaledWithinModelDimens() {
//        val objectDetectorHelper =
//            ObjectDetectorHelper(
//                context = InstrumentationRegistry.getInstrumentation().context,
//                objectDetectorListener =
//                object : ObjectDetectorHelper.DetectorListener {
//                    override fun onError(error: String) {}
//
//                    override fun onResults(
//                        results: MutableList<Detection>?,
//                        inferenceTime: Long,
//                        imageHeight: Int,
//                        imageWidth: Int
//                    ) {
//                        Assert.assertNotNull(results)
//                        for (result in results!!) {
//                            Assert.assertTrue(result.boundingBox.top <= imageHeight)
//                            Assert.assertTrue(result.boundingBox.bottom <= imageHeight)
//                            Assert.assertTrue(result.boundingBox.left <= imageWidth)
//                            Assert.assertTrue(result.boundingBox.right <= imageWidth)
//                        }
//                    }
//                }
//            )
//
//        // Create Bitmap and convert to TensorImage
//        val bitmap = loadImage("cat1.png")
//        // Run the object detector on the sample image
//        objectDetectorHelper.detect(bitmap!!, 0)
//    }
//
//    @Throws(Exception::class)
//    private fun loadImage(fileName: String): Bitmap? {
//        val assetManager: AssetManager =
//            InstrumentationRegistry.getInstrumentation().context.assets
//        val inputStream: InputStream = assetManager.open(fileName)
//        return BitmapFactory.decodeStream(inputStream)
//    }

}