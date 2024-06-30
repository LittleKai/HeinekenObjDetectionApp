package com.littlekai.heneikenobjdetection

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Html
import android.util.DisplayMetrics
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.littlekai.heneikenobjdetection.dao.HenikenObjDetectionApplication
import com.littlekai.heneikenobjdetection.databinding.ActivityObjDetectBinding
import com.littlekai.heneikenobjdetection.helper.ObjectDetectorHelper
import com.littlekai.heneikenobjdetection.utils.AddLocationDialog
import com.littlekai.heneikenobjdetection.utils.DetectResultHelper
import com.littlekai.heneikenobjdetection.utils.RecognitionDialog
import com.littlekai.heneikenobjdetection.utils.UtilHelper
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.util.concurrent.ExecutorService


class ObjDetectActivity : AppCompatActivity(), ObjectDetectorHelper.DetectorListener {

    private lateinit var binding: ActivityObjDetectBinding
    private var TAG = "ObjDetectActivity"
    private var objectDetector: ObjectDetector? = null
    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var bitmapBuffer: Bitmap
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    var inputBitmap: Bitmap? = null
    var detectBitmap: Bitmap? = null
    var detectContext = ""

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var inputImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityObjDetectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inputImageView = binding.inputImageView;

        objectDetectorHelper = ObjectDetectorHelper(
            context = this,
            objectDetectorListener = this, maxResults = 50, currentModel = 0, threshold = 0.4f
        )

        var henikenObjDetectionApplication: HenikenObjDetectionApplication =
            this.getApplication() as HenikenObjDetectionApplication

        inputBitmap = henikenObjDetectionApplication.capturedBm

        if (inputBitmap != null) {
            runObjectDetection(inputBitmap!!)
        }

