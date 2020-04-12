package com.gigforce.app.modules.verification

import android.annotation.SuppressLint
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

import com.gigforce.app.modules.verification.models.OCRDocData
import com.gigforce.app.modules.verification.models.OCRDocsData
import com.gigforce.app.modules.verification.models.PostDataOCR
import com.gigforce.app.modules.verification.models.PostDataOCRs
import com.gigforce.app.modules.verification.service.RetrofitFactory
import com.gigforce.app.utils.GlideApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
    var firebaseDB = FirebaseFirestore.getInstance()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    lateinit var layout: View
    private lateinit var AadhaarFront: ImageView
    private lateinit var AadhaarBack: ImageView
    lateinit var viewModel: VerificationViewModel
    private var PHOTO_CROP: Int = 45
    private var frontNotDone = 1;
    private var docUploaded = 0;

    private  lateinit  var uriFront: Uri
    private  lateinit  var uriBack: Uri

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
        tvAadhaarNo.setOnClickListener { findNavController().navigate(R.id.uploadDropDown) }
        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)
        AadhaarFront = layout.findViewById(R.id.Aadhaar_front)
        AadhaarBack = layout.findViewById(R.id.Aadhaar_back)
        val photoCropIntent = Intent(context, PhotoCrop::class.java)
        photoCropIntent.putExtra("purpose","verification")
        photoCropIntent.putExtra("uid",viewModel.uid)
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

    @SuppressLint("CheckResult")
    private fun idfyApiCall(postData: PostDataOCR){
        if(this.context?.let { UtilMethods.isConnectedToInternet(it) }!!){
            this.context?.let { UtilMethods.showLoading(it) }
            val observable = RetrofitFactory.idfyApiCall().postOCR(postData)
            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    UtilMethods.hideLoading()
                    //here we can load all the data required

                    var extractionOutput = response!!.result!!.extraction_output!!

                    firebaseDB.collection("Verification")
                        .document(uid).update("Aadhar", FieldValue.arrayUnion(extractionOutput))
                        .addOnSuccessListener {
                            Log.d("REPOSITORY", "contact added successfully!")
                        }
                        .addOnFailureListener{
                                exception ->  Log.d("Repository", exception.toString())
                        }

                    Toast.makeText(
                        this.context,
                        ">>>"+response!!.result!!.extraction_output!!.gender!!.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    /** response is response data class*/
                }, { error ->
                    UtilMethods.hideLoading()
                    UtilMethods.showLongToast(this.context!!, error.message.toString())
                }
                )
        }else{
            UtilMethods.showLongToast(this.context!!, "No Internet Connection!")
        }
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
                    uriFront = data?.getParcelableExtra("uri")!!;
                    //loadImage("verification",filepath, layout.Aadhaar_front)
                    layout.Aadhaar_front.setImageURI(uriFront);
                    frontNotDone = 0;
                }
                else{
                    uriBack = data?.getParcelableExtra("uri")!!;
                    Toast.makeText(
                        this.context,
                        "front: $uriFront"+"<<<back: $uriBack",
                        Toast.LENGTH_SHORT
                    ).show()
                    //var imgb64 = UtilMethods.encodeImagesToBase64(context!!, uriFront, uriBack);
                    var imgb64 = UtilMethods.encodeImageToBase64(context!!, uriFront);
                    var ocrdata = OCRDocData(imgb64,"yes")
                    //var ocrdata = OCRDocsData(imgb64,imgb64,"yes")
                    val taskid:String = "74f4c926-250c-43ca-9c53-453e87ceacd2";
                    val groupid:String = "8e16424a-58fc-4ba4-ab20-5bc8e7c3c41f";
                    var postData = PostDataOCR(taskid,groupid,ocrdata!!)
                    idfyApiCall(postData)
                    //loadImage("verification",filepath, layout.Aadhaar_back)
                    layout.Aadhaar_back.setImageURI(uriBack);
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