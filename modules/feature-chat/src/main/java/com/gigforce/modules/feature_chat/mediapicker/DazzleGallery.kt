package com.gigforce.modules.feature_chat.mediapicker

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import com.gigforce.modules.feature_chat.R
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.util.Log
import android.view.View
import android.view.View.*
import android.view.Window
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.gigforce.common_image_picker.CameraAndGalleryIntegrator
import com.gigforce.common_image_picker.ImageCropOptions
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
import com.gigforce.common_ui.chat.ChatFileManager
//import com.aemerse.cropper.CropImageContract
//import com.aemerse.cropper.CropImageView
//import com.aemerse.cropper.options
import com.gigforce.modules.feature_chat.databinding.ActivityDazzleGalleryBinding
import com.gigforce.modules.feature_chat.mediapicker.gallery.BottomSheetMediaRecyclerAdapter
import com.gigforce.modules.feature_chat.mediapicker.gallery.BottomSheetMediaRecyclerAdapter.Companion.HEADER
import com.gigforce.modules.feature_chat.mediapicker.gallery.BottomSheetMediaRecyclerAdapter.Companion.SPAN_COUNT
import com.gigforce.modules.feature_chat.mediapicker.gallery.MediaModel
import com.gigforce.modules.feature_chat.mediapicker.interfaces.MediaClickInterface
import com.gigforce.modules.feature_chat.mediapicker.interfaces.PermissionCallback
import com.gigforce.modules.feature_chat.mediapicker.utils.DazzleOptions
import com.gigforce.modules.feature_chat.mediapicker.utils.GeneralUtils.getStringDate
import com.gigforce.modules.feature_chat.mediapicker.utils.HeaderItemDecoration
import com.gigforce.modules.feature_chat.mediapicker.utils.MediaConstants.IMAGE_VIDEO_URI
import com.gigforce.modules.feature_chat.mediapicker.utils.MediaConstants.getFileFromUri
import com.gigforce.modules.feature_chat.mediapicker.utils.MediaConstants.getImageVideoCursor
import com.gigforce.modules.feature_chat.mediapicker.utils.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList


class DazzleGallery : AppCompatActivity() {

    private lateinit var mBinding: ActivityDazzleGalleryBinding
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var mDazzleOptions: DazzleOptions
    private val mPathList = ArrayList<String>()
    private val mPath = ArrayList<Uri>()
    private var mVideoUri: String? = null
    private var mImageUri: String? = null
    private var win: Window? = null

    private val chatFileManager: ChatFileManager by lazy {
        ChatFileManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityDazzleGalleryBinding.inflate(layoutInflater)
//        changeStatusBarColor()
//        setStatusBarIcons(false)
        setContentView(mBinding.root)


        mDazzleOptions = intent?.getSerializableExtra(PICKER_OPTIONS) as DazzleOptions
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }
        Handler(Looper.getMainLooper()).postDelayed({ getMedia() }, 500)
    }

