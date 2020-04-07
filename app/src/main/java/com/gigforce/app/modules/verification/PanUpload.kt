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
import com.gigforce.app.modules.verification.models.Address
import com.gigforce.app.modules.verification.models.Idfydata
import com.gigforce.app.modules.verification.models.OCRDocData
import com.gigforce.app.modules.verification.models.PostDataOCR
import com.gigforce.app.modules.verification.service.RetrofitFactory
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.android.schedulers.AndroidSchedulers.*
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_verification.view.*
import kotlinx.android.synthetic.main.layout_verification_pancard.*
import kotlinx.android.synthetic.main.layout_verification_pancard.view.*
import java.io.ByteArrayOutputStream

class PanUpload: Fragment() {
    companion object {
        fun newInstance() = PanUpload()
    }

    private lateinit var storage: FirebaseStorage
    lateinit var layout: View
    private lateinit var panFront: ImageView
    private lateinit var panBack: ImageView
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
        layout = inflater.inflate(R.layout.layout_verification_pancard, container, false)
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        layout.pbPan.setProgress(20,true)
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
        panFront = layout.findViewById(R.id.Pan_front)
        panBack = layout.findViewById(R.id.Pan_back)
        val photoCropIntent = Intent(context, PhotoCrop::class.java)
        photoCropIntent.putExtra("folder", "/verification/pan/")
        photoCropIntent.putExtra("fbDir", "/verification/pan/")
        photoCropIntent.putExtra("detectFace",0)
        panFront.setOnClickListener {
            photoCropIntent.putExtra("file", "panfront.jpg")
            startActivityForResult(photoCropIntent, PHOTO_CROP)
        }
        panBack.setOnClickListener {
            photoCropIntent.putExtra("file", "panback.jpg")
            startActivityForResult(photoCropIntent, PHOTO_CROP)
        }

        buttonPan1.setOnClickListener {
            findNavController().navigate(R.id.verification);
        }

        buttonPan1.setOnClickListener {
            //if() docs are not uploaded
            if(docUploaded==1)
            {
                findNavController().navigate(R.id.aadhaarUpload)
            }
            else {
                Toast.makeText(
                    this.context,
                    "Please upload the Pan before proceeding",
                    Toast.LENGTH_LONG).show()
            }
        }
    }


    @SuppressLint("CheckResult")
    private fun idfyApiCall(postData: OCRDocData){
        if(this.context?.let { UtilMethods.isConnectedToInternet(it) }!!){
            this.context?.let { UtilMethods.showLoading(it) }
            val observable = RetrofitFactory.idfyApiCall().postOCR(postData)
            observable.subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .subscribe({ response ->
                    UtilMethods.hideLoading()
                    //here we can load all the data required
                    Toast.makeText(
                        this.context,
                        ">>"+response!!.result!!.extraction_output!!.gender!!.toString(),
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
                var filepath = "/pan/"+imageName;
                if(frontNotDone==1){
                    loadImage("verification",filepath, layout.Pan_front)
                    frontNotDone = 0;
                }
                else{
                    loadImage("verification",filepath, layout.Pan_back)
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