        initBottomSheetControls()

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val dialog = RecognitionDialog(this)
            dialog.show(recognizedObjects)
        }
        val addLocationButton = findViewById<FloatingActionButton>(R.id.add_location_button)
        addLocationButton.setOnClickListener { // Show the Add Location Dialog
            AddLocationDialog(this).showDialog()
        }
    }

    private val recognizedObjects = mutableListOf(
        "logo_tiger", "logo_heineken", "beer_bottle_tiger", "beer_bottle_heineken",
        "face", "human", "drinker", "logo", "beer_bottle", "beer_can",
        "beer_carton", "PG", "poster", "display_stand"
    )

    private fun runObjectDetection(bitmap: Bitmap) {

        runOnUiThread {
            detectBitmap = copyBitmap(bitmap)
            objectDetectorHelper.detect(detectBitmap!!, 0)

        }
    }

    private fun initBottomSheetControls() {
        binding.bottomSheetLayout.thresholdValue.setText(objectDetectorHelper.threshold.toString())
        // When clicked, lower detection score threshold floor
        binding.bottomSheetLayout.thresholdMinus.setOnClickListener {
            if (objectDetectorHelper.threshold >= 0.1) {
                objectDetectorHelper.threshold -= 0.1f
                updateControlsUi()
            }
        }

        // When clicked, raise detection score threshold floor
        binding.bottomSheetLayout.thresholdPlus.setOnClickListener {
            if (objectDetectorHelper.threshold <= 0.8) {
                objectDetectorHelper.threshold += 0.1f
                updateControlsUi()
            }
        }
        binding.bottomSheetLayout.maxResultsValue.setText(objectDetectorHelper.maxResults.toString())
        // When clicked, reduce the number of objects that can be detected at a time
        binding.bottomSheetLayout.maxResultsMinus.setOnClickListener {
            if (objectDetectorHelper.maxResults > 3) {
                objectDetectorHelper.maxResults = objectDetectorHelper.maxResults - 3
                updateControlsUi()
            }
        }

        // When clicked, increase the number of objects that can be detected at a time
        binding.bottomSheetLayout.maxResultsPlus.setOnClickListener {
            if (objectDetectorHelper.maxResults < 100) {
                objectDetectorHelper.maxResults = objectDetectorHelper.maxResults + 3
                updateControlsUi()
            }
        }

        binding.bottomSheetLayout.threadsValue.setText(objectDetectorHelper.numThreads.toString())
        // When clicked, decrease the number of threads used for detection
        binding.bottomSheetLayout.threadsMinus.setOnClickListener {
            if (objectDetectorHelper.numThreads > 1) {
                objectDetectorHelper.numThreads--
                updateControlsUi()
            }
        }

        // When clicked, increase the number of threads used for detection
        binding.bottomSheetLayout.threadsPlus.setOnClickListener {
            if (objectDetectorHelper.numThreads < 4) {
                objectDetectorHelper.numThreads++
                updateControlsUi()
            }
        }

        // When clicked, change the underlying hardware used for inference. Current options are CPU
        // GPU, and NNAPI
        binding.bottomSheetLayout.spinnerDelegate.setSelection(
            objectDetectorHelper.currentDelegate,
            false
        )
        binding.bottomSheetLayout.spinnerDelegate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    objectDetectorHelper.currentDelegate = p2
                    updateControlsUi()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /* no op */
                }
            }
        // When clicked, change the underlying model used for object detection
        binding.bottomSheetLayout.spinnerModel.setSelection(
            objectDetectorHelper.currentModel, false
        )
        binding.bottomSheetLayout.spinnerModel.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    objectDetectorHelper.currentModel = p2
                    updateControlsUi()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /* no op */
                }
            }
    }

    fun copyBitmap(inputBitmap: Bitmap): Bitmap {
        return inputBitmap.copy(inputBitmap.config, true)
    }

    private fun updateControlsUi() {
        binding.bottomSheetLayout.maxResultsValue.text =
            objectDetectorHelper.maxResults.toString()
        binding.bottomSheetLayout.thresholdValue.text =
            String.format("%.2f", objectDetectorHelper.threshold)
        binding.bottomSheetLayout.threadsValue.text =
            objectDetectorHelper.numThreads.toString()

        // Needs to be cleared instead of reinitialized because the GPU
        // delegate needs to be initialized on the thread using it when applicable
        objectDetectorHelper.clearObjectDetector()

        runObjectDetection(inputBitmap!!)

    }

    override fun onError(error: String) {
        this.runOnUiThread {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResults(
        results: MutableList<Detection>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int
    ) {
        this.runOnUiThread {

            binding.bottomSheetLayout.inferenceTimeVal.text =
                String.format("%d ms", inferenceTime)

            detectContext = DetectResultHelper.detectContextResults1(results)
//            binding.imgContextTv.setText(detectContext)
            binding.imgContextTv.setText(
                Html.fromHtml(detectContext),
                TextView.BufferType.SPANNABLE
            )

            val filterResults = DetectResultHelper.preferenceLabelFilteredResults(results)
            drawResultsDetection(detectBitmap!!, filterResults)

        }
    }

    val finalMinimumConfidence = 0.3f
    val BOUNDING_RECT_TEXT_PADDING = 4

    fun drawResultsDetection(bitmap: Bitmap, results: MutableList<Detection>) {
        val mutableBitmap: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(mutableBitmap)
        val boxPaint = Paint()
        boxPaint.color = ContextCompat.getColor(this, R.color.bounding_box_color)
        boxPaint.style = Paint.Style.STROKE

        var textBackgroundPaint = Paint()
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        val textPaint = Paint()
        textPaint.color = Color.WHITE
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)
        if (sharedPreferences.getBoolean("resize_input", true)) {
            var resizeRatio =
                sharedPreferences.getString("crop_size", "640")!!.toFloat() / 640
//            if (resizeRatio < 1f)
//                resizeRatio = 0.3f
//            Toast.makeText(this, "Resize ratio: $resizeRatio", Toast.LENGTH_SHORT).show()
            boxPaint.strokeWidth = 5.0f * resizeRatio
            textPaint.strokeWidth = 5.0f* resizeRatio
            textBackgroundPaint.textSize = 20f * resizeRatio
            textPaint.textSize = 20f * resizeRatio
        } else {
            boxPaint.strokeWidth = 5.0f
            textPaint.strokeWidth = 5.0f
            textBackgroundPaint.textSize = 30f
            textPaint.textSize = 30f
        }
        var bounds = Rect()

        if (results.isEmpty())
            binding.inputImageView.setImageBitmap(fixInputBitmap(detectBitmap!!))
        else {
            val labels = DetectResultHelper.getLabelBrand(results)

            for (label in labels) {
                val boundingBox: RectF = label.boundingBox
                val left = boundingBox.left
                val top = boundingBox.top
//                if (boundingBox != null && result.categories[0].score >= finalMinimumConfidence) {
                if (boundingBox != null) {

                    if (label.name != null) {
                        val drawableText = label.name + " " +
                                String.format("%.2f", label.score)

                        textBackgroundPaint.getTextBounds(
                            drawableText,
                            0,
                            drawableText.length,
                            bounds
                        )
                        val textWidth = bounds.width()
                        val textHeight = bounds.height()
                        canvas.drawRect(
                            left,
                            top,
                            left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                            top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                            textBackgroundPaint
                        )
                        canvas.drawText(
                            drawableText,
                            left,
                            top + bounds.height(),
                            textPaint
                        )
                    }


                    canvas.drawRect(UtilHelper.fixLocation(boundingBox, mutableBitmap), boxPaint)
                }
                binding.inputImageView.setImageBitmap(fixInputBitmap(mutableBitmap))
//            Glide.with(this)
//                .load("https://maps.googleapis.com/maps/api/staticmap?center=37.4219983,-122.084&zoom=14&size=400x400&key=" +
//                        "AIzaSyAXqMvWpBJh3V5w52p7uHPZhVdW6y05FbQ")
//                .placeholder(R.drawable.placeholder) // Hình ảnh chờ nếu cần
//                .into(binding.inputImageView);
            }
        }


    }


    fun fixInputBitmap(bitmap: Bitmap): Bitmap {
        val display = windowManager.defaultDisplay
        val rotation = display.rotation

        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val isPortrait = metrics.heightPixels > metrics.widthPixels

        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        var shouldRotate = false
        var degrees = 0f
        if (isPortrait && bitmapWidth > bitmapHeight) {
            shouldRotate = true
            degrees = 90f
        } else if (!isPortrait && bitmapWidth < bitmapHeight) {
            shouldRotate = true
            degrees = -90f

        }

        return if (shouldRotate) {
            val matrix = Matrix()
            matrix.postRotate(degrees)
            scaleBitmapToScreenWidth(
                Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmapWidth,
                    bitmapHeight,
                    matrix,
                    true
                )
            )
        } else {
            scaleBitmapToScreenWidth(bitmap)
        }
    }

    private fun scaleBitmapToScreenWidth(bitmap: Bitmap): Bitmap {
        // Lấy kích thước của màn hình
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        // Lấy chiều rộng và chiều cao của bitmap
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        // Tính tỷ lệ scale
        val scale = screenWidth.toFloat() / bitmapWidth

        // Tạo matrix để scale bitmap
        val matrix = Matrix()
        matrix.postScale(scale, scale)

        // Tạo bitmap mới với kích thước đã scale
        return Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true)
    }


}
