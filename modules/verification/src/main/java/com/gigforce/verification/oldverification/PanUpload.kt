package com.gigforce.verification.oldverification

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.common_ui.utils.UtilMethods.encodeImageToBase64
import com.gigforce.core.datamodels.verification.PANDocData
import com.gigforce.core.datamodels.verification.PostDataPAN
import com.gigforce.verification.oldverification.service.RetrofitFactory
import com.gigforce.core.utils.GlideApp
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_verification_pancard.*
import kotlinx.android.synthetic.main.layout_verification_pancard.view.*
import javax.inject.Inject

@AndroidEntryPoint
class PanUpload: Fragment() {
    companion object {
        fun newInstance() = PanUpload()
    }

    private lateinit var storage: FirebaseStorage
    var firebaseDB = FirebaseFirestore.getInstance()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    lateinit var layout: View
    private lateinit var panFront: ImageView
    private lateinit var panBack: ImageView
    lateinit var viewModel: VerificationViewModel
    private var PHOTO_CROP: Int = 45
    private var frontNotDone = 1;
    private var docUploaded = 0;

    private  lateinit  var uriFront: Uri
    private  lateinit  var uriBack: Uri

    @Inject lateinit var navigation : INavigation
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        storage = FirebaseStorage.getInstance()
        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)
        layout = inflater.inflate(R.layout.layout_verification_pancard, container, false)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        layout.pbPan.setProgress(80,true)
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
//        val photoCropIntent = Intent(context, PhotoCrop::class.java)
        val photoCropIntent = Intent()
        photoCropIntent.putExtra("purpose","verification")
        photoCropIntent.putExtra("uid",viewModel.uid)
        photoCropIntent.putExtra("folder", "/verification/pan/")
        photoCropIntent.putExtra("fbDir", "/verification/pan/")
        photoCropIntent.putExtra("detectFace",0)
        panFront.setOnClickListener {
            photoCropIntent.putExtra("file", "panfront.jpg")
//            startActivityForResult(photoCropIntent, PHOTO_CROP)
            navigation.navigateToPhotoCrop(photoCropIntent,PHOTO_CROP,this)
        }
        panBack.setOnClickListener {
            if(panFront.drawable==null) {
                Toast.makeText(
                    this.context,
                    "Please upload the front side first!",
                    Toast.LENGTH_LONG).show()
            }
            else {
                photoCropIntent.putExtra("file", "panback.jpg")
//                startActivityForResult(photoCropIntent, PHOTO_CROP)
                navigation.navigateToPhotoCrop(photoCropIntent,PHOTO_CROP,this)

            }
        }

        buttonPan2.setOnClickListener {
//            findNavController().navigate(R.id.bankUpload2);
            navigation.navigateTo("verification/bankUpload2")
        }

        buttonPan1.setOnClickListener {
            //if() docs are not uploaded
            if(docUploaded==1)
            {
//                findNavController().navigate(R.id.verificationDone)
                navigation.navigateTo("verification/verificationDone")
            }
            else {
                Toast.makeText(
                    this.context,
                    "Please upload the Pan before proceeding",
                    Toast.LENGTH_LONG).show()
            }
        }
    }


    @SuppressLint("CheckResult", "UseRequireInsteadOfGet")
    private fun idfyApiCall(postData: PostDataPAN){
        if(this.context?.let { UtilMethods.isConnectedToInternet(it) }!!){
            this.context?.let { UtilMethods.showLoading(it) }
            val observable = RetrofitFactory.idfyApiCallPAN().postPAN(postData)
            observable.subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .subscribe({ response ->
                    UtilMethods.hideLoading()
                    //here we can load all the data required
                    var extractionOutput = response!!.result!!.extraction_output!!
                    // TODO: check the doc type is actually pan by - "type": "ind_pan"
                    firebaseDB.collection("Verification")
                        .document(uid).update("PAN", FieldValue.arrayUnion(extractionOutput))
                        .addOnSuccessListener {
                            Toast.makeText(
                                this.context,
                                "Document successfully uploaded!",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("REPOSITORY", "Aadhaar added successfully!")
                        }
                        .addOnFailureListener{
                                exception ->  Log.d("Repository", exception.toString())
                            Toast.makeText(
                                this.context,
                                "Some failure, please retry!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Unit {
        super.onActivityResult(requestCode, resultCode, data)
        /*
        For photo crop. The activity returns the the filename with which the cropped photo
        is saved on firestore. The name is updated in verification information and the new
        photo is loaded in the view

        // can we combine or concatenate both front and back images into one and do one id  fy call instead of two?
        https://stackoverflow.com/questions/2738834/combining-two-png-files-in-android
        */
        if (requestCode == PHOTO_CROP && resultCode == Activity.RESULT_OK) {
            var imageName: String? = data?.getStringExtra("filename")
            Log.v("PAN UPLOAD", "filename is:" + imageName)
            if (null != imageName) {
                viewModel.setCardAvatarName(imageName.toString())
                var filepath = "/pan/"+imageName;
                if(frontNotDone == 1) {
                    uriFront = data?.getParcelableExtra("uri")!!;
                    layout.Pan_front.setImageURI(uriFront);
                    frontNotDone = 0;
                }
                else{
                    uriBack = data?.getParcelableExtra("uri")!!;
                    var imgb641 = encodeImageToBase64(requireContext(),uriFront);
                    var imgb642 = encodeImageToBase64(requireContext(),uriBack);
                    var ocrdata =
                        PANDocData(
                            imgb641,
                            imgb642,
                            "yes"
                        )
                    val taskid:String = "74f4c926-250c-43ca-9c53-453e87ceacd2";
                    val groupid:String = "8e16424a-58fc-4ba4-ab20-5bc8e7c3c41f";
                    var postData =
                        PostDataPAN(
                            taskid,
                            groupid,
                            ocrdata!!
                        )
                    idfyApiCall(postData)
                    //loadImage("verification",filepath, layout.Pan_back)
                    layout.pbPan.setProgress(100,true)
                    layout.Pan_back.setImageURI(uriBack);
                    docUploaded = 1;
                }
            }
        }
    }
    
    private fun loadImage(collection: String, filepath: String, layoutid: ImageView ) {
        var picRef: StorageReference = storage.reference.child(collection).child(filepath)
        GlideApp.with(requireContext())
            .load(picRef)
            .into(layoutid)
    }
}