//    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
//        when {
//            result.isSuccessful -> {
//                val savedUri = result.getUriFilePath(this)
//                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(File(savedUri!!).extension)
//                MediaScannerConnection.scanFile(
//                    this,
//                    arrayOf(savedUri),
//                    arrayOf(mimeType)
//                ) { _, uri ->
//                    Log.d(TAG, "Image capture scanned into media store: $uri")
//                }
//                mPathList.add(savedUri.toString())
//
//                val intent = Intent()
//                intent.putExtra(Dazzle.PICKED_MEDIA_LIST, mPathList)
//                setResult(Activity.RESULT_OK, intent)
//                finish()
//            }
//            else -> {
//                setResult(Activity.RESULT_CANCELED, intent)
//                finish()
//            }
//        }
//    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()

    }

    override fun onStop() {
       // setStatusBarIcons(true)
        super.onStop()
    }

    private val galleryImageList = ArrayList<MediaModel>()
    private var mBottomMediaAdapter: BottomSheetMediaRecyclerAdapter? = null

    private fun getMedia() {

        CoroutineScope(Dispatchers.Main).launch {
            val cursor: Cursor? = withContext(Dispatchers.IO) {
                getImageVideoCursor(this@DazzleGallery, mDazzleOptions.excludeVideos)
            }

            if (cursor != null) {

                Log.e(TAG, "getMedia: ${cursor.count}")

                val index = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                val dateIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)
                val typeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)

                var headerDate = ""

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(index)
                    val path = ContentUris.withAppendedId(IMAGE_VIDEO_URI, id)
                    val mediaType = cursor.getInt(typeIndex)
                    val longDate = cursor.getLong(dateIndex)
                    val mediaDate = getStringDate(this@DazzleGallery, longDate)

                    if (!headerDate.equals(mediaDate, true)) {
                        headerDate = mediaDate
                        galleryImageList.add(MediaModel(null, mediaType, headerDate))
                    }

                    galleryImageList.add(MediaModel(path, mediaType, ""))
                }
                handleBottomSheet()
            }
        }
    }

    private val mMediaClickListener = object : MediaClickInterface {
        override fun onMediaClick(media: MediaModel) {
            pickImages()
        }

        override fun onMediaLongClick(media: MediaModel, intentFrom: String) {
            if (intentFrom == BottomSheetMediaRecyclerAdapter::class.java.simpleName) {
                when {
                    mBottomMediaAdapter?.imageCount!! > 0 -> {
                        mBinding.textViewImageCount.text = mBottomMediaAdapter?.imageCount?.toString()
                        mBinding.textViewTopSelect.text = String.format(
                            getString(R.string.images_selected),
                            mBottomMediaAdapter?.imageCount?.toString()
                        )
                        showTopViews()
                    }
                    else -> hideTopViews()
                }
            }
        }
    }

    private fun showTopViews() {
        mBinding.constraintCheck.visibility = VISIBLE
        mBinding.textViewOk.visibility = VISIBLE
        mBinding.textViewTopSelect.visibility = VISIBLE
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                mBinding.constraintBottomSheetTop.setBackgroundColor(
                    resources.getColor(
                        R.color.white,
                        null
                    )
                )
                DrawableCompat.setTint(
                    mBinding.imageViewBack.drawable,
                    resources.getColor(R.color.black, null)
                )
            }
            else -> {
                mBinding.constraintBottomSheetTop.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.white
                    )
                )
                DrawableCompat.setTint(
                    mBinding.imageViewBack.drawable,
                    ContextCompat.getColor(applicationContext, R.color.black)
                )
            }
        }
    }

    private fun hideTopViews() {
        mBinding.constraintCheck.visibility = GONE
        mBinding.textViewOk.visibility = GONE
        mBinding.textViewTopSelect.visibility = GONE
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                mBinding.constraintBottomSheetTop.setBackgroundColor(
                    resources.getColor(R.color.colorWhite, null)
                )
                DrawableCompat.setTint(
                    mBinding.imageViewBack.drawable,
                    resources.getColor(R.color.colorBlack, null)
                )
            }
            else -> {
                mBinding.constraintBottomSheetTop.setBackgroundColor(
                    ContextCompat.getColor(applicationContext, R.color.colorWhite)
                )
                DrawableCompat.setTint(mBinding.imageViewBack.drawable, ContextCompat.getColor(applicationContext, R.color.colorBlack))
            }
        }
    }

    private fun handleBottomSheet() {
        mBottomMediaAdapter = BottomSheetMediaRecyclerAdapter(galleryImageList, mMediaClickListener, this@DazzleGallery)
        mBottomMediaAdapter?.maxCount = mDazzleOptions.maxCount

        val layoutManager = GridLayoutManager(this, SPAN_COUNT)
        mBinding.recyclerViewBottomSheetMedia.layoutManager = layoutManager

        layoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (mBottomMediaAdapter?.getItemViewType(position) == HEADER) {
                    SPAN_COUNT
                } else 1
            }
        }

        mBinding.recyclerViewBottomSheetMedia.adapter = mBottomMediaAdapter
        mBinding.recyclerViewBottomSheetMedia.addItemDecoration(
            HeaderItemDecoration(
                mBottomMediaAdapter!!,
                this
            )
        )
        
        var count = 0
        galleryImageList.map { mediaModel ->
            if (mediaModel.isSelected) count++
        }
        mBottomMediaAdapter?.imageCount = count
        mBottomMediaAdapter?.notifyDataSetChanged()

        mBinding.constraintCheck.setOnClickListener { pickImages() }
        mBinding.textViewOk.setOnClickListener { pickImages() }
        mBinding.imageViewBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun pickImages() {
        mPathList.clear()
        mPath.clear()
        mVideoUri = null
        mImageUri = null
        galleryImageList.map { mediaModel ->
            if (mediaModel.isSelected) {
                Log.d("MediaPicker", "mSelectedMedia: ${mediaModel.mMediaType} , ${mediaModel.mMediaUri}")
                mPathList.add(
                    getFileFromUri(
                        contentResolver,
                        mediaModel.mMediaUri!!,
                        cacheDir
                    ).path
                )
                if (mediaModel.mMediaType == MEDIA_TYPE_IMAGE) {
                    mPath.add(mediaModel.mMediaUri!!)
                    mImageUri = mediaModel.mMediaUri.toString()
                }
                if (mediaModel.mMediaType == 3) {
                    mPath.add(mediaModel.mMediaUri!!)
                    mVideoUri = getFileFromUri(
                        contentResolver,
                        mediaModel.mMediaUri!!,
                        cacheDir
                    ).path
                }
            }
        }

        //Log.d("MediaPicker", "mVideoUri: $mVideoUri , mImageUri: $mImageUri")

        when {
            mDazzleOptions.cropEnabled && mImageUri != null -> {
                Log.d("MediaPicker", "Going for cropping the image")
                startCropImage(imageUri = Uri.parse(mImageUri),
                    getImageCropOptions((true)))
            }
            mVideoUri != null -> {
                val intent = Intent()
                intent.putExtra(PICKED_MEDIA_URI, mVideoUri)
                intent.putExtra(PICKED_MEDIA_TYPE, "video")
                setResult(RESULT_OK, intent)
                finish()
            }
            else -> {
                Toast.makeText(this, "Select at least one media", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        when {
            mBottomMediaAdapter?.imageCount != null && mBottomMediaAdapter?.imageCount!! > 0 -> removeSelection()
            else -> super.onBackPressed()
        }
    }

    private fun getImageCropOptions(
        shouldCreatedDestinationFile: Boolean
    ): ImageCropOptions {


        return ImageCropOptions
            .Builder()
            .shouldOpenImageCrop(true)
            .setShouldEnableFaceDetector(false)
            .shouldEnableFreeCrop(true).apply {

                if (shouldCreatedDestinationFile) {

                    val image = chatFileManager.createImageFile()
                    setOutputFileUri(image)
                    Log.d("ChatPage", "creating file ...")
                }
            }
            .build()
    }

    private fun startCropImage(
        imageUri: Uri,
        imageCropOptions: ImageCropOptions
    ) {
        val photoCropIntent = Intent(this, ImageCropActivity::class.java)
        photoCropIntent.putExtra("outgoingUri", imageUri.toString())
        photoCropIntent.putExtra(ImageCropActivity.INTENT_EXTRA_DESTINATION_URI,imageCropOptions.outputFileUri)

        val outputFileUri = imageCropOptions.outputFileUri
            ?: Uri.fromFile(File(this.cacheDir, "IMG_" + System.currentTimeMillis() + CameraAndGalleryIntegrator.EXTENSION))
        photoCropIntent.putExtra(ImageCropActivity.INTENT_EXTRA_DESTINATION_URI,outputFileUri)
        photoCropIntent.putExtra(ImageCropActivity.INTENT_EXTRA_ENABLE_FREE_CROP,imageCropOptions.freeCropEnabled)


//        if (fragment != null) {
//            fragment!!.startActivityForResult(photoCropIntent, ImageCropActivity.CROP_RESULT_CODE)
//        } else {
        this.startActivityForResult(photoCropIntent, ImageCropActivity.CROP_RESULT_CODE)
        //}
    }

    private fun removeSelection() {
        mBottomMediaAdapter?.imageCount = 0
        for (i in 0 until galleryImageList.size) galleryImageList[i].isSelected = false
        mBottomMediaAdapter?.notifyDataSetChanged()
        mBinding.constraintCheck.visibility = GONE
        mBinding.textViewOk.visibility = GONE
        mBinding.textViewTopSelect.visibility = GONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBinding.constraintBottomSheetTop.setBackgroundColor(
                resources.getColor(
                    R.color.colorWhite,
                    null
                )
            )
            DrawableCompat.setTint(
                mBinding.imageViewBack.drawable,
                resources.getColor(R.color.colorBlack, null)
            )
        } else {
            mBinding.constraintBottomSheetTop.setBackgroundColor(
                ContextCompat.getColor(
                    applicationContext,
                    R.color.colorWhite
                )
            )
            DrawableCompat.setTint(
                mBinding.imageViewBack.drawable,
                ContextCompat.getColor(applicationContext, R.color.colorBlack)
            )
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    fun setStatusBarIcons(shouldChangeStatusBarTintToDark: Boolean){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decor: View = this?.window?.decorView!!
            if (shouldChangeStatusBarTintToDark) {
                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                // We want to change tint color to white again.
                // You can also record the flags in advance so that you can turn UI back completely if
                // you have set other flags before, such as translucent or full screen.
                decor.systemUiVisibility = 0
            }
        }
    }

    private fun changeStatusBarColor() {
        win = this.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

// finally change the color
        win?.statusBarColor = resources.getColor(R.color.lipstick)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ImageCropActivity.CROP_RESULT_CODE -> {
                if (resultCode == Activity.RESULT_OK){
                    val imageUriResultCrop: Uri? =
                        Uri.parse(data?.getStringExtra(ImageCropActivity.CROPPED_IMAGE_URL_EXTRA))
                    Log.d("MediaPicker", "Cropped image: $imageUriResultCrop")
                    val intent = Intent()
                    intent.putExtra(PICKED_MEDIA_URI, imageUriResultCrop.toString())
                    intent.putExtra(PICKED_MEDIA_TYPE, "image")
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
    }
    
    companion object {
        private const val TAG = "Picker"
       const val REQUEST_CODE_PICKER = 10
        const val PICKER_OPTIONS = "PICKER_OPTIONS"
        const val PICKED_MEDIA_LIST = "PICKED_MEDIA_LIST"
        const val PICKED_MEDIA_URI = "PICKED_MEDIA_URI"
        const val PICKED_MEDIA_TYPE = "PICKED_MEDIA_TYPE"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        @JvmStatic
        fun startPicker(fragment: Fragment, mDazzleOptions: DazzleOptions) {
            PermissionUtils.checkForCameraWritePermissions(fragment, object : PermissionCallback {
                override fun onPermission(approved: Boolean) {
                    val mPickerIntent = Intent(fragment.activity, DazzleGallery::class.java)
                    mPickerIntent.putExtra(PICKER_OPTIONS, mDazzleOptions)
                    mPickerIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    fragment.startActivityForResult(mPickerIntent, REQUEST_CODE_PICKER)
                }
            })
        }

        @JvmStatic
        fun startPicker(activity: FragmentActivity, mDazzleOptions: DazzleOptions) {
            PermissionUtils.checkForCameraWritePermissions(activity, object : PermissionCallback {
                override fun onPermission(approved: Boolean) {
                    val mPickerIntent = Intent(activity, DazzleGallery::class.java)
                    mPickerIntent.putExtra(PICKER_OPTIONS, mDazzleOptions)
                    mPickerIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    activity.startActivityForResult(mPickerIntent, REQUEST_CODE_PICKER)
                }
            })
        }
    }
}