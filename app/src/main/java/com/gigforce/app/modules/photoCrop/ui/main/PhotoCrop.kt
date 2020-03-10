package com.gigforce.app.modules.photoCrop.ui.main

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.gigforce.app.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.UploadTask.*
import com.yalantis.ucrop.UCrop
import java.io.File

class PhotoCrop : AppCompatActivity() {

    private var img: ImageView? = null
    private val CODE_IMG_GALLERY: Int = 1
    private val SAMPLE_CROPPED_IMG_NAME: String = "SampleCropImg"
    //lateinit var uri : Uri
    var mStorage: FirebaseStorage = FirebaseStorage.getInstance()


    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)
        //        setContentView(R.layout.activity_main)
//        init()
//        img.setOnClickListener(object : View.OnClickListener {
//            open fun onClick(v: View): Unit {
//                startActivityForResult(
//                    Intent().setAction(Intent.ACTION_GET_CONTENT)
//                        .setType("image/*"), CODE_IMG_GALLERY
//                )
//            }
//        })
        startActivityForResult(
            Intent().setAction(Intent.ACTION_GET_CONTENT)
                .setType("image/*"), CODE_IMG_GALLERY
        )
    }


//    open fun init(): Unit {
//        var img = findViewById<ImageView>(R.id.imageView)
//    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Unit {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_IMG_GALLERY && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                startCrop(imageUri)
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? = UCrop.getOutput((data!!))
            if (imageUriResultCrop != null) {
                img?.setImageURI(imageUriResultCrop)
            }
        }
    }

    open fun startCrop(uri: Uri): Unit {
        var destinationFileName: String? = SAMPLE_CROPPED_IMG_NAME
        destinationFileName += ".jpg"
        val uCrop: UCrop = UCrop.of(
            uri,
            Uri.fromFile(File(cacheDir, destinationFileName))
        )
        uCrop.withAspectRatio(1F, 1F)
        uCrop.withAspectRatio(16F, 9F)
        uCrop.withMaxResultSize(450, 450)
        uCrop.withOptions(getCropOptions())
        uCrop.start(this as AppCompatActivity, Activity.RESULT_OK)
        upload(Uri.fromFile(File(cacheDir, destinationFileName)))
    }

    open fun getCropOptions(): UCrop.Options {
        val options: UCrop.Options = UCrop.Options()
        options.setCompressionQuality(70)
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
        options.setHideBottomControls((false))
        options.setFreeStyleCropEnabled(true)
        options.setStatusBarColor(resources.getColor(R.color.colorPrimaryDark))
        options.setToolbarColor(resources.getColor(R.color.colorPrimary))
        options.setToolbarTitle("image adjustment")
        return options
    }

    private fun upload(uri:Uri) {
        var mReference = mStorage.reference.child("profile_pics").child(uri.lastPathSegment!!)
        try {
            mReference.putFile(uri).addOnSuccessListener { taskSnapshot: TaskSnapshot->
                val url:String = taskSnapshot.metadata?.reference?.downloadUrl.toString()
//                val dwnTxt = findViewById<View>(R.id.dwnTxt) as TextView
//                dwnTxt.text = url.toString()
                Toast.makeText(this, "Successfully Uploaded :)", Toast.LENGTH_LONG).show()
                print(url)
            }
        }catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }

    }

}