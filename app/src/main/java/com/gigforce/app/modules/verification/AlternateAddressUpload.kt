package com.gigforce.app.modules.verification

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
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.modules.verification.models.*
import com.gigforce.app.modules.verification.service.RetrofitFactory
import com.gigforce.core.utils.GlideApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_verification_dropdown.*
import kotlinx.android.synthetic.main.layout_verification_dropdown.view.*


class AlternateAddressUpload: BaseFragment() {
    companion object {
        fun newInstance() = AlternateAddressUpload()
    }

    private lateinit var storage: FirebaseStorage
    var firebaseDB = FirebaseFirestore.getInstance()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    lateinit var fieldVerification:String;
    lateinit var layout: View
    private lateinit var ddFront: ImageView
    private lateinit var ddBack: ImageView
    lateinit var viewModel: VerificationViewModel
    private var PHOTO_CROP: Int = 45
    private var frontNotDone = 1;
    private var docUploaded = 0;

    var updatesDL: ArrayList<DL> = ArrayList()
    private lateinit var     id_number :String;
    private lateinit var     name_on_card :String;
    private lateinit var     fathers_name :String;
    private lateinit var     date_of_birth :String;
    private lateinit var     date_of_validity :String;
    private lateinit var     address :String;
    private lateinit var     district :String;
    private lateinit var     pincode :String;
    private lateinit var     state :String;
    private lateinit var     street_address :String;

    var updatesVoterID: ArrayList<VoterID> = ArrayList()
    private lateinit var     house_number :String;
    private lateinit var     age :String;
    private lateinit var     year_of_birth :String;

    var updatesPassport: ArrayList<Passport> = ArrayList();
    private lateinit var     first_name :String;
    private lateinit var     last_name :String;
    private lateinit var     mothers_name :String;
    private lateinit var     nationality :String;
    private lateinit var     place_of_birth :String;
    private lateinit var     date_of_issue :String;
    private lateinit var     date_of_expiry :String;
    private lateinit var     place_of_issue :String;
    private lateinit var     gender :String;
    private lateinit var     name_of_spouse :String;

    private var spinner: Spinner? = null
    private val paths =
        arrayOf("DrivingLicense", "VoterId", "Passport")
    private lateinit var filepathappender:String;
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
        layout = inflater.inflate(R.layout.layout_verification_dropdown, container, false)
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        layout.pbVeriDD.setProgress(40,true)

        spinner = layout.spinnerVeri
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, paths)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter

        //spinner?.setOnItemSelectedListener(this)
        return layout
    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() { // Handle the back button event
                onBackPressed()
            }
        }

    override fun onBackPressed() :Boolean{
        findNavController().popBackStack()
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)
        ddFront = layout.findViewById(R.id.VeriDD_front)
        ddBack = layout.findViewById(R.id.VeriDD_back)
        val photoCropIntent = Intent(context, PhotoCrop::class.java)
        photoCropIntent.putExtra("purpose","verification")
        photoCropIntent.putExtra("uid",viewModel.uid)
        photoCropIntent.putExtra("detectFace",0)

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                when (parent.getItemAtPosition(position).toString()) {
                    "DrivingLicense" -> {
                        fieldVerification = "DL";
                        filepathappender="/dl/"
                        photoCropIntent.putExtra("fbDir", "/verification/dl/")
                        photoCropIntent.putExtra("folder", "/verification/dl/")
                        Toast.makeText(
                            context,
                            "Upload DL",
                            Toast.LENGTH_LONG).show()
                    }
                    "Passport" -> {
                        fieldVerification = "Passport";
                        filepathappender="/passport/"
                        photoCropIntent.putExtra("fbDir", "/verification/passport/")
                        photoCropIntent.putExtra("folder", "/verification/passport/")
                        Toast.makeText(
                            context,
                            "Upload Passport",
                            Toast.LENGTH_LONG).show()
                    }
                    "VoterId" -> {
                        fieldVerification = "VoterID";
                        filepathappender="/voterid/"
                        photoCropIntent.putExtra("fbDir", "/verification/voterid/")
                        photoCropIntent.putExtra("folder", "/verification/voterid/")
                        Toast.makeText(
                            context,
                            "Upload VoterID",
                            Toast.LENGTH_LONG).show()
                    }
                }
            } // to close the onItemSelected

            override fun onNothingSelected(parent: AdapterView<*>) {
                Toast.makeText(
                    context,
                    "Please select which doc to be uploaded",
                    Toast.LENGTH_LONG).show()
            }
        }

        ddFront.setOnClickListener {
            photoCropIntent.putExtra("file", "adfront.jpg")
            startActivityForResult(photoCropIntent, PHOTO_CROP)
        }
        ddBack.setOnClickListener {
            if(ddFront.drawable==null) {
                Toast.makeText(
                    this.context,
                    "Please upload the front side first!",
                    Toast.LENGTH_LONG).show()
            }
            else {
                photoCropIntent.putExtra("file", "adback.jpg")
                startActivityForResult(photoCropIntent, PHOTO_CROP)
            }
        }

