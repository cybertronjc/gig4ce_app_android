package com.gigforce.app.modules.verification

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.auth.ui.main.Login
import com.gigforce.app.modules.photocrop.*
import com.gigforce.app.modules.verification.Verification
import com.gigforce.app.modules.verification.VerificationViewModel
//import com.gigforce.app.modules.verification.models.VerificationData
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.layout_verification.view.*
import kotlinx.android.synthetic.main.layout_verification_aadhaar.*
import kotlinx.android.synthetic.main.layout_verification_aadhaar.view.*
import kotlinx.android.synthetic.main.layout_verification_pancard.view.*
import java.io.ByteArrayOutputStream

class AadhaarUpload: Fragment() {
    companion object {
        fun newInstance() = Login()
    }

    private lateinit var storage: FirebaseStorage
    lateinit var layout: View
    private lateinit var AadhaarFront: ImageView
    private lateinit var AadhaarBack: ImageView
    lateinit var viewModel: VerificationViewModel
    private var PHOTO_CROP: Int = 45
    private var frontNotDone = 1;
    private var docUploaded = 0;

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        storage = FirebaseStorage.getInstance()
        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)
        layout = inflater.inflate(R.layout.layout_verification_aadhaar, container, false)
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        layout.pbAadhaar.setProgress(40,true)
        return layout
    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() { // Handle the back button event
                onBackPressed()
            }
        }

    fun onBackPressed() {
        findNavController().popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)
        AadhaarFront = layout.findViewById(R.id.Aadhaar_front)
        AadhaarBack = layout.findViewById(R.id.Aadhaar_back)
        val photoCropIntent = Intent(context, PhotoCrop::class.java)
        photoCropIntent.putExtra("fbDir", "/verification/aadhaar/")
        photoCropIntent.putExtra("folder", "/verification/aadhaar/")
        photoCropIntent.putExtra("detectFace",0)
        AadhaarFront.setOnClickListener {
            photoCropIntent.putExtra("file", "adfront.jpg")
            startActivityForResult(photoCropIntent, PHOTO_CROP)
        }
        AadhaarBack.setOnClickListener {
            photoCropIntent.putExtra("file", "adback.jpg")
            startActivityForResult(photoCropIntent, PHOTO_CROP)
        }

        buttonAadhaar2.setOnClickListener {
            findNavController().navigate(R.id.verification)
        }

        buttonAadhaar1.setOnClickListener {
            if(docUploaded==1)
            {
                findNavController().navigate(R.id.uploadDropDown)
            }
            else {
                Toast.makeText(
                    this.context,
                    "Please upload the Aadhaar before proceeding",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun encodeImageToBase64(uri: Uri):String{
        val baos = ByteArrayOutputStream()
        val bitmap =  MediaStore.Images.Media.getBitmap(context?.contentResolver, uri);//BitmapFactory.decodeResource(resources, uri)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes: ByteArray = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Unit {

        super.onActivityResult(requestCode, resultCode, data)

        /*
        For photo crop. The activity returns the the filename with which the cropped photo
        is saved on firestore. The name is updated in profile information and the new
        photo is loaded in the view
        */
        if (requestCode == PHOTO_CROP && resultCode == Activity.RESULT_OK) {
            var imageName: String? = data?.getStringExtra("filename")
            Log.v("PROFILE_FRAG_OAR", "filename is:" + imageName)
            if (null != imageName) {
                viewModel.setCardAvatarName(imageName.toString())
                var filepath = "/Aadhaar/"+imageName;
                if(frontNotDone==1){
                    loadImage("verification",filepath, layout.Aadhaar_front)
                    frontNotDone = 0;
                }
                else{
                    loadImage("verification",filepath, layout.Aadhaar_back)
                    docUploaded = 1;
                }
            }
        }
    }

    private fun loadImage(collection: String, filepath: String, layoutid: ImageView ) {
        var picRef: StorageReference = storage.reference.child(collection).child(filepath)
        GlideApp.with(this.context!!)
            .load(picRef)
            .into(layoutid)
    }
}