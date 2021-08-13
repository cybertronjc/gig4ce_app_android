package com.gigforce.common_ui.utils
import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.gigforce.common_ui.R
import com.gigforce.common_ui.databinding.ActivityImageCropBinding
import com.gigforce.core.logger.GigforceLogger
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ImageCropActivity : AppCompatActivity() {

    companion object {

    }

    private lateinit var viewBinding: ActivityImageCropBinding
    private var cropImageUri: Uri? = null
    private lateinit var incomingFile: String
    @Inject
    lateinit var logger: GigforceLogger
    private var win: Window? = null

    /**
     * Every call to start crop needs to have an extra with the key "purpose"
     * based on the purpose, a function can be created to get the other extras
     * depending on the purpose. Should not initiate other bundles in onCreate
     * to keep this activity adaptable to all cropping purposes
     */
    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }

        //this.setContentView(R.layout.activity_image_crop)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_image_crop)
        //getDataFromIntent(savedInstanceState)
        val extras = intent.extras
        if (extras != null) {
            incomingFile = extras.getString("outgoingUri").toString()
        }
        changeStatusBarColor()
        setIncomingImage(incomingFile)
        initListeners()

    }

    private fun initListeners() = viewBinding.apply{



        //this will rotate the image by 90 degree in anticlockwise
        rotateImg.setOnClickListener {
            rotateImageAntiClock()
        }

        closeImg.setOnClickListener {
            //finish()
            MaterialAlertDialogBuilder(this@ImageCropActivity)
                .setTitle("Alert")
                .setMessage("Are you sure you want to close?")
                .setPositiveButton("Okay") { dialog, _ ->
                    dialog.dismiss()
                    onBackPressed()
                }
                .show()
        }

        okayImg.setOnClickListener {
            viewBinding.progressCircular.visibility = View.VISIBLE
            cropImageView.getCroppedImageAsync()

        }
        cropImageView.setOnCropImageCompleteListener(object : CropImageView.OnCropImageCompleteListener {
            override fun onCropImageComplete(
                view: CropImageView,
                result: CropImageView.CropResult
            ) {
                viewBinding.progressCircular.visibility = View.GONE
                handleCropResult(result)
            }

        })

        cropImageView.setOnSetImageUriCompleteListener(object : CropImageView.OnSetImageUriCompleteListener {
            override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
                viewBinding.progressCircular.visibility = View.GONE
                Log.d("uri", "uri : $uri")
            }

        })

        appBar.apply {
            setBackButtonListener(View.OnClickListener {
                onBackPressed()
            })
        }

    }

    private fun changeStatusBarColor() {
        win = this.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        win?.setStatusBarColor(resources.getColor(R.color.status_bar_pink))
    }


    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri
            val uriContent = result.uriContent
            val uriFilePath = result.getUriFilePath(this)  // optional usage
            Log.d("returnedUri", "content : $uriContent , path: $uriFilePath")
        } else {
            // an error occurred
            val exception = result.error
            Log.d("returnedUriError", "error : $exception")
        }
    }

     private fun setIncomingImage(incomingFile: String) = viewBinding.apply{
         if (incomingFile.isNotEmpty()){
             val incomingUri: Uri = Uri.parse(incomingFile)
             logger.d(ImageCropFragment.logTag, "incomingFile uri : $incomingUri")
             viewBinding.progressCircular.visibility = View.VISIBLE
             cropImageUri = incomingUri
             cropImageView.setImageUriAsync(incomingUri)
             setCropImageControls()
         } else {
             //showToast("Invalid incoming image")
             Toast.makeText(this@ImageCropActivity, "Invalid incoming image", Toast.LENGTH_SHORT)
         }

     }

    private fun rotateImageAntiClock(){
        viewBinding.cropImageView.rotateImage(90)
    }



    override fun onBackPressed() {
        super.finish()
    }

    private fun getCroppedImage(){
        viewBinding.cropImageView.getCroppedImageAsync()
    }

     private fun setCropImageControls(){

         viewBinding.cropImageView.apply {
             setAspectRatio(1, 1)
             setCenterMoveEnabled(true)
             isShowProgressBar = true
             setBackgroundColor(resources.getColor(R.color.warm_grey))
             setFixedAspectRatio(true)
             setMultiTouchEnabled(false)
             isShowCropOverlay = true
         }
     }



    private fun handleCropResult(result: CropImageView.CropResult?) {
        if (result != null && result?.error == null) {
//            val imageBitmap =
//                if (viewBinding.cropImageView.cropShape == CropImageView.CropShape.OVAL)
//                    result.bitmap?.let { CropImage.toOvalBitmap(it) }
//                else result.bitmap
            Log.v("File Path", "filepath ${result?.getUriFilePath(this)} ")
            Log.v("File result", "result : ${result?.bitmap.toString()} , ${result?.cropRect.toString()}, ${result.uriContent.toString()} , :bimap , ${result.error}" )

            logger.d(ImageCropFragment.logTag, "cropped result  : ${result?.uriContent.toString() }")
            //SCropResultActivity.start(this, imageBitmap, result.uriContent, result.sampleSize)
            val resultIntent = Intent()
            resultIntent.putExtra("croppedImage", result?.uriContent?.toString())
            setResult(Activity.RESULT_OK, resultIntent)
            finish()

        } else {
            Log.e("AIC", "Failed to crop image", result?.error)
         logger.d(ImageCropFragment.logTag, "Failed to crop image", result?.error.toString())

         Toast.makeText(this@ImageCropActivity, "Crop failed: ${result?.error?.message}", Toast.LENGTH_SHORT)
        }
    }

}