//        panFront = layout.findViewById(R.id.Pan_front)
//        panBack = layout.findViewById(R.id.Pan_back)
//        val photoCropIntent = Intent(context, PhotoCrop::class.java)
//        photoCropIntent.putExtra("folder", "/verification/pan/")
//        photoCropIntent.putExtra("fbDir", "/verification/pan/")
//        photoCropIntent.putExtra("detectFace",0)
//        panFront.setOnClickListener {
//            photoCropIntent.putExtra("file", "panfront.jpg")
//            startActivityForResult(photoCropIntent, PHOTO_CROP)
//        }
//        panBack.setOnClickListener {
//            photoCropIntent.putExtra("file", "panback.jpg")
//            startActivityForResult(photoCropIntent, PHOTO_CROP)
//        }

        buttonVeriDD2.setOnClickListener {
            findNavController().navigate(R.id.aadhaarUpload);
        }

        buttonVeriDD1.setOnClickListener {
            //if() docs are not uploaded
            if(docUploaded==1)
            {
                findNavController().navigate(R.id.bankUpload2)
            }
            else {
                Toast.makeText(
                    this.context,
                    "Please upload the doc before proceeding",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("CheckResult", "UseRequireInsteadOfGet")
    private fun idfyApiCall(postData: PostDataOCRs){
        if(this.context?.let { UtilMethods.isConnectedToInternet(it) }!!){
            this.context?.let { UtilMethods.showLoading(it) }
            //TODO Here based on the selection call the api instance - DL, VoterID, Passport
            val observable = RetrofitFactory.idfyApiCallAD().postOCR(postData)
            observable.subscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .subscribe({ response ->
                    UtilMethods.hideLoading()
                    //here we can load all the data required
                    var extractionOutput = response!!.result!!.extraction_output!!
                    firebaseDB.collection("Verification")
                        .document(uid).update(fieldVerification, FieldValue.arrayUnion(extractionOutput))
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
                var filepath = filepathappender+imageName;
                if(frontNotDone==1) {
                    uriFront = data?.getParcelableExtra("uri")!!;
                    //loadImage("verification",filepath, layout.VeriDD_front)
                    layout.VeriDD_front.setImageURI(uriFront);
                    frontNotDone = 0;
                }
                else{
                    uriBack = data?.getParcelableExtra("uri")!!;
                    var imgb641 = UtilMethods.encodeImageToBase64(requireContext(), uriFront);
                    var imgb642 = UtilMethods.encodeImageToBase64(requireContext(), uriBack);
                    var ocrdata = OCRDocsData(imgb641,imgb642,"yes")
                    //var ocrdata = OCRDocsData(imgb64,imgb64,"yes")
                    val taskid:String = "74f4c926-250c-43ca-9c53-453e87ceacd2";
                    val groupid:String = "8e16424a-58fc-4ba4-ab20-5bc8e7c3c41f";
                    var postData = PostDataOCRs(taskid,groupid,ocrdata!!)
                    idfyApiCall(postData)
                    //loadImage("verification",filepath, layout.VeriDD_back)
                    layout.VeriDD_back.setImageURI(uriBack);
                    docUploaded = 1;
                }
            }
        }
    }

    private fun loadImage(collection: String, filepath: String, layoutid: ImageView ) {
        var picRef: StorageReference = storage.reference.child(collection).child(filepath)
        GlideApp.with(this.requireContext())
            .load(picRef)
            .into(layoutid)
    }

    private fun addDLData() {
        updatesDL.add(
            DL(
                id_number = id_number,
                name_on_card = name_on_card,
                fathers_name = fathers_name,
                date_of_birth = date_of_birth,
                date_of_validity = date_of_validity,
                address = address,
                district = district,
                pincode = pincode,
                state = state,
                street_address = street_address
            )
        )
    }

    private fun addVoterIDData() {
        updatesVoterID.add(
            VoterID(
                id_number = id_number,
                name_on_card = name_on_card,
                fathers_name = fathers_name,
                date_of_birth = date_of_birth,
                address = address,
                state = state,
                district = district,
                street_address = street_address,
                house_number = house_number,
                pincode = pincode,
                gender = gender,
                age = age,
                year_of_birth = year_of_birth
            )
        )
    }

    private fun addPassportData() {
        updatesPassport.add(
            Passport(
                id_number = id_number,
                first_name = first_name,
                last_name = last_name,
                name_on_card = name_on_card,
                fathers_name = fathers_name,
                mothers_name = mothers_name,
                nationality = nationality,
                date_of_birth = date_of_birth,
                place_of_birth = place_of_birth,
                date_of_issue = date_of_issue,
                date_of_expiry = date_of_expiry,
                place_of_issue = place_of_issue,
                address = address,
                gender = gender,
                name_of_spouse = name_of_spouse
            )
        )
    }

    private fun saveNewDL() {
        viewModel.setVerificationDL(updatesDL)
    }

    private fun saveNewVoterID() {
        viewModel.setVerificationVoterID(updatesVoterID)
    }

    private fun saveNewPassport() {
        viewModel.setVerificationPassport(updatesPassport)
    }
}