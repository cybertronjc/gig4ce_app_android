//package com.gigforce.app.modules.verification
//
//import android.app.Activity
//import android.content.Intent
//import android.graphics.Bitmap
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.util.Base64
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.activity.OnBackPressedCallback
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.ViewModelProviders
//import androidx.navigation.fragment.findNavController
//import com.gigforce.app.R
//import com.gigforce.app.modules.auth.ui.main.Login
//import com.gigforce.app.modules.verification.models.Idfydata
//import com.gigforce.app.modules.verification.models.PostBody
//import com.gigforce.app.modules.verification.service.ApiFactory
//import com.google.gson.Gson
//import org.json.JSONObject
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import java.io.ByteArrayOutputStream
//
//class UploadFileVerification: Fragment() {
//    companion object {
//        fun newInstance() = Login()
//        private const val IMAGE_PICK_CODE = 999
//    }
//
//
//    private lateinit var imageViewVerification: ImageView
//    private lateinit var imageButtonVerification: Button
//    private lateinit var sendButtonVerification: Button
//    private var imageData: ByteArray? = null
//    private val postURL: String =
//        "https://ptsv2.com/t/54odo-1576291398/post" // remember to use your own api
//
//    lateinit var layout: View
//    lateinit var viewModel: VerificationViewModel
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//
//        super.onCreate(savedInstanceState)
//        val service = ApiFactory.placeholderApi
//        val idfyService = ApiFactory.idfyApi
//
//        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)
//        layout = inflater.inflate(R.layout.upload_file_verification, container, false)
//        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
//
//
//        imageViewVerification = layout.findViewById(R.id.imageViewVerification)
//
//        imageButtonVerification = layout.findViewById(R.id.imageButtonVerification)
//        imageButtonVerification.setOnClickListener {
//            launchGallery()
//        }
//        sendButtonVerification = layout.findViewById(R.id.sendButtonVerification)
//        sendButtonVerification.setOnClickListener {
//            uploadImage()
//        }
//
//        return layout
//    }
//
//    private fun launchGallery() {
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        startActivityForResult(intent, IMAGE_PICK_CODE)
//    }
//
//
//    private fun encodeImageToBase64(uri:Uri):String{
//        val baos = ByteArrayOutputStream()
//        val bitmap =  MediaStore.Images.Media.getBitmap(context?.contentResolver, uri);//BitmapFactory.decodeResource(resources, uri)
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//        val imageBytes: ByteArray = baos.toByteArray()
//        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
//            val uri = data?.data
//            if (uri != null) {
//                imageViewVerification.setImageURI(uri)
//
//                val taskid:String = "74f4c926-250c-43ca-9c53-453e87ceacd2";
//                val groupid:String = "8e16424a-58fc-4ba4-ab20-5bc8e7c3c41f";
//
//                val idfyService = ApiFactory.idfyApi
//
//                //encode image to base64 string
//                var imageString = encodeImageToBase64(uri);
//
//                val data = PostBody;//(taskid,groupid,"{ 'document1':$imageString+'.consent': 'yes','details': {} }");
//                data?.task_id=taskid;
//                data?.group_id=groupid;
//                data?.data= "{ 'document1':$imageString+'.consent': 'yes','details': {} }"
//
//                val gson = Gson()
//                val json = gson.toJson(data)
//
//                val call: Call<Idfydata> = idfyService.postAadhar(json);
//
//                /**Log the URL called*/
//                /**Log the URL called */
//                Log.wtf("URL Called", call.request().url().toString() + "")
//
//                Toast.makeText(
//                    this.activity,
//                    ">>"+call.request().url().toString(),
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                call.enqueue(object : Callback<Idfydata?> {
//                    override fun onResponse(call: Call<Idfydata?>, response: Response<Idfydata?>) {
//
//                        Toast.makeText(
//                            context,
//                            response.body().toString(),
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        //textview.text=response.body().toString()
//                    }
//
//                    override fun onFailure(call: Call<Idfydata?>, t: Throwable) {
//                        Toast.makeText(
//                            context,
//                            "Something went wrong...Error message: " + t.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                })
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data)
//    }
//
//    private fun uploadImage()
//    {
//
//    }
//
//    val callback: OnBackPressedCallback =
//        object : OnBackPressedCallback(true /* enabled by default */) {
//            override fun handleOnBackPressed() { // Handle the back button event
//                onBackPressed()
//            }
//        }
//
//    fun onBackPressed() {
//        findNavController().popBackStack()
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)
//    }
//}