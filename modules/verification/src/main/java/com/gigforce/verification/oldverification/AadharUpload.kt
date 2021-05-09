package com.gigforce.verification.oldverification

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
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.gigforce.verification.oldverification.service.RetrofitFactory
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.core.datamodels.verification.OCRDocsData
import com.gigforce.core.datamodels.verification.PostDataOCRs
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.gigforce.verification.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_verification_aadhaar.*
import kotlinx.android.synthetic.main.layout_verification_aadhaar.view.*
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class AadhaarUpload : Fragment() {

    private lateinit var storage: FirebaseStorage
    var firebaseDB = FirebaseFirestore.getInstance()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    private var layout: View? = null
    private lateinit var AadhaarFront: ImageView
    private lateinit var AadhaarBack: ImageView
    lateinit var viewModel: VerificationViewModel
    private var PHOTO_CROP: Int = 45
    private var frontNotDone = 1
    private var docUploaded = 0

    private lateinit var uriFront: Uri
    private lateinit var uriBack: Uri
    @Inject lateinit var navigation : INavigation

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        storage = FirebaseStorage.getInstance()
        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)
        layout = inflater.inflate(R.layout.layout_verification_aadhaar, container, false)
        //requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        layout?.pbAadhaar?.setProgress(20, true)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvAadhaarNo.setOnClickListener {
//            findNavController().navigate(R.id.uploadDropDown)
            navigation.navigateTo("verification/uploadDropDown")
        }
        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)
        AadhaarFront = layout?.findViewById(R.id.Aadhaar_front)!!
        AadhaarBack = layout?.findViewById(R.id.Aadhaar_back)!!

//        val photoCropIntent = Intent(context, PhotoCrop::class.java)
        val photoCropIntent = Intent()
        photoCropIntent.putExtra("purpose", "verification")
        photoCropIntent.putExtra("uid", viewModel.uid)
        photoCropIntent.putExtra("fbDir", "/verification/aadhaar/")
        photoCropIntent.putExtra("folder", "/verification/aadhaar/")
        photoCropIntent.putExtra("detectFace", 0)

        AadhaarFront.setOnClickListener {
            photoCropIntent.putExtra("file", "adfront.jpg")
//            startActivityForResult(photoCropIntent, PHOTO_CROP)
            activity?.let {
                navigation.navigateToPhotoCrop(photoCropIntent,PHOTO_CROP,it)

            }
        }
        AadhaarBack.setOnClickListener {
            if (AadhaarFront.drawable == null) {
                showToast("Please upload the front side first!")
            } else {
                photoCropIntent.putExtra("file", "adback.jpg")
//                startActivityForResult(photoCropIntent, PHOTO_CROP)
                activity?.let {
                    navigation.navigateToPhotoCrop(photoCropIntent,PHOTO_CROP,it)

                }
            }
        }

        buttonAadhaar2.setOnClickListener {
//            findNavController().navigate(R.id.verification)
            navigation.navigateTo("verification")
        }

        buttonAadhaar1.setOnClickListener {
            if (docUploaded == 1) {
//                findNavController().navigate(R.id.uploadDropDown)
                navigation.navigateTo("verification/uploadDropDown")
            } else {
                showToast("Please upload the Aadhaar before proceeding")
            }
        }
    }

    private fun encodeImageToBase64(uri: Uri): String {
        val baos = ByteArrayOutputStream()
        val bitmap = MediaStore.Images.Media.getBitmap(
            context?.contentResolver,
            uri
        )//BitmapFactory.decodeResource(resources, uri)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes: ByteArray = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    @SuppressLint("CheckResult", "UseRequireInsteadOfGet")
    private fun idfyApiCall(postData: PostDataOCRs) {
        if (this.context?.let { UtilMethods.isConnectedToInternet(it) }!!) {
            this.context?.let { UtilMethods.showLoading(it) }
            val observable = RetrofitFactory.idfyApiCallAD().postOCR(postData)
            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    UtilMethods.hideLoading()
                    //here we can load all the data required
                    var extractionOutput = response!!.result.extraction_output

                    firebaseDB.collection("Verification")
                        .document(uid).update("Aadhaar", FieldValue.arrayUnion(extractionOutput))
                        .addOnSuccessListener {
                            showToast("Document successfully uploaded!")
                            Log.d("REPOSITORY", "Aadhaar added successfully!")
                        }
                        .addOnFailureListener { exception ->
                            Log.d("Repository", exception.toString())
                            showToast("Some failure, please retry!")
                        }
                    /** response is response data class*/
                }, { error ->
                    UtilMethods.hideLoading()
                    UtilMethods.showLongToast(this.context!!, error.message.toString())
                }
                )
        } else {
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
        is saved on firestore. The name is updated in verification information and the new
        photo is loaded in the view
        */
        if (requestCode == PHOTO_CROP && resultCode == Activity.RESULT_OK) {
            var imageName: String? = data?.getStringExtra("filename")
            Log.v("verification_FRAG_OAR", "filename is:" + imageName)
            if (null != imageName) {
                viewModel.setCardAvatarName(imageName.toString())
                var filepath = "/Aadhaar/" + imageName
                if (frontNotDone == 1) {
                    uriFront = data?.getParcelableExtra("uri")!!
                    //loadImage("verification",filepath, layout?.Aadhaar_front)
                    layout?.Aadhaar_front?.setImageURI(uriFront)
                    frontNotDone = 0
                } else {
                    uriBack = data?.getParcelableExtra("uri")!!
                    //var imgb64 = UtilMethods.encodeImagesToBase64(context!!, uriFront, uriBack);
                    var imgb641 = UtilMethods.encodeImageToBase64(requireContext(), uriFront)
                    var imgb642 = UtilMethods.encodeImageToBase64(requireContext(), uriBack)
                    var ocrdata =
                        OCRDocsData(
                            imgb641,
                            imgb642,
                            "yes"
                        )
                    //var ocrdata = OCRDocsData(imgb64,imgb64,"yes")
                    val taskid: String = "74f4c926-250c-43ca-9c53-453e87ceacd2"
                    val groupid: String = "8e16424a-58fc-4ba4-ab20-5bc8e7c3c41f"
                    var postData =
                        PostDataOCRs(
                            taskid,
                            groupid,
                            ocrdata
                        )
                    idfyApiCall(postData)
                    //loadImage("verification",filepath, layout?.Aadhaar_back)
                    layout?.Aadhaar_back?.setImageURI(uriBack)
                    docUploaded = 1
                }
            }
        }
    }

    private fun loadImage(collection: String, filepath: String, layoutid: ImageView) {
        var picRef: StorageReference = storage.reference.child(collection).child(filepath)
        GlideApp.with(this.requireContext())
            .load(picRef)
            .into(layoutid)
    }
}