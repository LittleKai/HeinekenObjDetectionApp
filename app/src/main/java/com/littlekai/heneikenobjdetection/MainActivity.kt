package com.littlekai.heneikenobjdetection

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.ui.AppBarConfiguration
import androidx.viewpager.widget.ViewPager
import com.littlekai.heneikenobjdetection.adapter.ViewPagerAdapter
import com.littlekai.heneikenobjdetection.dao.HenikenObjDetectionApplication
import com.littlekai.heneikenobjdetection.database.HeneikenDatabaseHelper
import com.littlekai.heneikenobjdetection.databinding.ActivityMainBinding
import com.littlekai.heneikenobjdetection.fragments.ObjDetectFragment
import com.littlekai.heneikenobjdetection.fragments.SettingFragment
import com.luseen.spacenavigation.SpaceItem
import com.luseen.spacenavigation.SpaceNavigationView
import com.luseen.spacenavigation.SpaceOnClickListener
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var objDetectFragment: ObjDetectFragment
    private lateinit var settingFragment: SettingFragment
    lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivityTask"
    var spaceNavigationView: SpaceNavigationView? = null
    var viewPager: ViewPager? = null
    private val REQUEST_CAPTURE_IMAGE = 110
    private val REQUEST_DISPATCH_GALLERY = 111
    var picUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = findViewById<View>(R.id.viewpager) as ViewPager
        spaceNavigationView = findViewById<View>(R.id.space) as SpaceNavigationView
        spaceNavigationView!!.initWithSaveInstanceState(savedInstanceState)
        setupViewPager(viewPager!!)

        val helper = HeneikenDatabaseHelper(this)
        val db = helper.writableDatabase // This will trigger onCreate if needed

        checkAndRequestPermissions(this)
    }

    private fun checkAndRequestPermissions(context: Context): Boolean {
        val cameraPermissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val readStoragePermissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val permissionsNeeded: MutableList<String> = ArrayList()
        if (!cameraPermissionGranted) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (!readStoragePermissionGranted) {
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissionsNeeded.add(Manifest.permission.ACCESS_MEDIA_LOCATION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray<String>(),
                123
            )
            return false
        }
        return true
    }

    private fun setupViewPager(viewPager: ViewPager) {
        spaceNavigationView!!.addSpaceItem(SpaceItem("DETECT", R.drawable.icon_home))
        spaceNavigationView!!.addSpaceItem(SpaceItem("SETTINGS", R.drawable.ic_main_settings))
        //Initializing the spaceNavigationView
        spaceNavigationView!!.setSpaceOnClickListener(object : SpaceOnClickListener {
            override fun onCentreButtonClick() {
                selectImage()
            }

            override fun onItemClick(itemIndex: Int, itemName: String) {
                when (itemIndex) {
                    0 -> viewPager.currentItem = 0
                    1 -> viewPager.currentItem = 1
                }
            }

            override fun onItemReselected(itemIndex: Int, itemName: String) {
            }
        })
        val adapter = ViewPagerAdapter(supportFragmentManager)
        objDetectFragment = ObjDetectFragment()
        settingFragment = SettingFragment()
        adapter.addFragments(objDetectFragment)
        adapter.addFragments(settingFragment)
        viewPager.adapter = adapter
        Log.d(TAG, "sensorOrientation: " + getScreenOrientation())
    }

    private val pictureImagePath = ""

    var CAMERA_PERMISSION_CODE: Int = 101
    var STORAGE_PERMISSION_CODE: Int = 201
    var REQUEST_READ_STORAGE_PERMISSION: Int = 202
    var REQUEST_WRITE_STORAGE_PERMISSION: Int = 203
    private val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )


    private fun checkPermission(permissionCode: Int): Boolean {
        when (permissionCode) {
            CAMERA_PERMISSION_CODE -> {
                return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            }

            REQUEST_READ_STORAGE_PERMISSION -> {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                } else {
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                }
            }

            else -> {
                return false
            }
        }
    }


    protected fun getScreenOrientation(): Int {
        return when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_270 -> 270
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_90 -> 90
            else -> 0
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        spaceNavigationView!!.onSaveInstanceState(outState)
        outState.putParcelable("pic_uri", picUri)
    }

    private fun selectImage() {
        val options = arrayOf<CharSequence>(
            "Locations List",
//            "Take Photo",
            "Choose from Gallery",
            "Cancel"
        )
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Choose Option!")
        builder.setItems(options) { dialog, item ->
            if (options[item] == "Take Photo") {
                openCameraIntent()
            } else if (options[item] == "Choose from Gallery") {
                if (checkPermission(REQUEST_READ_STORAGE_PERMISSION)) dispatchGalleryIntent()
            } else if (options[item] == "Locations List") {
                val intent = Intent(this, LocationListActivity::class.java)
                startActivity(intent)
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private val TAKE_PICTURE = 1
    private val mImageUri: Uri? = null
    private val PICK_FROM_CAMERA = 1

    private fun openCameraIntent() {
        Toast.makeText(this, "openCameraIntent", Toast.LENGTH_SHORT).show()

        val pictureIntent = Intent(
            MediaStore.ACTION_IMAGE_CAPTURE
        )
        if (pictureIntent.resolveActivity(packageManager) != null) {
            //Create a file to store the image
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "com.littlekai.heneikenobjdetection.provider",
                    photoFile
                )
                pictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    photoURI
                )
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val values = ContentValues()
                values.put(MediaStore.Images.Media.TITLE, "New Picture")
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
                imageUri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(intent, REQUEST_CAPTURE_IMAGE)
                //                startActivityForResult(cameraIntent, REQUEST_CAPTURE_IMAGE);
            }
        }
    }

    var imageUri: Uri? = null

    private fun dispatchGalleryIntent() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(pickPhoto, REQUEST_DISPATCH_GALLERY)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val bitmap = arrayOfNulls<Bitmap>(1)
        if (resultCode == RESULT_OK) {
            if (requestCode == 901) {
                //Task after next activity is finished
//                val result: Food = data.getParcelableExtra("result")
//                val returnIntent = Intent()
//                returnIntent.putExtra("result", result)
//                setResult(RESULT_OK, returnIntent)
//                finish()
            }

            if (requestCode == REQUEST_CAPTURE_IMAGE) {

                try {
                    bitmap[0] = MediaStore.Images.Media.getBitmap(
                        contentResolver, imageUri
                    )
                    //                        imageurl = getRealPathFromURI(imageUri);
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (requestCode == REQUEST_DISPATCH_GALLERY) {
                val selectedImage = data?.data

                // method 1
                try {
                    bitmap[0] = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            if (bitmap[0] != null) {
                Log.d(TAG, bitmap[0]!!.height.toString() + "," + bitmap[0]!!.width)

                val intent = Intent(this, ObjDetectActivity::class.java)
                //                intent.putExtra("image", bitmap[0]);
                val henikenObjDetectionApplication: HenikenObjDetectionApplication =
                    applicationContext as HenikenObjDetectionApplication
                henikenObjDetectionApplication.setCapturedBm(bitmap[0])
                startActivity(intent)


                //                    getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        }
    }

    var imageFilePath: String? = null

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp =
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        imageFilePath = image.absolutePath
        return image
    }
}